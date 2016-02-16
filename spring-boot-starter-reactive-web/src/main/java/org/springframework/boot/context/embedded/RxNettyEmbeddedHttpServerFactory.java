package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Dave Syer
 */
public class RxNettyEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	@Override
	public ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler) {
		RxNettyEmbeddedHttpServer server = new RxNettyEmbeddedHttpServer();
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
