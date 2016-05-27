package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

public class ReactorEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {


	@Override
	public ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler) {
		ReactorEmbeddedHttpServer server = new ReactorEmbeddedHttpServer();
		server.setHandler(httpHandler);
		server.setPort(8080);
		try {
			server.afterPropertiesSet();
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return server;
	}
}
