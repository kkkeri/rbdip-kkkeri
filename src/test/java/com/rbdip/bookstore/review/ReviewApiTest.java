package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbdip.bookstore.reference.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReviewApiTest extends AbstractIntegrationTest {

    @Test
    void createsAndListsAnonymousReviewWithoutAnOrder() {
        Long productId = jdbcTemplate.queryForObject(
                "INSERT INTO products (name, price) VALUES ('Refactoring', 40.00) RETURNING id",
                Long.class);
        String url = "/products/" + productId + "/reviews";

        var created = restTemplate.postForEntity(
                url, Map.of("rating", 5, "comment", "Полезная книга"), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().path("id").isIntegralNumber()).isTrue();

        var listed = restTemplate.getForEntity(url, JsonNode.class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).isNotNull();
        assertThat(listed.getBody().size()).isEqualTo(1);

        JsonNode review = listed.getBody().get(0);
        assertThat(review.path("authorName").asText()).isEqualTo("anonymous");
        assertThat(review.path("rating").asInt()).isEqualTo(5);
        assertThat(review.path("comment").asText()).isEqualTo("Полезная книга");
    }
}