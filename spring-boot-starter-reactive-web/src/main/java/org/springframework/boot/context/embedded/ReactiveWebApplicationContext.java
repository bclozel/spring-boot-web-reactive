package org.springframework.boot.context.embedded;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.StringUtils;

/**
 * @author Brian Clozel
 */
public class ReactiveWebApplicationContext extends AnnotationConfigApplicationContext {

	private ReactiveEmbeddedHttpServer reactiveEmbeddedHttpServer;

	@Override
	protected void onRefresh() throws BeansException {
		super.onRefresh();
		try {
			createReactiveHttpServer();
		}
		catch (Throwable ex) {
			throw new ApplicationContextException("Unable to start embedded container", ex);
		}

	}

	@Override
	protected void finishRefresh() {
		super.finishRefresh();
		ReactiveEmbeddedHttpServer localHttpServer = startReactiveHttpServer();
	}

	@Override
	protected void onClose() {
		super.onClose();
		stopReactiveHttpServer();
	}

	protected void createReactiveHttpServer() {
		ReactiveHttpServerFactory serverFactory = getReactiveHttpServerFactory();
		HttpHandler httpHandler = getHttpHandler();
		this.reactiveEmbeddedHttpServer = serverFactory.getReactiveHttpServer(httpHandler);
		initPropertySources();
	}

	protected ReactiveHttpServerFactory getReactiveHttpServerFactory() {
		// Use bean names so that we don't consider the hierarchy
		String[] beanNames = getBeanFactory()
				.getBeanNamesForType(ReactiveHttpServerFactory.class);
		if (beanNames.length == 0) {
			throw new ApplicationContextException(
					"Unable to start ReactiveWebApplicationContext due to missing "
							+ "ReactiveHttpServerFactory bean.");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException(
					"Unable to start ReactiveWebApplicationContext due to multiple "
							+ "ReactiveHttpServerFactory beans : "
							+ StringUtils.arrayToCommaDelimitedString(beanNames));
		}
		return getBeanFactory().getBean(beanNames[0], ReactiveHttpServerFactory.class);
	}

	protected HttpHandler getHttpHandler() {
		// Use bean names so that we don't consider the hierarchy
		String[] beanNames = getBeanFactory()
				.getBeanNamesForType(HttpHandler.class);
		if (beanNames.length == 0) {
			throw new ApplicationContextException(
					"Unable to start ReactiveWebApplicationContext due to missing HttpHandler bean.");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException(
					"Unable to start ReactiveWebApplicationContext due to multiple HttpHandler beans : "
							+ StringUtils.arrayToCommaDelimitedString(beanNames));
		}
		return getBeanFactory().getBean(beanNames[0], HttpHandler.class);
	}

	private ReactiveEmbeddedHttpServer startReactiveHttpServer() {
		ReactiveEmbeddedHttpServer localServer = this.reactiveEmbeddedHttpServer;
		if (localServer != null) {
			localServer.start();
		}
		return localServer;
	}

	private void stopReactiveHttpServer() {
		ReactiveEmbeddedHttpServer localServer = this.reactiveEmbeddedHttpServer;
		if (localServer != null) {
			localServer.stop();
		}
	}

}
