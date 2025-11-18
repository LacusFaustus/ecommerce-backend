package com.ecommerce.onlinestore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DiagnosticTest {

    @Test
    void contextLoads() {
        // Этот тест покажет детальную ошибку
    }
}
