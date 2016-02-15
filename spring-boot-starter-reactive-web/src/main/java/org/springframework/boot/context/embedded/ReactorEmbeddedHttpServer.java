package org.springframework.boot.context.embedded;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.server.reactive.boot.ReactorHttpServer;

public class ReactorEmbeddedHttpServer extends ReactorHttpServer implements ReactiveEmbeddedHttpServer {

	@Override
	public void start() {
		super.start();
		CompletableFuture<Void> stop = new CompletableFuture<>();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stop.complete(null);
		}));
		synchronized (stop) {
			try {
				stop.wait();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
