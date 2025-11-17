package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.util.TestConstants;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();

        testCart = TestDataFactory.createCart(null, TestConstants.TEST_SESSION_ID);
        testCart = cartRepository.save(testCart);
    }

    @Test
    void shouldFindCartBySessionId() {
        Optional<Cart> found = cartRepository.findBySessionId(TestConstants.TEST_SESSION_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo(TestConstants.TEST_SESSION_ID);
    }

    @Test
    void shouldFindCartByIdWithItems() {
        Optional<Cart> found = cartRepository.findByIdWithItems(testCart.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testCart.getId());
    }

    @Test
    void shouldReturnEmptyWhenCartNotFound() {
        Optional<Cart> found = cartRepository.findBySessionId("non-existent-session");

        assertThat(found).isEmpty();
    }
}
