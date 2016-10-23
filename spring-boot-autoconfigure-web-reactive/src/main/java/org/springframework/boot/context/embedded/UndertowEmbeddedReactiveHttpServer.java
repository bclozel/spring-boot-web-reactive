package org.springframework.boot.context.embedded;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.http.server.reactive.UndertowHttpHandlerAdapter;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.xnio.Options;
import org.xnio.Sequence;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.*;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Brian Clozel
 */
public class UndertowEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer
		implements EmbeddedReactiveHttpServer {

	private static final Log logger = LogFactory.getLog(UndertowEmbeddedReactiveHttpServer.class);

	private final String DEFAULT_HOST = "0.0.0.0";

	private Undertow server;

	private boolean running;

	private List<UndertowBuilderCustomizer> builderCustomizers = new ArrayList();

	public void addBuilderCustomizers(UndertowBuilderCustomizer... customizers) {
		Assert.notNull(customizers, "Customizers must not be null");
		this.builderCustomizers.addAll(Arrays.asList(customizers));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		HttpHandler handler = new UndertowHttpHandlerAdapter(getHttpHandler());

		Undertow.Builder builder = Undertow.builder();

		for(UndertowBuilderCustomizer customizer : this.builderCustomizers) {
			customizer.customize(builder);
		}
		if(this.getSsl() != null && this.getSsl().isEnabled()) {
			this.configureSsl(this.getSsl(), builder);
		} else {
			builder.addHttpListener(getPort(), determineHost());
		}

		this.server = builder.setHandler(handler).build();
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
			logger.info("Undertow started on port(s) " + this.getPort());
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

	private void configureSsl(Ssl ssl, Undertow.Builder builder) {
		try {
            SSLContext ex = SSLContext.getInstance(ssl.getProtocol());
			ex.init(this.getKeyManagers(), this.getTrustManagers(), (SecureRandom)null);
            builder.addHttpsListener(getPort(), determineHost(), ex);
			builder.setSocketOption(Options.SSL_CLIENT_AUTH_MODE, this.getSslClientAuthMode(ssl));
			if(ssl.getEnabledProtocols() != null) {
				builder.setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of(ssl.getEnabledProtocols()));
			}

			if(ssl.getCiphers() != null) {
				builder.setSocketOption(Options.SSL_ENABLED_CIPHER_SUITES, Sequence.of(ssl.getCiphers()));
			}

		} catch (NoSuchAlgorithmException var5) {
			throw new IllegalStateException(var5);
		} catch (KeyManagementException var6) {
			throw new IllegalStateException(var6);
		}
	}

	private SslClientAuthMode getSslClientAuthMode(Ssl ssl) {
		return ssl.getClientAuth() == Ssl.ClientAuth.NEED?SslClientAuthMode.REQUIRED:(ssl.getClientAuth() == Ssl.ClientAuth.WANT?SslClientAuthMode.REQUESTED:SslClientAuthMode.NOT_REQUESTED);
	}

	private TrustManager[] getTrustManagers() {
		try {
			KeyStore ex = this.getTrustStore();
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(ex);
			return trustManagerFactory.getTrustManagers();
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

	private KeyManager[] getKeyManagers() {
		try {
			KeyStore ex = this.getKeyStore();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			Ssl ssl = this.getSsl();
			char[] keyPassword = ssl.getKeyPassword() != null?ssl.getKeyPassword().toCharArray():null;
			if(keyPassword == null && ssl.getKeyStorePassword() != null) {
				keyPassword = ssl.getKeyStorePassword().toCharArray();
			}

			keyManagerFactory.init(ex, keyPassword);
			return keyManagerFactory.getKeyManagers();
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
