/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.autoconfigure.reactiveweb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.CacheControl;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.config.ResourceChainRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebReactiveConfiguration;
import org.springframework.web.reactive.resource.AppCacheManifestTransformer;
import org.springframework.web.reactive.resource.GzipResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolver;
import org.springframework.web.reactive.resource.VersionResourceResolver;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

/**
 * @author Brian Clozel
 * @author Rob Winch
 */
@Configuration
@ConditionalOnClass({DispatcherHandler.class, HttpHandler.class})
@AutoConfigureAfter({ReactiveHttpServerAutoConfiguration.class})
public class ReactiveWebAutoConfiguration {

	@Configuration
	@ConditionalOnMissingBean(WebReactiveConfiguration.class)
	@EnableConfigurationProperties({ResourceProperties.class, WebReactiveProperties.class})
	public static class WebReactiveConfig extends WebReactiveConfiguration {

		private static final Log logger = LogFactory
				.getLog(WebReactiveConfig.class);

		private final ResourceProperties resourceProperties;

		private final WebReactiveProperties webReactiveProperties;

		private final List<HandlerMethodArgumentResolver> argumentResolvers;

		final ResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer;

		public WebReactiveConfig(ResourceProperties resourceProperties,
				WebReactiveProperties webReactiveProperties,
				ObjectProvider<List<HandlerMethodArgumentResolver>> resolvers,
				ObjectProvider<ResourceHandlerRegistrationCustomizer> resourceHandlerRegistrationCustomizerProvider) {
			this.resourceProperties = resourceProperties;
			this.webReactiveProperties = webReactiveProperties;
			this.argumentResolvers = resolvers.getIfAvailable();
			this.resourceHandlerRegistrationCustomizer = resourceHandlerRegistrationCustomizerProvider.getIfAvailable();
		}

		@Override
		protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			if (this.argumentResolvers != null) {
				resolvers.addAll(this.argumentResolvers);
			}
		}

		@Override
		protected void addResourceHandlers(ResourceHandlerRegistry registry) {
			if (!this.resourceProperties.isAddMappings()) {
				logger.debug("Default resource handling disabled");
				return;
			}
			Integer cachePeriod = this.resourceProperties.getCachePeriod();
			if (!registry.hasMappingForPattern("/webjars/**")) {
				ResourceHandlerRegistration registration = registry
						.addResourceHandler("/webjars/**")
						.addResourceLocations("classpath:/META-INF/resources/webjars/");
				if (cachePeriod != null) {
					registration.setCacheControl(CacheControl.maxAge(cachePeriod, TimeUnit.SECONDS));
				}
				customizeResourceHandlerRegistration(registration);
			}
			String staticPathPattern = this.webReactiveProperties.getStaticPathPattern();
			if (!registry.hasMappingForPattern(staticPathPattern)) {
				ResourceHandlerRegistration registration = registry.addResourceHandler(staticPathPattern)
						.addResourceLocations(this.resourceProperties.getStaticLocations());
				if (cachePeriod != null) {
					registration.setCacheControl(CacheControl.maxAge(cachePeriod, TimeUnit.SECONDS));
				}
				customizeResourceHandlerRegistration(registration);
			}
		}

		private void customizeResourceHandlerRegistration(
				ResourceHandlerRegistration registration) {
			if (this.resourceHandlerRegistrationCustomizer != null) {
				this.resourceHandlerRegistrationCustomizer.customize(registration);
			}

		}
	}

	@Configuration
	@Import(WebReactiveConfig.class)
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

	@Configuration
	@ConditionalOnEnabledResourceChain
	static class ResourceChainCustomizerConfiguration {

		@Bean
		public ResourceChainResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer() {
			return new ResourceChainResourceHandlerRegistrationCustomizer();
		}

	}

	interface ResourceHandlerRegistrationCustomizer {

		void customize(ResourceHandlerRegistration registration);

	}

	private static class ResourceChainResourceHandlerRegistrationCustomizer
			implements ResourceHandlerRegistrationCustomizer {

		@Autowired
		private ResourceProperties resourceProperties = new ResourceProperties();

		@Override
		public void customize(ResourceHandlerRegistration registration) {
			ResourceProperties.Chain properties = this.resourceProperties.getChain();
			configureResourceChain(properties,
					registration.resourceChain(properties.isCache()));
		}

		private void configureResourceChain(ResourceProperties.Chain properties,
				ResourceChainRegistration chain) {
			ResourceProperties.Strategy strategy = properties.getStrategy();
			if (strategy.getFixed().isEnabled() || strategy.getContent().isEnabled()) {
				chain.addResolver(getVersionResourceResolver(strategy));
			}
			if (properties.isGzipped()) {
				chain.addResolver(new GzipResourceResolver());
			}
			if (properties.isHtmlApplicationCache()) {
				chain.addTransformer(new AppCacheManifestTransformer());
			}
		}

		private ResourceResolver getVersionResourceResolver(
				ResourceProperties.Strategy properties) {
			VersionResourceResolver resolver = new VersionResourceResolver();
			if (properties.getFixed().isEnabled()) {
				String version = properties.getFixed().getVersion();
				String[] paths = properties.getFixed().getPaths();
				resolver.addFixedVersionStrategy(version, paths);
			}
			if (properties.getContent().isEnabled()) {
				String[] paths = properties.getContent().getPaths();
				resolver.addContentVersionStrategy(paths);
			}
			return resolver;
		}

	}

}
