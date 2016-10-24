package org.springframework.boot.context.embedded;

import java.net.InetAddress;

import org.springframework.http.server.reactive.HttpHandler;

/**
 * @author Brian Clozel
 */
public interface ConfigurableEmbeddedReactiveHttpServer {

	void setAddress(InetAddress address);

	void setPort(int port);

	void setHandler(HttpHandler handler);

	void setRequestTimeout(Long requestTimeout);
}
