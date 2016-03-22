package org.springframework.boot.context.embedded;

import org.springframework.core.io.buffer.DataBufferAllocator;
import org.springframework.http.server.reactive.HttpHandler;

public class ReactorEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	private final DataBufferAllocator dataBufferAllocator;

	public ReactorEmbeddedHttpServerFactory(DataBufferAllocator dataBufferAllocator) {
		this.dataBufferAllocator = dataBufferAllocator;
	}

	@Override
	public ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler) {
		ReactorEmbeddedHttpServer server = new ReactorEmbeddedHttpServer();
		server.setAllocator(dataBufferAllocator);
		server.setHandler(httpHandler);
		server.setPort(8080);
		server.setAllocator(this.dataBufferAllocator);
		try {
			server.afterPropertiesSet();
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return server;
	}
}
