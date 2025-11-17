package com.ecommerce.onlinestore.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

@Configuration
@EnableJpaAuditing
public class DatabaseConfig {

    // Базовая конфигурация через application.yml
    // Дополнительные настройки можно добавить здесь

    /*
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/online_store")
                .username("root")
                .password("password")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
    */
}
