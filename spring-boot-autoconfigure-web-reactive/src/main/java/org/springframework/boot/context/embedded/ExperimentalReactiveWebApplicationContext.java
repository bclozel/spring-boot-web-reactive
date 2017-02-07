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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.util.StringUtils;

/**
 * @author Brian Clozel
 */
public class ExperimentalReactiveWebApplicationContext extends ReactiveWebApplicationContext {

	private EmbeddedReactiveHttpServer embeddedReactiveHttpServer;

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
		startReactiveHttpServer();
	}

	@Override
	protected void onClose() {
		super.onClose();
		stopReactiveHttpServer();
	}

	protected void createReactiveHttpServer() {
		final Boolean enabled = getEnvironment().getProperty("spring.reactive.enabled", Boolean.class, true);
		if (!enabled) {
			return;
		}
		ReactiveHttpServerFactory serverFactory = getReactiveHttpServerFactory();
		HttpHandler httpHandler = getHttpHandler();
		Collection<EmbeddedReactiveHttpServerCustomizer> customizers = getReactiveHttpServerCustomizers();
		this.embeddedReactiveHttpServer = serverFactory
				.getReactiveHttpServer(httpHandler, customizers.toArray(new EmbeddedReactiveHttpServerCustomizer[0]));
		setPortProperty(this, "local.server.port", this.embeddedReactiveHttpServer.getPort());
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
					"Unable to start ExperimentalReactiveWebApplicationContext due to missing HttpHandler bean.");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException(
					"Unable to start ExperimentalReactiveWebApplicationContext due to multiple HttpHandler beans : "
							+ StringUtils.arrayToCommaDelimitedString(beanNames));
		}
		return getBeanFactory().getBean(beanNames[0], HttpHandler.class);
	}

	protected Collection<EmbeddedReactiveHttpServerCustomizer> getReactiveHttpServerCustomizers() {
		ArrayList<EmbeddedReactiveHttpServerCustomizer> customizers =
				new ArrayList<>(getBeanFactory().getBeansOfType(EmbeddedReactiveHttpServerCustomizer.class).values());
		Collections.sort(customizers, AnnotationAwareOrderComparator.INSTANCE);
		return Collections.unmodifiableList(customizers);
	}

	private EmbeddedReactiveHttpServer startReactiveHttpServer() {
		EmbeddedReactiveHttpServer localServer = this.embeddedReactiveHttpServer;
		if (localServer != null) {
			localServer.start();
		}
		return localServer;
	}

	private void stopReactiveHttpServer() {
		EmbeddedReactiveHttpServer localServer = this.embeddedReactiveHttpServer;
		if (localServer != null) {
			localServer.stop();
		}
	}

	private void setPortProperty(ApplicationContext context, String propertyName,
			int port) {
		if (context instanceof ConfigurableApplicationContext) {
			setPortProperty(((ConfigurableApplicationContext) context).getEnvironment(),
					propertyName, port);
		}
		if (context.getParent() != null) {
			setPortProperty(context.getParent(), propertyName, port);
		}
	}

	@SuppressWarnings("unchecked")
	private void setPortProperty(ConfigurableEnvironment environment, String propertyName,
			int port) {
		MutablePropertySources sources = environment.getPropertySources();
		PropertySource<?> source = sources.get("server.ports");
		if (source == null) {
			source = new MapPropertySource("server.ports", new HashMap<String, Object>());
			sources.addFirst(source);
		}
		((Map<String, Object>) source.getSource()).put(propertyName, port);
	}

}
