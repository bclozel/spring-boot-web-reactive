package org.springframework.boot.context.embedded;

import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Brian Clozel
 */
public interface ReactiveHttpServerFactory {

	ReactiveEmbeddedHttpServer getReactiveHttpServer(HttpHandler httpHandler);
}
