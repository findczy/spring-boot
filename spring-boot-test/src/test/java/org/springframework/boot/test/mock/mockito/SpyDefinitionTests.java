/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.mock.mockito;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.mockito.internal.util.MockUtil;
import org.mockito.mock.MockCreationSettings;

import org.springframework.boot.test.mock.mockito.example.ExampleService;
import org.springframework.boot.test.mock.mockito.example.ExampleServiceCaller;
import org.springframework.boot.test.mock.mockito.example.RealExampleService;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpyDefinition}.
 *
 * @author Phillip Webb
 */
public class SpyDefinitionTests {

	private static final ResolvableType REAL_SERVICE_TYPE = ResolvableType
			.forClass(RealExampleService.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void classToSpyMustNotBeNull() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("TypeToSpy must not be null");
		new SpyDefinition(null, null, null, true);
	}

	@Test
	public void createWithDefaults() throws Exception {
		SpyDefinition definition = new SpyDefinition(null, REAL_SERVICE_TYPE, null, true);
		assertThat(definition.getName()).isNull();
		assertThat(definition.getTypeToSpy()).isEqualTo(REAL_SERVICE_TYPE);
		assertThat(definition.getReset()).isEqualTo(MockReset.AFTER);
		assertThat(definition.isProxyTargetAware()).isTrue();
	}

	@Test
	public void createExplicit() throws Exception {
		SpyDefinition definition = new SpyDefinition("name", REAL_SERVICE_TYPE,
				MockReset.BEFORE, false);
		assertThat(definition.getName()).isEqualTo("name");
		assertThat(definition.getTypeToSpy()).isEqualTo(REAL_SERVICE_TYPE);
		assertThat(definition.getReset()).isEqualTo(MockReset.BEFORE);
		assertThat(definition.isProxyTargetAware()).isFalse();
	}

	@Test
	public void createSpy() throws Exception {
		SpyDefinition definition = new SpyDefinition("name", REAL_SERVICE_TYPE,
				MockReset.BEFORE, true);
		RealExampleService spy = definition.createSpy(new RealExampleService("hello"));
		MockCreationSettings<?> settings = new MockUtil().getMockSettings(spy);
		assertThat(spy).isInstanceOf(ExampleService.class);
		assertThat(settings.getMockName().toString()).isEqualTo("name");
		assertThat(settings.getDefaultAnswer())
				.isEqualTo(Answers.CALLS_REAL_METHODS.get());
		assertThat(MockReset.get(spy)).isEqualTo(MockReset.BEFORE);
	}

	@Test
	public void createSpyWhenNullInstanceShouldThrowException() throws Exception {
		SpyDefinition definition = new SpyDefinition("name", REAL_SERVICE_TYPE,
				MockReset.BEFORE, true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Instance must not be null");
		definition.createSpy(null);
	}

	@Test
	public void createSpyWhenWrongInstanceShouldThrowException() throws Exception {
		SpyDefinition definition = new SpyDefinition("name", REAL_SERVICE_TYPE,
				MockReset.BEFORE, true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("must be an instance of");
		definition.createSpy(new ExampleServiceCaller(null));
	}

	@Test
	public void createSpyTwice() throws Exception {
		SpyDefinition definition = new SpyDefinition("name", REAL_SERVICE_TYPE,
				MockReset.BEFORE, true);
		Object instance = new RealExampleService("hello");
		instance = definition.createSpy(instance);
		instance = definition.createSpy(instance);
	}

}
