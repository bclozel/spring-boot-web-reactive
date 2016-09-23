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


import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.junit.Test;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

public class ReactiveContextPostProcessorTests {

	@Test
	public void shouldChangeApplicationContextClass() throws Exception {
		Properties properties = new Properties();
		properties.put("spring.main.applicationContextClass", "test");
		ConfigurableEnvironment environment = new MockEnvironment();
		ReactiveContextPostProcessor postProcessor = new ReactiveContextPostProcessor();
		postProcessor.postProcessEnvironment(environment, null);
		assertThat(environment.getProperty("spring.main.applicationContextClass"))
				.isEqualTo("org.springframework.boot.context.embedded.ReactiveWebApplicationContext");
	}

}
