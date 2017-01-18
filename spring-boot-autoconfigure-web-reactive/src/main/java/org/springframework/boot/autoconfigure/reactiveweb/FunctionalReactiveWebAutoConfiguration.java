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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.session.WebSessionManager;

import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

/**
 * @author Brian Clozel
 */
@Configuration
@ConditionalOnClass({DispatcherHandler.class, HttpHandler.class})
@ConditionalOnBean(RouterFunction.class)
@ConditionalOnMissingBean(HttpHandler.class)
@AutoConfigureAfter({ReactiveHttpServerAutoConfiguration.class})
public class FunctionalReactiveWebAutoConfiguration {

	@Configuration
	public static class FunctionalWebReactiveConfig {

		private final List<WebFilter> webFilters;

		private final WebSessionManager webSessionManager;

		private final List<HttpMessageReader> messageReaders;

		private final List<HttpMessageWriter> messageWriters;

		private final List<ViewResolver> viewResolvers;

		public FunctionalWebReactiveConfig(ObjectProvider<List<WebFilter>> webFilters,
				ObjectProvider<WebSessionManager> webSessionManager,
				ObjectProvider<List<HttpMessageReader>> messageReaders,
				ObjectProvider<List<HttpMessageWriter>> messageWriters,
				ObjectProvider<List<ViewResolver>> viewResolvers) {
			this.webFilters = webFilters.getIfAvailable();
			if (this.webFilters != null) {
				AnnotationAwareOrderComparator.sort(this.webFilters);
			}
			this.webSessionManager = webSessionManager.getIfAvailable();
			this.messageReaders = messageReaders.getIfAvailable();
			this.messageWriters = messageWriters.getIfAvailable();
			this.viewResolvers = viewResolvers.getIfAvailable();
		}

		@Bean
		public HttpHandler httpHandler(List<RouterFunction> routerFunctions) {
			Collections.sort(routerFunctions, new AnnotationAwareOrderComparator());
			RouterFunction routerFunction = routerFunctions.stream().reduce(RouterFunction::and).get();
			HandlerStrategies.Builder strategiesBuilder = HandlerStrategies.builder();
			if (this.messageReaders != null) {
				this.messageReaders.forEach(reader -> strategiesBuilder.messageReader(reader));
			}
			if (this.messageWriters != null) {
				this.messageWriters.forEach(writer -> strategiesBuilder.messageWriter(writer));
			}
			if (this.viewResolvers != null) {
				this.viewResolvers.forEach(viewResolver -> strategiesBuilder.viewResolver(viewResolver));
			}
			WebHandler webHandler = toHttpHandler(routerFunction, strategiesBuilder.build());
			WebHttpHandlerBuilder builder = WebHttpHandlerBuilder
					.webHandler(webHandler)
					.sessionManager(this.webSessionManager);
			if (this.webFilters != null) {
				builder.filters(this.webFilters.toArray(new WebFilter[this.webFilters.size()]));
			}
			return builder.build();
		}
	}
}
