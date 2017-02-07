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
package org.springframework.boot.autoconfigure.reactiveweb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * @author Brian Clozel
 */
public class ReactiveContextPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final Map<String, Object> PROPERTIES;

	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 9;

	static {
		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.main.applicationContextClass",
				"org.springframework.boot.context.embedded.ExperimentalReactiveWebApplicationContext");
		PROPERTIES = Collections.unmodifiableMap(properties);
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
		configurableEnvironment.getPropertySources().addLast(new MapPropertySource("reactive", PROPERTIES));
	}
}
