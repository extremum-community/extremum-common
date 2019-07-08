package com.extremum.jpa.config;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilities;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilitiesAccessorConfigurator;
import com.extremum.jpa.properties.JpaProperties;
import com.extremum.jpa.repositories.EnableExtremumJpaRepositories;
import com.extremum.jpa.repositories.ExtremumJpaRepositoryFactoryBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnProperty("jpa.repository-packages")
@EnableConfigurationProperties(JpaProperties.class)
@EnableExtremumJpaRepositories(basePackages = "${jpa.repository-packages}",
        repositoryFactoryBeanClass = ExtremumJpaRepositoryFactoryBean.class)
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class JpaRepositoriesConfiguration {
    private final JpaProperties jpaProperties;

    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFacilities postgresqlDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver) {
        return new PostgresqlDescriptorFacilities(descriptorFactory, descriptorSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresqlDescriptorFacilitiesAccessorConfigurator postgresqlDescriptorFacilitiesAccessorConfigurator(
            PostgresqlDescriptorFacilities postgresqlDescriptorFacilities) {
        return new PostgresqlDescriptorFacilitiesAccessorConfigurator(postgresqlDescriptorFacilities);
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
