/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.embedded;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import reactor.ipc.netty.NettyContext;

import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;

public class ReactorEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer implements EmbeddedReactiveHttpServer {

	private static CountDownLatch latch = new CountDownLatch(1);

	private AtomicReference<NettyContext> nettyContext = new AtomicReference<>();

	private ReactorHttpHandlerAdapter reactorHandler;

	private reactor.ipc.netty.http.server.HttpServer reactorServer;


	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getHttpHandler());
		this.reactorHandler = new ReactorHttpHandlerAdapter(getHttpHandler());
		if (getAddress() != null) {
			this.reactorServer = reactor.ipc.netty.http.server.HttpServer.create(getAddress().getHostAddress(), getPort());
		}
		else {
			this.reactorServer = reactor.ipc.netty.http.server.HttpServer.create(getPort());
		}
	}

	@Override
	public void start() {
		if (this.nettyContext.get() == null) {
			this.nettyContext.set(this.reactorServer.newHandler(reactorHandler).block());
			startDaemonAwaitThread();
		}
	}

	@Override
	public void stop() {
		NettyContext context = this.nettyContext.getAndSet(null);
		if (context != null) {
			context.dispose();
		}
		latch.countDown();
	}

	@Override
	public boolean isRunning() {
		NettyContext context = this.nettyContext.get();
		return context != null && context.channel().isActive();
	}

	private void startDaemonAwaitThread() {
		Thread awaitThread = new Thread("server") {
			@Override
			public void run() {
				try {
					ReactorEmbeddedReactiveHttpServer.latch.await();
				}
				catch (InterruptedException e) { }
			}
		};
		awaitThread.setContextClassLoader(getClass().getClassLoader());
		awaitThread.setDaemon(false);
		awaitThread.start();
	}
}
