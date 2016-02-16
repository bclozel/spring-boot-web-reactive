package org.springframework.boot.autoconfigure.reactiveweb;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.JettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.ReactorEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.RxNettyEmbeddedHttpServerFactory;
import org.springframework.boot.context.embedded.TomcatEmbeddedHttpServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferAllocator;
import org.springframework.core.io.buffer.NettyDataBufferAllocator;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.reactivex.netty.protocol.http.server.HttpServerImpl;
import reactor.io.net.http.HttpServer;

/**
 * @author Brian Clozel
 */
@Configuration
@ConditionalOnClass({ DispatcherHandler.class, HttpHandler.class })
public class ReactiveHttpServerAutoConfiguration {

	public static final String RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME = "runtimeDataBufferAllocator";

	@Configuration
	@ConditionalOnClass({ org.apache.catalina.startup.Tomcat.class })
	public static class TomcatAutoConfiguration {
		@Bean
		public TomcatEmbeddedHttpServerFactory tomcatEmbeddedHttpServerFactory() {
			return new TomcatEmbeddedHttpServerFactory();
		}

		// TODO: databuffer should be configurable via properties
		@Bean(name = RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME)
		public DefaultDataBufferAllocator defaultDataBufferAllocator() {
			return new DefaultDataBufferAllocator();
		}
	}

	@Configuration
	@ConditionalOnClass({ HttpServer.class })
	public static class ReactorAutoConfiguration {
		@Bean
		public ReactorEmbeddedHttpServerFactory reactorEmbeddedHttpServerFactory() {
			return new ReactorEmbeddedHttpServerFactory(nettyDataBufferAllocator());
		}

		// TODO: databuffer should be configurable via properties
		@Bean(name = RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME)
		public NettyDataBufferAllocator nettyDataBufferAllocator() {
			return new NettyDataBufferAllocator(UnpooledByteBufAllocator.DEFAULT);
		}
	}

	@Configuration
	@ConditionalOnClass({ HttpServerImpl.class })
	public static class RxNettyAutoConfiguration {

		@Bean
		public RxNettyEmbeddedHttpServerFactory rxNettyEmbeddedHttpServerFactory() {
			return new RxNettyEmbeddedHttpServerFactory();
		}

		@Bean(name = RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME)
		public DefaultDataBufferAllocator nettyDataBufferAllocator() {
			return new DefaultDataBufferAllocator();
		}

	}

	@Configuration
	@ConditionalOnClass({ org.eclipse.jetty.server.Server.class })
	public static class JettyAutoConfiguration {

		@Bean
		public JettyEmbeddedHttpServerFactory jettyEmbeddedHttpServerFactory() {
			return new JettyEmbeddedHttpServerFactory();
		}

		// TODO: databuffer should be configurable via properties
		@Bean(name = RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME)
		public DefaultDataBufferAllocator defaultDataBufferAllocator() {
			return new DefaultDataBufferAllocator();
		}

	}

}
