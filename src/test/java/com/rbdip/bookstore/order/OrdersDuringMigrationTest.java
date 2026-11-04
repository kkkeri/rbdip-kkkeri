package com.rbdip.bookstore.order;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.target=8")
@Testcontainers
class OrdersDuringMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbc;

    @LocalServerPort
    int port;

    @Test
    void servesOrdersWhileContractMigrationIsRunning() throws Exception {
        Long customer = jdbc.queryForObject("""
                INSERT INTO customers (first_name, last_name) VALUES ('Anna', 'Petrova') RETURNING id
                """, Long.class);
        jdbc.update("INSERT INTO orders (customer_id, status) VALUES (?, 'new')", customer);
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/orders"))
                .timeout(Duration.ofSeconds(15)).GET().build();
        assertOrderVisible(client.send(request, HttpResponse.BodyHandlers.ofString()));

        var flyway = Flyway.configure().dataSource(dataSource).target("9").load();
        var responses = new ArrayList<CompletableFuture<HttpResponse<String>>>();
        CompletableFuture<Void> migration;

        try (Connection gate = dataSource.getConnection(); Statement lock = gate.createStatement()) {
            gate.setAutoCommit(false);
            // Задерживаем V9 на первой ALTER TABLE до появления HTTP-запросов.
            lock.execute("LOCK TABLE customers IN ACCESS SHARE MODE");
            migration = CompletableFuture.runAsync(flyway::migrate);
            waitForBlockedLock("AccessExclusiveLock");
            for (int i = 0; i < 4; i++) {
                responses.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
            }
            waitForBlockedLock("AccessShareLock");
            gate.commit();
        }

        migration.get(15, SECONDS);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("9");
        for (var response : responses) {
            assertOrderVisible(response.get(15, SECONDS));
        }
        assertOrderVisible(client.send(request, HttpResponse.BodyHandlers.ofString()));
    }

    private void waitForBlockedLock(String mode) {
        await().atMost(5, SECONDS).until(() -> Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM pg_locks
                               WHERE relation = 'customers'::regclass AND mode = ? AND NOT granted)
                """, Boolean.class, mode)));
    }

    private static void assertOrderVisible(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Anna Petrova");
    }
}
