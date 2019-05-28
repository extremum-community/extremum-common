package com.extremum.config;

import com.extremum.factory.PostgresqlDescriptorFactory;
import com.extremum.factory.PostgresqlDescriptorFactoryAccessorConfigurator;
import com.extremum.properties.JpaProperties;
import com.extremum.repositories.EnableExtremumJpaRepositories;
import com.extremum.repositories.ExtremumJpaRepositoryFactoryBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(JpaProperties.class)
@EnableExtremumJpaRepositories(basePackages = "${jpa.repository-packages}",
        repositoryFactoryBeanClass = ExtremumJpaRepositoryFactoryBean.class)
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@ConditionalOnProperty(JpaProperties.REPOSITORY_PACKAGES_PROPERTY)
@RequiredArgsConstructor
public class JpaRepositoriesConfiguration {
    private final JpaProperties jpaProperties;

    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFactory postgresqlDescriptorFactory() {
        return new PostgresqlDescriptorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFactoryAccessorConfigurator postgresqlDescriptorFactoryAccessorConfigurator() {
        return new PostgresqlDescriptorFactoryAccessorConfigurator(postgresqlDescriptorFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(jpaProperties.getUri())
                .username(jpaProperties.getUsername())
                .password(jpaProperties.getPassword())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        vendorAdapter.setShowSql(jpaProperties.isShowSql());

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(jpaProperties.getEntityPackages().toArray(new String[0]));
        factory.setDataSource(dataSource);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }
}
