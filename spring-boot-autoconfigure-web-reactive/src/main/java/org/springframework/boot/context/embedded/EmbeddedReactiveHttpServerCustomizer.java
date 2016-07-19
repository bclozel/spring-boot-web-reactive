package org.springframework.boot.context.embedded;

/**
 * @author Brian Clozel
 */
public interface EmbeddedReactiveHttpServerCustomizer {

	void customize(ConfigurableEmbeddedReactiveHttpServer server);
}
