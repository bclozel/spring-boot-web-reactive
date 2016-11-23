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
import io.undertow.Undertow;
import reactor.ipc.netty.http.server.HttpServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.JettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.boot.context.embedded.ReactorEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.RxNettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.TomcatEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.UndertowEmbeddedHttpServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Brian Clozel
 */
abstract class ReactiveHttpServerConfiguration {

	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({org.apache.catalina.startup.Tomcat.class})
	static class TomcatAutoConfiguration {
		@Bean
		public TomcatEmbeddedHttpServerFactory tomcatEmbeddedHttpServerFactory() {
			return new TomcatEmbeddedHttpServerFactory();
		}
	}

	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({org.eclipse.jetty.server.Server.class})
	static class JettyAutoConfiguration {
		@Bean
		public JettyEmbeddedHttpServerFactory jettyEmbeddedHttpServerFactory() {
			return new JettyEmbeddedHttpServerFactory();
		}
	}

	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({HttpServer.class})
	static class ReactorNettyAutoConfiguration {
		@Bean
		public ReactorEmbeddedHttpServerFactory reactorEmbeddedHttpServerFactory() {
			return new ReactorEmbeddedHttpServerFactory();
		}
	}

	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({HttpServerImpl.class})
	static class RxNettyAutoConfiguration {
		@Bean
		public RxNettyEmbeddedHttpServerFactory rxNettyEmbeddedHttpServerFactory() {
			return new RxNettyEmbeddedHttpServerFactory();
		}
	}

	@ConditionalOnMissingBean(ReactiveHttpServerFactory.class)
	@ConditionalOnClass({Undertow.class})
	static class UndertowAutoConfiguration {
		@Bean
		public UndertowEmbeddedHttpServerFactory undertowEmbeddedHttpServerFactory() {
			return new UndertowEmbeddedHttpServerFactory();
		}
	}

}
