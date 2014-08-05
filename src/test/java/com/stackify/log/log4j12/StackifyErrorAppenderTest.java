/*
 * Copyright 2013 Stackify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stackify.log.log4j12;

import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.stackify.api.common.ApiConfiguration;
import com.stackify.api.common.log.LogAppender;
import com.stackify.log.log4j12.StackifyErrorAppender;

/**
 * StackifyErrorAppender JUnit Test
 * 
 * @author Eric Martin
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({StackifyErrorAppender.class})
public class StackifyErrorAppenderTest {
	
	/**
	 * testGetSetApiUrl
	 */
	@Test
	public void testGetSetApiUrl() {
		String apiUrl = "apiUrl";
		StackifyErrorAppender appender = new StackifyErrorAppender();
		Assert.assertEquals("https://api.stackify.com", appender.getApiUrl());
		appender.setApiUrl(apiUrl);
		Assert.assertEquals(apiUrl, appender.getApiUrl());
	}
	
	/**
	 * testGetSetApiKey
	 */
	@Test
	public void testGetSetApiKey() {
		String apiKey = "apiKey";
		StackifyErrorAppender appender = new StackifyErrorAppender();
		Assert.assertNull(appender.getApiKey());
		appender.setApiKey(apiKey);
		Assert.assertEquals(apiKey, appender.getApiKey());
	}
	
	/**
	 * testGetSetApplication
	 */
	@Test
	public void testGetSetApplication() {
		String application = "application";
		StackifyErrorAppender appender = new StackifyErrorAppender();
		Assert.assertNull(appender.getApplication());
		appender.setApplication(application);
		Assert.assertEquals(application, appender.getApplication());
	}
		
	/**
	 * testGetSetEnvironment
	 */
	@Test
	public void testGetSetEnvironment() {
		String environment = "environment";
		StackifyErrorAppender appender = new StackifyErrorAppender();
		Assert.assertNull(appender.getEnvironment());
		appender.setEnvironment(environment);
		Assert.assertEquals(environment, appender.getEnvironment());
	}
	
	/**
	 * testRequiresLayout
	 */
	@Test
	public void testRequiresLayout() {
		StackifyErrorAppender appender = new StackifyErrorAppender();
		Assert.assertFalse(appender.requiresLayout());
	}

	/**
	 * testActivateAppendClose
	 * @throws Exception 
	 */
	@Test
	public void testActivateAppendClose() throws Exception {
		String application = "application";
		String environment = "environment";

		StackifyErrorAppender appender = new StackifyErrorAppender();
		appender.setApiKey("key");
		appender.setApplication(application);
		appender.setEnvironment(environment);
		
		LogAppender<LoggingEvent> logAppender = Mockito.mock(LogAppender.class);
		
		PowerMockito.whenNew(LogAppender.class).withAnyArguments().thenReturn(logAppender);

		appender.activateOptions();
		
		Mockito.verify(logAppender).activate(Mockito.any(ApiConfiguration.class));
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		appender.doAppend(event);
		
		Mockito.verify(logAppender).appendError(event);

		appender.close();
		
		Mockito.verify(logAppender).close();
	}
}
