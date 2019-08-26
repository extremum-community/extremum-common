package io.extremum.jpa.config;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.jpa.facilities.PostgresDescriptorFacilities;
import io.extremum.jpa.facilities.PostgresDescriptorFacilitiesAccessorConfigurator;
import io.extremum.jpa.facilities.PostgresDescriptorFacilitiesImpl;
import io.extremum.jpa.properties.JpaProperties;
import io.extremum.jpa.repositories.EnableExtremumJpaRepositories;
import io.extremum.jpa.repositories.ExtremumJpaRepositoryFactoryBean;
import io.extremum.jpa.tx.JpaCollectionTransactor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

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
    public PostgresDescriptorFacilities postgresqlDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver) {
        return new PostgresDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresDescriptorFacilitiesAccessorConfigurator postgresqlDescriptorFacilitiesAccessorConfigurator(
            PostgresDescriptorFacilities postgresDescriptorFacilities) {
        return new PostgresDescriptorFacilitiesAccessorConfigurator(postgresDescriptorFacilities);
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

    @Bean
    @ConditionalOnMissingBean
    public TransactionOperations jpaTransactionOperations(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaCollectionTransactor jpaCollectionTransactor(
            @Qualifier("jpaTransactionOperations") TransactionOperations transactionOperations) {
        return new JpaCollectionTransactor(transactionOperations);
    }
}
