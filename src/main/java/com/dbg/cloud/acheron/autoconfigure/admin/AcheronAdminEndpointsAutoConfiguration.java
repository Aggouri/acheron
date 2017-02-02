package com.dbg.cloud.acheron.autoconfigure.admin;

import com.dbg.cloud.acheron.adminendpoints.AdminChildContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.type.MethodMetadata;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Modifier;

@Configuration
@Slf4j
public class AcheronAdminEndpointsAutoConfiguration implements ApplicationContextAware, BeanFactoryAware,
        SmartInitializingSingleton {

    private ApplicationContext applicationContext;

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // Determine admin port
        AdminServerPort adminPort = AdminServerPort.DIFFERENT;
        if (this.applicationContext instanceof WebApplicationContext) {
            adminPort = AdminServerPort.get(this.applicationContext.getEnvironment(), this.beanFactory);
        }

        if (adminPort == AdminServerPort.DIFFERENT) {
            // admin port is different than server port
            if (this.applicationContext instanceof EmbeddedWebApplicationContext &&
                    ((EmbeddedWebApplicationContext) this.applicationContext).getEmbeddedServletContainer() != null) {
                createChildAdminContext();
            } else {
                log.error("Could not start embedded admin container.");
            }
        } else {
            log.error("Could not start embedded admin container as it is set up the same as server port.");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("Setting application context {}", applicationContext.getId());
    }

    private void createChildAdminContext() {
        final AnnotationConfigEmbeddedWebApplicationContext childContext =
                new AnnotationConfigEmbeddedWebApplicationContext();

        childContext.setParent(this.applicationContext);
        childContext.setNamespace("admin");
        childContext.setId(this.applicationContext.getId() + ":admin");
        childContext.setClassLoader(this.applicationContext.getClassLoader());

        childContext.register(
                AdminChildContextConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class,
                EmbeddedServletContainerAutoConfiguration.class,
                DispatcherServletAutoConfiguration.class);

        registerEmbeddedServletContainerFactory(childContext);

        CloseAdminContextListener.addIfPossible(this.applicationContext, childContext);

        childContext.refresh();
    }

    private void registerEmbeddedServletContainerFactory(
            final AnnotationConfigEmbeddedWebApplicationContext childContext) {
        try {
            final ConfigurableListableBeanFactory beanFactory = childContext.getBeanFactory();
            if (beanFactory instanceof BeanDefinitionRegistry) {
                final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                registry.registerBeanDefinition("embeddedServletContainerFactory",
                        new RootBeanDefinition(determineEmbeddedServletContainerFactoryClass()));
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // Ignore and assume auto-configuration
        }
    }

    private Class<?> determineEmbeddedServletContainerFactoryClass() throws NoSuchBeanDefinitionException {
        final Class<?> servletContainerFactoryClass =
                this.applicationContext.getBean(EmbeddedServletContainerFactory.class).getClass();

        if (cannotBeInstantiated(servletContainerFactoryClass)) {
            throw new FatalBeanException(
                    "EmbeddedServletContainerFactory implementation " + servletContainerFactoryClass.getName()
                            + " cannot be instantiated. To allow a separate admin port to be used, a top-level class "
                            + "or static inner class should be used instead");
        }

        return servletContainerFactoryClass;
    }

    private boolean cannotBeInstantiated(Class<?> clazz) {
        return clazz.isLocalClass()
                || (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()))
                || clazz.isAnonymousClass();
    }

    protected enum AdminServerPort {

        DISABLE, SAME, DIFFERENT;

        public static AdminServerPort get(final Environment environment, final BeanFactory beanFactory) {
            Integer serverPort = getPortProperty(environment, "server.");
            if (serverPort == null && hasCustomBeanDefinition(beanFactory, ServerProperties.class,
                    ServerPropertiesAutoConfiguration.class)) {
                serverPort = getTemporaryBean(beanFactory, ServerProperties.class).getPort();
            }

            Integer adminPort = getPortProperty(environment, "admin.");
            if (adminPort == null && hasCustomBeanDefinition(beanFactory, AdminServerProperties.class,
                    AdminServerPropertiesAutoConfiguration.class)) {
                adminPort = getTemporaryBean(beanFactory, AdminServerProperties.class).getPort();
            }

            if (adminPort != null && adminPort < 0) {
                return DISABLE;
            }

            return ((adminPort == null)
                    || (serverPort == null && adminPort.equals(8080))
                    || (adminPort != 0 && adminPort.equals(serverPort)) ? SAME : DIFFERENT);
        }

        private static <T> T getTemporaryBean(final BeanFactory beanFactory, final Class<T> type) {
            if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
                return null;
            }

            final ConfigurableListableBeanFactory listable = (ConfigurableListableBeanFactory) beanFactory;

            final String[] names = listable.getBeanNamesForType(type, true, false);
            if (names == null || names.length != 1) {
                return null;
            }

            // Use a temporary child bean factory to avoid instantiating the bean in the
            // parent (it won't be bound to the environment yet)
            return createTemporaryBean(type, listable, listable.getBeanDefinition(names[0]));
        }

        private static <T> T createTemporaryBean(final Class<T> type, final ConfigurableListableBeanFactory parent,
                                                 final BeanDefinition definition) {
            final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory(parent);
            beanFactory.registerBeanDefinition(type.getName(), definition);
            return beanFactory.getBean(type);
        }

        private static Integer getPortProperty(final Environment environment, final String prefix) {
            final RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment, prefix);
            return resolver.getProperty("port", Integer.class);
        }

        private static <T> boolean hasCustomBeanDefinition(BeanFactory beanFactory,
                                                           Class<T> type, Class<?> configClass) {
            if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
                return false;
            }

            return hasCustomBeanDefinition((ConfigurableListableBeanFactory) beanFactory, type, configClass);
        }

        private static <T> boolean hasCustomBeanDefinition(final ConfigurableListableBeanFactory beanFactory,
                                                           final Class<T> type, final Class<?> configClass) {
            final String[] names = beanFactory.getBeanNamesForType(type, true, false);
            if (names == null || names.length != 1) {
                return false;
            }

            final BeanDefinition definition = beanFactory.getBeanDefinition(names[0]);
            if (definition instanceof AnnotatedBeanDefinition) {
                final MethodMetadata factoryMethodMetadata =
                        ((AnnotatedBeanDefinition) definition).getFactoryMethodMetadata();
                if (factoryMethodMetadata != null) {
                    final String className = factoryMethodMetadata.getDeclaringClassName();
                    return !configClass.getName().equals(className);
                }
            }

            return true;
        }
    }

    private static class CloseAdminContextListener implements ApplicationListener<ApplicationEvent> {

        private final ApplicationContext parentContext;

        private final ConfigurableApplicationContext childContext;

        CloseAdminContextListener(final ApplicationContext parentContext,
                                  final ConfigurableApplicationContext childContext) {
            this.parentContext = parentContext;
            this.childContext = childContext;
        }

        @Override
        public void onApplicationEvent(final ApplicationEvent event) {
            if (event instanceof ContextClosedEvent) {
                onContextClosedEvent((ContextClosedEvent) event);
            }

            if (event instanceof ApplicationFailedEvent) {
                onApplicationFailedEvent((ApplicationFailedEvent) event);
            }
        }

        private void onContextClosedEvent(final ContextClosedEvent event) {
            propagateCloseIfNecessary(event.getApplicationContext());
        }

        private void onApplicationFailedEvent(final ApplicationFailedEvent event) {
            propagateCloseIfNecessary(event.getApplicationContext());
        }

        private void propagateCloseIfNecessary(final ApplicationContext applicationContext) {
            if (applicationContext == this.parentContext) {
                this.childContext.close();
            }
        }

        public static void addIfPossible(final ApplicationContext parentContext,
                                         final ConfigurableApplicationContext childContext) {

            if (parentContext instanceof ConfigurableApplicationContext) {
                add((ConfigurableApplicationContext) parentContext, childContext);
            }
        }

        private static void add(final ConfigurableApplicationContext parentContext,
                                final ConfigurableApplicationContext childContext) {
            parentContext.addApplicationListener(new CloseAdminContextListener(parentContext, childContext));
        }

    }
}
