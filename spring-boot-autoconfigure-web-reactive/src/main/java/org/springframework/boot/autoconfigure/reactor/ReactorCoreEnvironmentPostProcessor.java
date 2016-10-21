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

package org.springframework.boot.autoconfigure.reactor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * {@link EnvironmentPostProcessor} to add properties that make sense when working at
 * development time.
 * <p>This should be eventually merged in {@code DevToolsPropertyDefaultsPostProcessor}.
 * @author Brian Clozel
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class ReactorCoreEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final Map<String, Object> PROPERTIES;

	static {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("spring.reactor.stacktrace-mode.enabled", "true");
		PROPERTIES = Collections.unmodifiableMap(properties);
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
			PropertySource<?> propertySource = new MapPropertySource("reactive",
					PROPERTIES);
			environment.getPropertySources().addLast(propertySource);
	}

}
