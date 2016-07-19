package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

public class TomcatEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	@Override
	public EmbeddedReactiveHttpServer getReactiveHttpServer(HttpHandler httpHandler,
			EmbeddedReactiveHttpServerCustomizer... customizers) {
		TomcatEmbeddedReactiveHttpHttpServer server = new TomcatEmbeddedReactiveHttpHttpServer();
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
