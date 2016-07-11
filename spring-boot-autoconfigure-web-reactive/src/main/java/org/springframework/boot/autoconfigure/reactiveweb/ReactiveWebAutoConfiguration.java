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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.ResponseStatusExceptionHandler;
import org.springframework.web.reactive.config.WebReactiveConfiguration;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * @author Brian Clozel
 */
@Configuration
@ConditionalOnClass({ DispatcherHandler.class, HttpHandler.class })
@AutoConfigureAfter({ ReactiveHttpServerAutoConfiguration.class })
public class ReactiveWebAutoConfiguration {

	@Configuration
	public static class WebReactiveConfig extends WebReactiveConfiguration {

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

}
