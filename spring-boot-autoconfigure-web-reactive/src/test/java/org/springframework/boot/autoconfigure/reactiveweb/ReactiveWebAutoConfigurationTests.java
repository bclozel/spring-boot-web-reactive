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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import org.springframework.boot.context.embedded.ReactiveServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.accept.CompositeContentTypeResolver;
import org.springframework.web.reactive.config.WebReactiveConfiguration;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.FilteringWebHandler;
import org.springframework.web.server.handler.WebHandlerDecorator;

/**
 * @author Brian Clozel
 */
public class ReactiveWebAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@Test
	public void shouldNotProcessIfExistingWebReactiveConfiguration() throws Exception {
		this.context = new AnnotationConfigApplicationContext(BaseConfiguration.class, WebReactiveConfiguration.class);

		assertThat(this.context.getBeansOfType(RequestMappingHandlerMapping.class).size()).isEqualTo(1);
		assertThat(this.context.getBeansOfType(RequestMappingHandlerAdapter.class).size()).isEqualTo(1);
	}

	@Test
	public void shouldCreateDefaultBeans() throws Exception {
		this.context = new AnnotationConfigApplicationContext(BaseConfiguration.class);

		assertThat(this.context.getBeansOfType(RequestMappingHandlerMapping.class).size()).isEqualTo(1);
		assertThat(this.context.getBeansOfType(RequestMappingHandlerAdapter.class).size()).isEqualTo(1);
		assertThat(this.context.getBeansOfType(CompositeContentTypeResolver.class).size()).isEqualTo(1);
	}

	@Test
	public void shouldRegisterCustomHandlerMethodArgumentResolver() throws Exception {
		this.context = new AnnotationConfigApplicationContext(CustomArgumentResolvers.class);

		RequestMappingHandlerAdapter adapter = this.context.getBean(RequestMappingHandlerAdapter.class);
		assertThat(adapter.getArgumentResolvers())
				.contains(this.context.getBean("firstResolver", HandlerMethodArgumentResolver.class),
						this.context.getBean("secondResolver", HandlerMethodArgumentResolver.class));
	}

	@Test
	public void shouldRegisterSingleDispatcherHandler() throws Exception {
		this.context = new AnnotationConfigApplicationContext(ExistingDispatcherHandler.class);

		assertThat(this.context.getBeansOfType(DispatcherHandler.class).size()).isEqualTo(1);
		assertThat(this.context.getBean("dispatcherHandler", DispatcherHandler.class)).isNotNull();
	}

	@Test
	public void shouldRegisterCustomWebFilters() throws Exception {
		this.context = new AnnotationConfigApplicationContext(CustomWebFilters.class);

		HttpHandler handler = this.context.getBean(HttpHandler.class);
		assertThat(handler).isInstanceOf(WebHandler.class);
		WebHandler webHandler = (WebHandler) handler;
		while (webHandler instanceof WebHandlerDecorator) {
			if (webHandler instanceof FilteringWebHandler) {
				FilteringWebHandler filteringWebHandler = (FilteringWebHandler) webHandler;
				assertThat(filteringWebHandler.getFilters()).contains(
						this.context.getBean("firstWebFilter", WebFilter.class),
						this.context.getBean("secondWebFilter", WebFilter.class));
				return;
			}
			webHandler = ((WebHandlerDecorator) webHandler).getDelegate();
		}
		fail("Did not find any FilteringWebHandler");
	}


	@Configuration
	protected static class CustomWebFilters extends BaseConfiguration {

		@Bean
		public WebFilter firstWebFilter() {
			return mock(WebFilter.class);
		}

		@Bean
		public WebFilter secondWebFilter() {
			return mock(WebFilter.class);
		}
	}


	@Configuration
	protected static class ExistingDispatcherHandler extends BaseConfiguration {

		@Bean
		public DispatcherHandler dispatcherHandler() {
			return new DispatcherHandler();
		}
	}

	@Configuration
	protected static class CustomArgumentResolvers extends BaseConfiguration {

		@Bean
		public HandlerMethodArgumentResolver firstResolver() {
			return mock(HandlerMethodArgumentResolver.class);
		}

		@Bean
		public HandlerMethodArgumentResolver secondResolver() {
			return mock(HandlerMethodArgumentResolver.class);
		}

	}

	@Configuration
	@Import({ReactiveWebAutoConfiguration.class})
	@EnableConfigurationProperties(ReactiveServerProperties.class)
	protected static class BaseConfiguration {

	}
}
