package org.springframework.boot.autoconfigure.reactiveweb;

import io.reactivex.netty.protocol.http.server.HttpServerImpl;
import reactor.io.netty.http.HttpServer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.JettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.boot.context.embedded.ReactorEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.RxNettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.TomcatEmbeddedHttpServerFactory;
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
	static class ReactorAutoConfiguration {
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

}
