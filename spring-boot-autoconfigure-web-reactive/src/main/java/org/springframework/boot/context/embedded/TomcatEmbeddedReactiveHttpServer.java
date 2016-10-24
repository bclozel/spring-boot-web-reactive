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

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.util.Assert;

public class TomcatEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer
		implements EmbeddedReactiveHttpServer {

	private static AtomicInteger containerCounter = new AtomicInteger(-1);

	private Tomcat tomcatServer;

	private boolean running;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.tomcatServer = new Tomcat();
		if (getAddress() != null) {
			this.tomcatServer.setHostname(getAddress().getHostAddress());
		}
		this.tomcatServer.setPort(getPort());
		this.tomcatServer.getConnector().setAsyncTimeout(getRequestTimeout());

		Assert.notNull(getHttpHandler());
		ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(getHttpHandler());

		File base = new File(System.getProperty("java.io.tmpdir"));
		Context rootContext = tomcatServer.addContext("", base.getAbsolutePath());
		Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);
		rootContext.addServletMappingDecoded("/", "httpHandlerServlet");
	}

	@Override
	public void start() {
		if (!this.running) {
			try {
				this.running = true;
				this.tomcatServer.start();
				startDaemonAwaitThread();
			}
			catch (LifecycleException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	public void stop() {
		if (this.running) {
			try {
				this.running = false;
				this.tomcatServer.stop();
				this.tomcatServer.destroy();
			}
			catch (LifecycleException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	private void startDaemonAwaitThread() {
		Thread awaitThread = new Thread("container-" + (containerCounter.get())) {

			@Override
			public void run() {
				TomcatEmbeddedReactiveHttpServer.this.tomcatServer.getServer().await();
			}

		};
		awaitThread.setContextClassLoader(getClass().getClassLoader());
		awaitThread.setDaemon(false);
		awaitThread.start();
	}
}