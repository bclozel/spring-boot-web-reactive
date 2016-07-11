package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

public class TomcatEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	@Override
	public ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler) {
		TomcatEmbeddedHttpServer server = new TomcatEmbeddedHttpServer();
		server.setHandler(httpHandler);
		server.setPort(8080);
		try {
			server.afterPropertiesSet();
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
		return server;
	}
}
