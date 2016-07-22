package org.springframework.boot.context.embedded;

import java.net.InetAddress;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.UndertowHttpHandlerAdapter;

/**
 * @author Brian Clozel
 */
public class UndertowEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer
		implements EmbeddedReactiveHttpServer {

	private final String DEFAULT_HOST = "0.0.0.0";

	private Undertow server;

	private DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

	private boolean running;

	@Override
	public void afterPropertiesSet() throws Exception {

		HttpHandler handler = new UndertowHttpHandlerAdapter(getHttpHandler(), dataBufferFactory);
		this.server = Undertow.builder()
				.addHttpListener(getPort(), determineHost()).setHandler(handler).build();
	}

	private String determineHost() {
		InetAddress address = getAddress();
		if (address == null) {
			return DEFAULT_HOST;
		}
		else {
			return address.getHostAddress();
		}
	}

	@Override
	public void start() {
		if (!this.running) {
			this.server.start();
			this.running = true;
		}

	}

	@Override
	public void stop() {
		if (this.running) {
			this.server.stop();
			this.running = false;
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}
}
