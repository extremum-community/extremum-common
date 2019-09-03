package io.extremum.mongo.repository;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AuditingEventListener;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is essentially MongoAuditingRegistrar with one customization. We cannot extend it as it is package local,
 * so we had to copy.
 * The customization is that it applies the auditing to all
 * {@link org.springframework.data.mongodb.core.mapping.MongoMappingContext}s available in the application context,
 * and not only to one. This allows to solve a nasty problem with ZonedDateTime used as created/modified field type.
 * The customization lies entirely in PersistentEntitiesLookup.
 *
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author rpuch
 */
public class AllMongoAuditingRegistrar extends AuditingBeanDefinitionRegistrarSupport {
    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableAllMongoAuditing.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAuditingHandlerBeanName()
     */
    @Override
    protected String getAuditingHandlerBeanName() {
        return "mongoAuditingHandler";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        super.registerBeanDefinitions(annotationMetadata, registry);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAuditHandlerBeanDefinitionBuilder(org.springframework.data.auditing.config.AuditingConfiguration)
     */
    @Override
    protected BeanDefinitionBuilder getAuditHandlerBeanDefinitionBuilder(AuditingConfiguration configuration) {

        Assert.notNull(configuration, "AuditingConfiguration must not be null!");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(IsNewAwareAuditingHandler.class);

        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(
                PersistentEntitiesLookup.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        builder.addConstructorArgValue(definition.getBeanDefinition());
        return configureDefaultAuditHandlerAttributes(configuration, builder);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#registerAuditListener(org.springframework.beans.factory.config.BeanDefinition, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    protected void registerAuditListenerBeanDefinition(BeanDefinition auditingHandlerDefinition,
            BeanDefinitionRegistry registry) {

        Assert.notNull(auditingHandlerDefinition, "BeanDefinition must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        BeanDefinitionBuilder listenerBeanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(AuditingEventListener.class);
        listenerBeanDefinitionBuilder
                .addConstructorArgValue(
                        ParsingUtils.getObjectFactoryBeanDefinition(getAuditingHandlerBeanName(), registry));

        registerInfrastructureBeanWithId(listenerBeanDefinitionBuilder.getBeanDefinition(),
                AuditingEventListener.class.getName(), registry);
    }

    /**
     * Simple helper to be able to wire the {@link PersistentEntities} from {@link MappingMongoConverter}s beans
     * available in the application context.
     */
    static class PersistentEntitiesLookup implements FactoryBean<PersistentEntities> {

        private final List<MappingMongoConverter> converters;

        /**
         * Creates a new {@link PersistentEntitiesLookup} for the given {@link MappingMongoConverter}s.
         *
         * @param converters must not be {@literal null}.
         */
        public PersistentEntitiesLookup(List<MappingMongoConverter> converters) {
            this.converters = new ArrayList<>(converters);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#getObject()
         */
        @Override
        public PersistentEntities getObject() {
            List<MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty>> contexts = converters
                    .stream()
                    .map(MappingMongoConverter::getMappingContext)
                    .collect(Collectors.toList());
            return new PersistentEntities(contexts);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#getObjectType()
         */
        @Override
        public Class<?> getObjectType() {
            return PersistentEntities.class;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.beans.factory.FactoryBean#isSingleton()
         */
        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
