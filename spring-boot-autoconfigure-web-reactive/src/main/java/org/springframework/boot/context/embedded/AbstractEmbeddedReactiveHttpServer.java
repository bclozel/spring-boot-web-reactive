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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.SocketUtils;

public abstract class AbstractEmbeddedReactiveHttpServer
		implements ConfigurableEmbeddedReactiveHttpServer, EmbeddedReactiveHttpServer {

	private InetAddress address;

	private int port = -1;

    private long requestTimeout;

	private Ssl ssl;

	private SslStoreProvider sslStoreProvider;

	private HttpHandler httpHandler;

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
	public Ssl getSsl() {
		return ssl;
	}

	@Override
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	public SslStoreProvider getSslStoreProvider() {
		return sslStoreProvider;
	}

	@Override
	public void setSslStoreProvider(SslStoreProvider sslStoreProvider) {
		this.sslStoreProvider = sslStoreProvider;
	}

    @Override
    public void setHandler(HttpHandler handler) {
        this.httpHandler = handler;
    }

	public HttpHandler getHttpHandler() {
		return this.httpHandler;
	}

}
