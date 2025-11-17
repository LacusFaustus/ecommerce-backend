package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Order;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        testOrder = TestDataFactory.createOrder(null, "ORD-123", OrderStatus.PENDING);
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void shouldFindOrderByOrderNumber() {
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-123");

        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-123");
    }

    @Test
    void shouldFindOrdersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        assertThat(orders.getContent()).hasSize(1);
        assertThat(orders.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void shouldFindOrdersByCustomerEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderRepository.findByCustomerInfo_CustomerEmail(
                "test@example.com", pageable);

        assertThat(orders.getContent()).hasSize(1);
        assertThat(orders.getContent().get(0).getCustomerInfo().getCustomerEmail())
                .isEqualTo("test@example.com");
    }
}
