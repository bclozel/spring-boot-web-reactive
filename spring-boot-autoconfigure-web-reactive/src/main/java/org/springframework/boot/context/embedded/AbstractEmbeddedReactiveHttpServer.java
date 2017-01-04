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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.SocketUtils;

public abstract class AbstractEmbeddedReactiveHttpServer
		implements ConfigurableEmbeddedReactiveHttpServer, EmbeddedReactiveHttpServer {

	private InetAddress address;

	private int port = -1;

	private long requestTimeout;

	private HttpHandler httpHandler;

	private Map<String, HttpHandler> handlerMap;

	@Override
	public InetAddress getAddress() {
		if (this.address == null) {
			try {
				return InetAddress.getByAddress(new byte[] {0, 0, 0, 0});
			}
			catch (UnknownHostException e) { }
		}
		return this.address;
	}

	@Override
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@Override
	public int getPort() {
		if (this.port == -1) {
			this.port = 8080;
		}
		if (this.port == 0) {
			this.port = SocketUtils.findAvailableTcpPort(8080);
		}
		return this.port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	public long getRequestTimeout() {
		return requestTimeout;
	}

	@Override
	public void setRequestTimeout(Long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public void setHandler(HttpHandler handler) {
		this.httpHandler = handler;
	}

	public HttpHandler getHttpHandler() {
		return this.httpHandler;
	}

	@Override
	public void registerHttpHandler(String contextPath, HttpHandler handler) {
		if (this.handlerMap == null) {
			this.handlerMap = new LinkedHashMap<>();
		}
		this.handlerMap.put(contextPath, handler);
	}

	public Map<String, HttpHandler> getHttpHandlerMap() {
		return this.handlerMap;
	}

	/**
	 * Returns the absolute temp dir for given servlet container.
	 * @param prefix servlet container name
	 * @return The temp dir for given servlet container.
	 */
	protected File createTempDir(String prefix) {
		try {
			File tempDir = File.createTempFile(prefix + ".", "." + getPort());
			tempDir.delete();
			tempDir.mkdir();
			tempDir.deleteOnExit();
			return tempDir;
		}
		catch (IOException ex) {
			throw new EmbeddedServletContainerException(
					"Unable to create tempDir. java.io.tmpdir is set to "
							+ System.getProperty("java.io.tmpdir"),
					ex);
		}
	}

}
