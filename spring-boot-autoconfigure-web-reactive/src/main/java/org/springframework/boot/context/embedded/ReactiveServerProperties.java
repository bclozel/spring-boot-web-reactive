package org.springframework.boot.context.embedded;

import java.net.InetAddress;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Brian Clozel
 */

@ConfigurationProperties(prefix = "server")
public class ReactiveServerProperties implements EmbeddedReactiveHttpServerCustomizer {

	private Integer port;

	private InetAddress address;

	@NestedConfigurationProperty
	private Ssl ssl;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public Ssl getSsl() {
		return ssl;
	}

	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	@Override
	public void customize(ConfigurableEmbeddedReactiveHttpServer server) {
		if(getPort() != null) {
			server.setPort(getPort());
		}
		if(getAddress() != null) {
			server.setAddress(getAddress());
		}
		if(getSsl() != null) {
			server.setSsl(this.getSsl());
		}
	}
}
