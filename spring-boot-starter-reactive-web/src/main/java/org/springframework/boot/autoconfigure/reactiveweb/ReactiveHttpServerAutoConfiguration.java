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

import io.reactivex.netty.protocol.http.server.HttpServerImpl;
import reactor.io.netty.http.HttpServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.embedded.JettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.boot.context.embedded.ReactorEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.RxNettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.TomcatEmbeddedHttpServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;

/**
 * @author Brian Clozel
 */
@Configuration
@ConditionalOnClass({DispatcherHandler.class, HttpHandler.class})
public class ReactiveHttpServerAutoConfiguration {

	@Configuration
	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({org.apache.catalina.startup.Tomcat.class})
	public static class TomcatAutoConfiguration {
		@Bean
		public TomcatEmbeddedHttpServerFactory tomcatEmbeddedHttpServerFactory() {
			return new TomcatEmbeddedHttpServerFactory();
		}
	}

	@Configuration
	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({org.eclipse.jetty.server.Server.class})
	@Import(TomcatAutoConfiguration.class)
	public static class JettyAutoConfiguration {
		@Bean
		public JettyEmbeddedHttpServerFactory jettyEmbeddedHttpServerFactory() {
			return new JettyEmbeddedHttpServerFactory();
		}
	}

	@Configuration
	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({HttpServer.class})
	@Import(JettyAutoConfiguration.class)
	public static class ReactorAutoConfiguration {
		@Bean
		public ReactorEmbeddedHttpServerFactory reactorEmbeddedHttpServerFactory() {
			return new ReactorEmbeddedHttpServerFactory();
		}
	}

	@Configuration
	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({HttpServerImpl.class})
	@Import(ReactorAutoConfiguration.class)
	public static class RxNettyAutoConfiguration {
		@Bean
		public RxNettyEmbeddedHttpServerFactory rxNettyEmbeddedHttpServerFactory() {
			return new RxNettyEmbeddedHttpServerFactory();
		}
	}


}
