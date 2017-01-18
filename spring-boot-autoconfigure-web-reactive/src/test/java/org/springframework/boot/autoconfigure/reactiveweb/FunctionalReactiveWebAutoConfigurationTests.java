/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
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

import org.junit.Test;

import org.springframework.boot.context.embedded.ReactiveServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;
import org.springframework.web.server.handler.FilteringWebHandler;
import org.springframework.web.server.handler.WebHandlerDecorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author Brian Clozel
 */
public class FunctionalReactiveWebAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@Test
	public void shouldNotProcessIfExistingHttpHandler() throws Exception {
		load(CustomHttpHandler.class);

		assertThat(this.context.getBeansOfType(HttpWebHandlerAdapter.class).size()).isEqualTo(0);
	}

	@Test
	public void shouldNotProcessIfNoRouterFunction() throws Exception {
		load(BaseConfiguration.class);

		assertThat(this.context.getBeansOfType(HttpWebHandlerAdapter.class).size()).isEqualTo(0);
	}

	@Test
	public void shouldConfigureHttpHandler() {
		load(FunctionalConfig.class);

		assertThat(this.context.getBeansOfType(HttpHandler.class).size()).isEqualTo(1);
	}

	@Test
	public void shouldConfigureWebFilters() {
		load(FunctionalConfigWithWebFilters.class);

		assertThat(this.context.getBeansOfType(HttpHandler.class).size()).isEqualTo(1);
		HttpHandler handler = this.context.getBean(HttpHandler.class);
		assertThat(handler).isInstanceOf(WebHandler.class);
		WebHandler webHandler = (WebHandler) handler;
		while (webHandler instanceof WebHandlerDecorator) {
			if (webHandler instanceof FilteringWebHandler) {
				FilteringWebHandler filteringWebHandler = (FilteringWebHandler) webHandler;
				assertThat(filteringWebHandler.getFilters()).containsExactly(
						this.context.getBean("customWebFilter", WebFilter.class));
				return;
			}
			webHandler = ((WebHandlerDecorator) webHandler).getDelegate();
		}
		fail("Did not find any FilteringWebHandler");
	}


	private void load(Class<?> config, String... environment) {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context, environment);
		this.context.register(config);
		if (!config.equals(BaseConfiguration.class)) {
			this.context.register(BaseConfiguration.class);
		}
		this.context.refresh();
	}


	@Configuration
	@Import({FunctionalReactiveWebAutoConfiguration.class})
	@EnableConfigurationProperties(ReactiveServerProperties.class)
	protected static class BaseConfiguration {

	}

	@Configuration
	protected static class FunctionalConfig {

		@Bean
		public RouterFunction routerFunction() {
			return route(GET("/test"), serverRequest -> null);
		}
	}

	@Configuration
	protected static class FunctionalConfigWithWebFilters {

		@Bean
		public RouterFunction routerFunction() {
			return route(GET("/test"), serverRequest -> null);
		}

		@Bean
		public WebFilter customWebFilter() {
			return (serverWebExchange, webFilterChain) -> null;
		}
	}

	@Configuration
	protected static class CustomHttpHandler {

		@Bean
		public HttpHandler httpHandler() {
			return (serverHttpRequest, serverHttpResponse) -> null;
		}

		@Bean
		public RouterFunction routerFunction() {
			return route(GET("/test"), serverRequest -> null);
		}
	}

}
