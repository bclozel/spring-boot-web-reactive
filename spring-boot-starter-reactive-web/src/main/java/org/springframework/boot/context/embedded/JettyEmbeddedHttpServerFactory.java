package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Brian Clozel
 */
public class JettyEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {
	@Override
	public ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler) {
		JettyEmbeddedHttpServer server = new JettyEmbeddedHttpServer();
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
