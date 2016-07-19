package org.springframework.boot.context.embedded;

import java.net.InetAddress;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Brian Clozel
 */

@ConfigurationProperties(prefix = "server")
public class ReactiveServerProperties implements EmbeddedReactiveHttpServerCustomizer {

	private Integer port;

	private InetAddress address;

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

	@Override
	public void customize(ConfigurableEmbeddedReactiveHttpServer server) {
		if(getPort() != null) {
			server.setPort(getPort());
		}
		if(getAddress() != null) {
			server.setAddress(getAddress());
		}
	}
}
