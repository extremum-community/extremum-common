package io.extremum.mongo.springdata;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

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
                .rootBeanDefinition(AuditingEntityCallback.class);
        listenerBeanDefinitionBuilder
                .addConstructorArgValue(
                        ParsingUtils.getObjectFactoryBeanDefinition(getAuditingHandlerBeanName(), registry));

        registerInfrastructureBeanWithId(listenerBeanDefinitionBuilder.getBeanDefinition(),
                AuditingEntityCallback.class.getName(), registry);
    }
}
