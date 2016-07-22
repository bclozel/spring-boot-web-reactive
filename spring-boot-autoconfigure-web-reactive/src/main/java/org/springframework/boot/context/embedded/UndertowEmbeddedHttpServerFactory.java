package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Brian Clozel
 */
public class UndertowEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	@Override
	public EmbeddedReactiveHttpServer getReactiveHttpServer(HttpHandler httpHandler,
			EmbeddedReactiveHttpServerCustomizer... customizers) {
		UndertowEmbeddedReactiveHttpServer server = new UndertowEmbeddedReactiveHttpServer();
		server.setHandler(httpHandler);
		for (EmbeddedReactiveHttpServerCustomizer customizer : customizers) {
			customizer.customize(server);
		}
		try {
			server.afterPropertiesSet();
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return server;
	}
}
