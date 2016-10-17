package org.springframework.boot.autoconfigure.reactiveweb;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnReactiveWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import java.util.List;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnReactiveWebApplication
@ConditionalOnClass(DispatcherHandler.class)
public class DispatcherHandlerAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(DispatcherHandler.class)
    public static class DispatcherHandlerConfiguration
            implements ApplicationContextAware {

        public static final String DEFAULT_DISPATCHER_HANDLER_BEAN_NAME = "dispatcherHandler";

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext)
                throws BeansException {
            this.applicationContext = applicationContext;
        }

        @Bean
        @ConditionalOnMissingBean(name = DEFAULT_DISPATCHER_HANDLER_BEAN_NAME, value = DispatcherHandler.class)
        public DispatcherHandler dispatcherHandler() {
            DispatcherHandler dispatcherHandler = new DispatcherHandler();
            dispatcherHandler.setApplicationContext(applicationContext);
            return dispatcherHandler;
        }
    }

    @Configuration
    @Import(DispatcherHandlerConfiguration.class)
    public static class WebReactiveHandlerConfiguration {

        private final List<WebFilter> webFilters;

        public WebReactiveHandlerConfiguration(
                ObjectProvider<List<WebFilter>> webFilters) {
            this.webFilters = webFilters.getIfAvailable();
            if (this.webFilters != null) {
                AnnotationAwareOrderComparator.sort(this.webFilters);
            }
        }

        @Bean
        public HttpHandler httpHandler(DispatcherHandler dispatcherHandler) {
            WebHttpHandlerBuilder builder = WebHttpHandlerBuilder
                    .webHandler(dispatcherHandler)
                    .exceptionHandlers(new ResponseStatusExceptionHandler());
            if (this.webFilters != null) {
                builder.filters(this.webFilters.toArray(new WebFilter[0]));
            }
            return builder.build();
        }

    }

}
