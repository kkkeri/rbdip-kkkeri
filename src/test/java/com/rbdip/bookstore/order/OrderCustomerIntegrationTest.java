package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.reference.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class OrderCustomerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void twoOrdersWithSameContactsShareOneCustomer() {
        Long productId = jdbcTemplate.queryForObject(
                "INSERT INTO products (name, price) VALUES ('Refactoring', 40.00) RETURNING id",
                Long.class);

        var request = new CreateOrderRequest(
                "Анна Иванова", "Невский 10", null, "regular", null,
                List.of(new CreateOrderRequest.Item(productId, 1)));

        var firstOrder = restTemplate.postForEntity("/orders", request, Void.class);
        assertThat(firstOrder.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var secondOrder = restTemplate.postForEntity("/orders", request, Void.class);
        assertThat(secondOrder.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        List<Long> customerIds = jdbcTemplate.queryForList(
                "SELECT id FROM customers", Long.class);
        assertThat(customerIds).hasSize(1);

        Long customerId = customerIds.get(0);
        List<Long> orderCustomerIds = jdbcTemplate.queryForList(
                "SELECT customer_id FROM orders ORDER BY id", Long.class);
        assertThat(orderCustomerIds).containsExactly(customerId, customerId);

        List<Long> productIds = jdbcTemplate.queryForList(
                "SELECT product_id FROM order_items ORDER BY id", Long.class);
        assertThat(productIds).containsExactly(productId, productId);
    }
}