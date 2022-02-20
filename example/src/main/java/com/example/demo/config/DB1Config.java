package com.example.demo.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.demo.repo.db1",
        entityManagerFactoryRef = "db1EntityManagerFactory",
        transactionManagerRef = "db1TransactionManager"
)
public class DB1Config {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "db1.datasource.hikari")
    public DataSource db1DataSource() {
        return DataSourceBuilder.create().build();
    }

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#configurations
    private static Map<String, String> jpaProperties(String dialect, String ddl) {
        return Map.of(
                "hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName(),
                "hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName(),
                "hibernate.dialect", dialect,
                "hibernate.hbm2ddl.auto", ddl
        );
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                          DataSource db1DataSource,
                                                                          @Value("${db1.jpa.hibernate.dialect}") String dialect,
                                                                          @Value("${db1.jpa.hibernate.ddl-auto}") String ddl) {
        return builder
                .dataSource(db1DataSource)
                .packages("com.example.demo.repo.db1.entity")
                .properties(jpaProperties(dialect, ddl))
                .persistenceUnit("db1")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager db1TransactionManager(EntityManagerFactory db1EntityManagerFactory) {
        return new JpaTransactionManager(db1EntityManagerFactory);
    }
}
