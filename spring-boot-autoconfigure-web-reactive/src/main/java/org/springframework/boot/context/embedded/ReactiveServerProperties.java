package org.springframework.boot.context.embedded;

import java.net.InetAddress;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Brian Clozel
 */

@ConfigurationProperties(prefix = "server")
public class ReactiveServerProperties implements EmbeddedReactiveHttpServerCustomizer {

	/**
	 * Server HTTP port.
	 */
	private Integer port;

	/**
	 * Network address to which the server should bind to.
	 */
	private InetAddress address;

	/**
	 * Amount of time (in milliseconds) before asynchronous request handling times
	 * out. Overrides any default value configured by the container
	 * (e.g. 30 seconds on Tomcat with Servlet 3.1) but does nothing if the
	 * web server does not enforce any default value.
	 */
	// TODO: Improve this, as we should deal with this consistently
	// For now, only using this with Tomtcat and Jetty
	private Long requestTimeout = 1800000L;

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

	public Long getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public void customize(ConfigurableEmbeddedReactiveHttpServer server) {
		if(getPort() != null) {
			server.setPort(getPort());
		}
		if(getAddress() != null) {
			server.setAddress(getAddress());
		}
		if(getRequestTimeout() != null) {
			server.setRequestTimeout(getRequestTimeout());
		}
	}
}
