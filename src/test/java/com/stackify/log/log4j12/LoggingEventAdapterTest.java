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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.stackify.api.EnvironmentDetail;
import com.stackify.api.LogMsg;
import com.stackify.api.StackifyError;
import com.stackify.api.WebRequestDetail;
import com.stackify.api.common.log.ServletLogContext;

/**
 * LoggingEventAdapter JUnit Test
 * @author Eric Martin
 */
public class LoggingEventAdapterTest {

	/**
	 * testGetPropertiesWithoutMdcOrNdc
	 */
	@Test
	public void testGetPropertiesWithoutMdcOrNdc() {
		LoggingEvent event = Mockito.mock(LoggingEvent.class);

		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(0, properties.size());
	}
	
	/**
	 * testGetPropertiesWithMdc
	 */
	@Test
	public void testGetPropertiesWithMdc() {
		Map<String, String> mdcProperties = new HashMap<String, String>();
		mdcProperties.put("mdc1", "val1");
		mdcProperties.put("mdc2", "val2");
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getProperties()).thenReturn(mdcProperties);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(2, properties.size());
		Assert.assertEquals("val1", properties.get("mdc1"));
		Assert.assertEquals("val2", properties.get("mdc2"));
	}
	
	/**
	 * testGetPropertiesWithNdc
	 */
	@Test
	public void testGetPropertiesWithNdc() {
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getNDC()).thenReturn("ndcContext");
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("ndcContext", properties.get("NDC"));
	}

	/**
	 * testGetPropertiesWithMdcAndNdc
	 */
	@Test
	public void testGetPropertiesWithMdcAndNdc() {
		Map<String, String> mdcProperties = new HashMap<String, String>();
		mdcProperties.put("mdc1", "val1");
		mdcProperties.put("mdc2", "val2");
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getProperties()).thenReturn(mdcProperties);
		Mockito.when(event.getNDC()).thenReturn("ndcContext");

		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(3, properties.size());
		Assert.assertEquals("val1", properties.get("mdc1"));
		Assert.assertEquals("val2", properties.get("mdc2"));
		Assert.assertEquals("ndcContext", properties.get("NDC"));
	}
	
	/**
	 * testGetThrowable
	 */
	@Test
	public void testGetThrowable() {
		ThrowableInformation throwableInfo = Mockito.mock(ThrowableInformation.class);
		Mockito.when(throwableInfo.getThrowable()).thenReturn(new NullPointerException());

		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getThrowableInformation()).thenReturn(throwableInfo);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Throwable throwable = adapter.getThrowable(event);
		
		Assert.assertNotNull(throwable);
	}
	
	/**
	 * testGetThrowableFromMessage
	 */
	@Test
	public void testGetThrowableFromMessage() {
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getMessage()).thenReturn(new NullPointerException());
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Throwable throwable = adapter.getThrowable(event);
		
		Assert.assertNotNull(throwable);
	}

	/**
	 * testGetThrowableAbsent
	 */
	@Test
	public void testGetThrowableAbsent() {
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Throwable throwable = adapter.getThrowable(event);
		
		Assert.assertNull(throwable);
	}
	
	/**
	 * testGetLogMsg
	 */
	@Test
	public void testGetLogMsg() {
		String msg = "msg";
		StackifyError ex = Mockito.mock(StackifyError.class);
		String th = "th";
		String level = "debug";
		String srcClass = "srcClass";
		String srcMethod = "srcMethod";
		Integer srcLine = Integer.valueOf(14);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("key", "value");
		
		LocationInfo locInfo = Mockito.mock(LocationInfo.class);
		Mockito.when(locInfo.getClassName()).thenReturn(srcClass);
		Mockito.when(locInfo.getMethodName()).thenReturn(srcMethod);
		Mockito.when(locInfo.getLineNumber()).thenReturn(srcLine.toString());
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getMessage()).thenReturn(msg);
		Mockito.when(event.getThreadName()).thenReturn(th);
		Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);
		Mockito.when(event.getLocationInformation()).thenReturn(locInfo);
		Mockito.when(event.getProperties()).thenReturn(properties);

		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		LogMsg logMsg = adapter.getLogMsg(event, ex);
		
		Assert.assertNotNull(logMsg);
		Assert.assertEquals(msg, logMsg.getMsg());
		Assert.assertEquals("{\"key\":\"value\"}", logMsg.getData());
		Assert.assertEquals(ex, logMsg.getEx());		
		Assert.assertEquals(th, logMsg.getTh());		
		Assert.assertEquals(level, logMsg.getLevel());			
		Assert.assertEquals(srcClass + "." + srcMethod, logMsg.getSrcMethod());		
		Assert.assertEquals(srcLine, logMsg.getSrcLine());		
		Assert.assertEquals(srcLine, logMsg.getSrcLine());		
	}
	
	/**
	 * testGetStackifyError
	 */
	@Test
	public void testGetStackifyError() {
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getMessage()).thenReturn("Exception message");
		
		Throwable exception = Mockito.mock(Throwable.class);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, exception);
		
		Assert.assertNotNull(error);
	}
	
	/**
	 * testGetStackifyErrorServletContext
	 */
	@Test
	public void testGetStackifyErrorServletContext() {
		String user = "user";
		ServletLogContext.putUser(user);
		
		WebRequestDetail webRequest = WebRequestDetail.newBuilder().build();
		ServletLogContext.putWebRequest(webRequest);
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getMessage()).thenReturn("Exception message");
		
		Throwable exception = Mockito.mock(Throwable.class);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, exception);
		
		Assert.assertNotNull(error);
		
		Assert.assertEquals(user, error.getUserName());
		Assert.assertNotNull(error.getWebRequestDetail());
	}
	
	/**
	 * testGetLogMsgServletContext
	 */
	@Test
	public void testGetLogMsgServletContext() {
		String transactionId = UUID.randomUUID().toString();
		ServletLogContext.putTransactionId(transactionId);
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);

		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		LogMsg logMsg = adapter.getLogMsg(event, null);
		
		Assert.assertNotNull(logMsg);
		Assert.assertEquals(transactionId, logMsg.getTransId());
	}
	
	/**
	 * 
	 */
	@Test
	public void testIsErrorLevel() {
		LoggingEvent debug = Mockito.mock(LoggingEvent.class);
		Mockito.when(debug.getLevel()).thenReturn(Level.DEBUG);

		LoggingEvent error = Mockito.mock(LoggingEvent.class);
		Mockito.when(error.getLevel()).thenReturn(Level.ERROR);
		
		LoggingEvent fatal = Mockito.mock(LoggingEvent.class);
		Mockito.when(fatal.getLevel()).thenReturn(Level.FATAL);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));

		Assert.assertFalse(adapter.isErrorLevel(debug));
		Assert.assertTrue(adapter.isErrorLevel(error));
		Assert.assertTrue(adapter.isErrorLevel(fatal));
	}
	
	/**
	 * testGetStackifyErrorWithoutException
	 */
	@Test
	public void testGetStackifyErrorWithoutException() {
		LocationInfo locInfo = Mockito.mock(LocationInfo.class);
		Mockito.when(locInfo.getClassName()).thenReturn("class");
		Mockito.when(locInfo.getMethodName()).thenReturn("method");
		Mockito.when(locInfo.getLineNumber()).thenReturn("123");
		
		LoggingEvent event = Mockito.mock(LoggingEvent.class);
		Mockito.when(event.getMessage()).thenReturn("Exception message");
		Mockito.when(event.getLocationInformation()).thenReturn(locInfo);
		
		LoggingEventAdapter adapter = new LoggingEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, null);
		
		Assert.assertNotNull(error);
		Assert.assertEquals("StringException", error.getError().getErrorType());
	}
}
