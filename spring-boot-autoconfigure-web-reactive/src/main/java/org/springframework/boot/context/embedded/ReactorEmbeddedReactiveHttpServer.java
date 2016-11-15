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

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import reactor.ipc.netty.config.ServerOptions;
import reactor.ipc.netty.http.HttpServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReactorEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer implements EmbeddedReactiveHttpServer {

	private boolean running;

	private ReactorHttpHandlerAdapter reactorHandler;

	private HttpServer reactorServer;


	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getHttpHandler());
		this.reactorHandler = new ReactorHttpHandlerAdapter(getHttpHandler());
		ServerOptions reactorServerOptions = ServerOptions.create();
		if(this.getSsl() != null && this.getSsl().isEnabled()) {
			configureSsl(this.getSsl(), reactorServerOptions);
		}
		if (getAddress() != null) {
			reactorServerOptions.listen(getAddress().getHostAddress(), getPort());
		} else {
			reactorServerOptions.listen(getPort());
		}
		this.reactorServer = HttpServer.create(reactorServerOptions);
	}

	@Override
	public void start() {
		if (!this.running) {
			try {
				this.reactorServer.startAndAwait(reactorHandler);
				this.running = true;
			}
			catch (InterruptedException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	public void stop() {
		if (this.running) {
			this.reactorServer.shutdown();
			this.running = false;
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	private void configureSsl(Ssl ssl, ServerOptions reactorServerOptions) throws CertificateException {
		Set<String> ciphers = new HashSet<>(Http2SecurityUtil.CIPHERS);
		if (null != ssl.getCiphers()) {
			ciphers.addAll(Arrays.asList(ssl.getCiphers()));
		}
		SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;

		SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(getKeyManagerFactory())
				.trustManager(getTrustManagerFactory())
				.sslProvider(provider)
				.ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
				.applicationProtocolConfig(new ApplicationProtocolConfig(
						protocol(ssl.getProtocol()),
						// NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
						ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
						// ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
						ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
						ApplicationProtocolNames.HTTP_2,
						ApplicationProtocolNames.HTTP_1_1))
				.clientAuth(getSslClientAuth(ssl));

		reactorServerOptions.ssl(sslContextBuilder);
	}

	private ClientAuth getSslClientAuth(Ssl ssl) {
		return ssl.getClientAuth() == Ssl.ClientAuth.NEED?ClientAuth.REQUIRE:(ssl.getClientAuth() == Ssl.ClientAuth.WANT?ClientAuth.OPTIONAL:ClientAuth.NONE);
	}

	private ApplicationProtocolConfig.Protocol protocol(String protocol) {
		if ("NPN".equals(protocol)) {
			return ApplicationProtocolConfig.Protocol.NPN;
		} else if ("NPN_AND_ALPN".equals(protocol)) {
			return ApplicationProtocolConfig.Protocol.NPN_AND_ALPN;
		}
		return ApplicationProtocolConfig.Protocol.ALPN;
	}

	private TrustManagerFactory getTrustManagerFactory() {
		try {
			KeyStore ex = this.getTrustStore();
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
					TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(ex);
			return trustManagerFactory;
		} catch (Exception var3) {
			throw new IllegalStateException(var3);
		}
	}

	private KeyStore getTrustStore() throws Exception {
		if(this.getSslStoreProvider() != null) {
			return this.getSslStoreProvider().getTrustStore();
		} else {
			Ssl ssl = this.getSsl();
			return this.loadKeyStore(ssl.getTrustStoreType(), ssl.getTrustStore(), ssl.getTrustStorePassword());
		}
	}

	private KeyManagerFactory getKeyManagerFactory() {
		try {
			KeyStore ex = this.getKeyStore();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			Ssl ssl = this.getSsl();
			char[] keyPassword = ssl.getKeyPassword() != null?ssl.getKeyPassword().toCharArray():null;
			if(keyPassword == null && ssl.getKeyStorePassword() != null) {
				keyPassword = ssl.getKeyStorePassword().toCharArray();
			}

			keyManagerFactory.init(ex, keyPassword);
			return keyManagerFactory;
		} catch (Exception var5) {
			throw new IllegalStateException(var5);
		}
	}

	private KeyStore getKeyStore() throws Exception {
		if(this.getSslStoreProvider() != null) {
			return this.getSslStoreProvider().getKeyStore();
		} else {
			Ssl ssl = this.getSsl();
			return this.loadKeyStore(ssl.getKeyStoreType(), ssl.getKeyStore(), ssl.getKeyStorePassword());
		}
	}

	private KeyStore loadKeyStore(String type, String resource, String password) throws Exception {
		type = type == null?"JKS":type;
		if(resource == null) {
			return null;
		} else {
			KeyStore store = KeyStore.getInstance(type);
			URL url = ResourceUtils.getURL(resource);
			store.load(url.openStream(), password == null?null:password.toCharArray());
			return store;
		}
	}
}
