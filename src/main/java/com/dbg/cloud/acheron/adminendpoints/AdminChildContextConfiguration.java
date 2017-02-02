package com.dbg.cloud.acheron.adminendpoints;

import com.dbg.cloud.acheron.autoconfigure.admin.AdminServerProperties;
import org.apache.catalina.Valve;
import org.apache.catalina.valves.AccessLogValve;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebMvc
@EnableConfigurationProperties({AdminEndpointCorsProperties.class})
public class AdminChildContextConfiguration {

    @Autowired
    private AdminEndpointCorsProperties corsProperties;

    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet();

        // Ensure the parent configuration does not leak down to us
        dispatcherServlet.setDetectAllHandlerAdapters(false);
        dispatcherServlet.setDetectAllHandlerExceptionResolvers(false);
        dispatcherServlet.setDetectAllHandlerMappings(false);
        dispatcherServlet.setDetectAllViewResolvers(false);

        return dispatcherServlet;
    }

    @Bean
    public ServerCustomization serverCustomization() {
        return new ServerCustomization();
    }

    @Configuration
    protected static class AdminEndpointHandlerMappingConfiguration {

        @Autowired
        public void handlerMapping(AdminEndpointHandlerMapping mapping) {
            // In a child context we definitely want to see the parent endpoints
            mapping.setDetectHandlerMethodsInAncestorContexts(true);
        }
    }

    @Bean
    @ConditionalOnBean({ErrorAttributes.class, ServerProperties.class})
    public AdminErrorEndpoint errorEndpoint(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        return new AdminErrorEndpoint(errorAttributes, serverProperties.getError());
    }

    @Bean(name = DispatcherServlet.HANDLER_MAPPING_BEAN_NAME)
    public CompositeHandlerMapping compositeHandlerMapping() {
        return new CompositeHandlerMapping();
    }

    @Bean(name = DispatcherServlet.HANDLER_ADAPTER_BEAN_NAME)
    public CompositeHandlerAdapter compositeHandlerAdapter() {
        return new CompositeHandlerAdapter();
    }

    @Bean(name = DispatcherServlet.HANDLER_EXCEPTION_RESOLVER_BEAN_NAME)
    public CompositeHandlerExceptionResolver compositeHandlerExceptionResolver() {
        return new CompositeHandlerExceptionResolver();
    }

    static class ServerCustomization implements EmbeddedServletContainerCustomizer, Ordered {

        @Autowired
        private ListableBeanFactory beanFactory;

        private ServerProperties server;

        // This needs to be lazily initialized because EmbeddedServletContainerCustomizer
        // instances get their callback very early in the context lifecycle.
        private AdminServerProperties adminServerProperties;

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public void customize(final ConfigurableEmbeddedServletContainer container) {
            if (adminServerProperties == null) {
                adminServerProperties = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                        beanFactory, AdminServerProperties.class);
                server = BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, ServerProperties.class);
            }

            // Customize as per the parent context first (so e.g. the access logs go to the same place)
            server.customize(container);

            // Then reset the error pages
            container.setErrorPages(Collections.<ErrorPage>emptySet());

            // and the context path
            container.setContextPath("");

            // and add the admin-specific bits
            if (adminServerProperties.getPort() != null) {
                container.setPort(adminServerProperties.getPort());
            }

            if (adminServerProperties.getSsl() != null) {
                container.setSsl(this.adminServerProperties.getSsl());
            }

            if (adminServerProperties.getContextPath() != null) {
                container.setContextPath(adminServerProperties.getContextPath());
            }

            container.setServerHeader(server.getServerHeader());

            if (adminServerProperties.getAddress() != null) {
                container.setAddress(adminServerProperties.getAddress());
            }

            container.addErrorPages(new ErrorPage(server.getError().getPath()));
        }
    }

    @Bean
    public UndertowAccessLogCustomizer undertowAccessLogCustomizer() {
        return new UndertowAccessLogCustomizer();
    }

    @Bean
    @ConditionalOnClass(name = "org.apache.catalina.valves.AccessLogValve")
    public TomcatAccessLogCustomizer tomcatAccessLogCustomizer() {
        return new TomcatAccessLogCustomizer();
    }

    static class CompositeHandlerMapping implements HandlerMapping {

        @Autowired
        private ListableBeanFactory beanFactory;

        private List<HandlerMapping> mappings;

        @Override
        public HandlerExecutionChain getHandler(final HttpServletRequest request)
                throws Exception {
            if (this.mappings == null) {
                this.mappings = extractMappings();
            }

            for (HandlerMapping mapping : this.mappings) {
                final HandlerExecutionChain handler = mapping.getHandler(request);
                if (handler != null) {
                    return handler;
                }
            }

            return null;
        }

        private List<HandlerMapping> extractMappings() {
            final List<HandlerMapping> list = new ArrayList<HandlerMapping>();

            list.addAll(this.beanFactory.getBeansOfType(HandlerMapping.class).values());
            list.remove(this);
            AnnotationAwareOrderComparator.sort(list);

            return list;
        }
    }

    static class CompositeHandlerAdapter implements HandlerAdapter {

        @Autowired
        private ListableBeanFactory beanFactory;

        private List<HandlerAdapter> adapters;

        private List<HandlerAdapter> extractAdapters() {
            List<HandlerAdapter> list = new ArrayList<HandlerAdapter>();
            list.addAll(this.beanFactory.getBeansOfType(HandlerAdapter.class).values());
            list.remove(this);
            AnnotationAwareOrderComparator.sort(list);
            return list;
        }

        @Override
        public boolean supports(final Object handler) {
            if (this.adapters == null) {
                this.adapters = extractAdapters();
            }

            for (HandlerAdapter mapping : this.adapters) {
                if (mapping.supports(handler)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                                   final Object handler) throws Exception {
            if (this.adapters == null) {
                this.adapters = extractAdapters();
            }

            for (HandlerAdapter mapping : this.adapters) {
                if (mapping.supports(handler)) {
                    return mapping.handle(request, response, handler);
                }
            }

            return null;
        }

        @Override
        public long getLastModified(final HttpServletRequest request, final Object handler) {
            if (this.adapters == null) {
                this.adapters = extractAdapters();
            }

            for (HandlerAdapter mapping : this.adapters) {
                if (mapping.supports(handler)) {
                    return mapping.getLastModified(request, handler);
                }
            }

            return 0;
        }
    }

    static class CompositeHandlerExceptionResolver implements HandlerExceptionResolver {

        @Autowired
        private ListableBeanFactory beanFactory;

        private List<HandlerExceptionResolver> resolvers;

        private List<HandlerExceptionResolver> extractResolvers() {
            final List<HandlerExceptionResolver> list = new ArrayList<HandlerExceptionResolver>();

            list.addAll(this.beanFactory.getBeansOfType(HandlerExceptionResolver.class).values());
            list.remove(this);
            AnnotationAwareOrderComparator.sort(list);

            return list;
        }

        @Override
        public ModelAndView resolveException(HttpServletRequest request,
                                             HttpServletResponse response, Object handler, Exception ex) {
            if (this.resolvers == null) {
                this.resolvers = extractResolvers();
            }

            for (HandlerExceptionResolver mapping : this.resolvers) {
                final ModelAndView mav = mapping.resolveException(request, response, handler, ex);
                if (mav != null) {
                    return mav;
                }
            }

            return null;
        }
    }

    static abstract class AccessLogCustomizer<T extends EmbeddedServletContainerFactory>
            implements EmbeddedServletContainerCustomizer, Ordered {

        private final Class<T> factoryClass;

        AccessLogCustomizer(Class<T> factoryClass) {
            this.factoryClass = factoryClass;
        }

        protected String customizePrefix(String prefix) {
            return "admin_" + prefix;
        }

        @Override
        public int getOrder() {
            return 1;
        }

        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {
            if (this.factoryClass.isInstance(container)) {
                customize(this.factoryClass.cast(container));
            }
        }

        abstract void customize(T container);

    }

    static class TomcatAccessLogCustomizer extends AccessLogCustomizer<TomcatEmbeddedServletContainerFactory> {

        TomcatAccessLogCustomizer() {
            super(TomcatEmbeddedServletContainerFactory.class);
        }

        @Override
        public void customize(final TomcatEmbeddedServletContainerFactory container) {
            final AccessLogValve accessLogValve = findAccessLogValve(container);

            if (accessLogValve == null) {
                return;
            }

            accessLogValve.setPrefix(customizePrefix(accessLogValve.getPrefix()));
        }

        private AccessLogValve findAccessLogValve(final TomcatEmbeddedServletContainerFactory container) {
            for (Valve engineValve : container.getEngineValves()) {
                if (engineValve instanceof AccessLogValve) {
                    return (AccessLogValve) engineValve;
                }
            }

            return null;
        }
    }

    static class UndertowAccessLogCustomizer extends AccessLogCustomizer<UndertowEmbeddedServletContainerFactory> {

        UndertowAccessLogCustomizer() {
            super(UndertowEmbeddedServletContainerFactory.class);
        }

        @Override
        public void customize(final UndertowEmbeddedServletContainerFactory container) {
            container.setAccessLogPrefix(customizePrefix(container.getAccessLogPrefix()));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminEndpointHandlerMapping endpointHandlerMapping() {
        final Set<? extends AdminEndpoint> endpoints = adminEndpoints().getEndpoints();
        final CorsConfiguration corsConfiguration = getCorsConfiguration(this.corsProperties);
        final AdminEndpointHandlerMapping mapping = new AdminEndpointHandlerMapping(endpoints, corsConfiguration);

        return mapping;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminEndpoints adminEndpoints() {
        return new AdminEndpoints();
    }

    private CorsConfiguration getCorsConfiguration(final AdminEndpointCorsProperties properties) {
        if (CollectionUtils.isEmpty(properties.getAllowedOrigins())) {
            return null;
        }

        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getAllowedOrigins());

        if (!CollectionUtils.isEmpty(properties.getAllowedHeaders())) {
            configuration.setAllowedHeaders(properties.getAllowedHeaders());
        }

        if (!CollectionUtils.isEmpty(properties.getAllowedMethods())) {
            configuration.setAllowedMethods(properties.getAllowedMethods());
        }

        if (!CollectionUtils.isEmpty(properties.getExposedHeaders())) {
            configuration.setExposedHeaders(properties.getExposedHeaders());
        }

        if (properties.getMaxAge() != null) {
            configuration.setMaxAge(properties.getMaxAge());
        }

        if (properties.getAllowCredentials() != null) {
            configuration.setAllowCredentials(properties.getAllowCredentials());
        }

        return configuration;
    }
}
