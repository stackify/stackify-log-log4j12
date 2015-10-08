/*
 * Copyright 2014 Stackify
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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackify.api.EnvironmentDetail;
import com.stackify.api.LogMsg;
import com.stackify.api.StackifyError;
import com.stackify.api.WebRequestDetail;
import com.stackify.api.common.lang.Throwables;
import com.stackify.api.common.log.EventAdapter;
import com.stackify.api.common.log.ServletLogContext;
import com.stackify.api.common.util.Maps;
import com.stackify.api.common.util.Preconditions;

/**
 * LoggingEventAdapter
 * @author Eric Martin
 */
public class LoggingEventAdapter implements EventAdapter<LoggingEvent> {

	/**
	 * Environment detail
	 */
	private final EnvironmentDetail envDetail;
	
	/**
	 * JSON converter
	 */
	private final ObjectMapper json = new ObjectMapper();
	
	/**
	 * Constructor
	 * @param envDetail Environment detail
	 */
	public LoggingEventAdapter(final EnvironmentDetail envDetail) {
		Preconditions.checkNotNull(envDetail);
		this.envDetail = envDetail;
	}
	
	/**
	 * @see com.stackify.api.common.log.EventAdapter#getThrowable(java.lang.Object)
	 */
	@Override
	public Throwable getThrowable(final LoggingEvent event) {

		ThrowableInformation throwableInfo = event.getThrowableInformation();
		
		if (throwableInfo != null) {
			Throwable t = throwableInfo.getThrowable();
			
			if (t != null) {
				return t;
			}
		}

		Object message = event.getMessage();
		
		if (message != null) {
			if (message instanceof Throwable) {
				return (Throwable) message;
			}
		}
		
		return null;
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getStackifyError(java.lang.Object, java.lang.Throwable)
	 */
	@Override
	public StackifyError getStackifyError(final LoggingEvent event, final Throwable exception) {
		
		StackifyError.Builder builder = StackifyError.newBuilder();
		builder.environmentDetail(envDetail);		
		builder.occurredEpochMillis(new Date(event.getTimeStamp()));
		
		if (exception != null) {
			builder.error(Throwables.toErrorItem(getMessage(event), exception));
		} else {
			String className = null;
			String methodName = null;
			int lineNumber = 0;
			
			LocationInfo locInfo = event.getLocationInformation();
			
			if (locInfo != null) {
				className = locInfo.getClassName();
				methodName = locInfo.getMethodName();
				
				try {
					lineNumber = Integer.parseInt(locInfo.getLineNumber());
				} catch (Throwable e) {
				}
			}
			
			builder.error(Throwables.toErrorItem(getMessage(event), className, methodName, lineNumber));
		}
		
		String user = ServletLogContext.getUser();
		
		if (user != null) {
			builder.userName(user);
		}
		
		WebRequestDetail webRequest = ServletLogContext.getWebRequest();
		
		if (webRequest != null) {
			builder.webRequestDetail(webRequest);
		}
		
		builder.serverVariables(Maps.fromProperties(System.getProperties()));
		
		return builder.build();
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getLogMsg(java.lang.Object, com.google.common.base.Optional)
	 */
	@Override
	public LogMsg getLogMsg(final LoggingEvent event, final StackifyError error) {
		
		LogMsg.Builder builder = LogMsg.newBuilder();
		
		builder.msg(getMessage(event));

		Map<String, String> props = getProperties(event);
		
		if (!props.isEmpty()) {
			try {
				builder.data(json.writeValueAsString(props));
			} catch (Exception e) {
				// do nothing
			}
		}
				
		builder.ex(error);
		builder.th(event.getThreadName());
		builder.epochMs(event.getTimeStamp());
		builder.level(event.getLevel().toString().toLowerCase());

		String transactionId = ServletLogContext.getTransactionId();
		
		if (transactionId != null) {
			builder.transId(transactionId);
		}

		LocationInfo locInfo = event.getLocationInformation();

		if (locInfo != null) {			
			builder.srcMethod(locInfo.getClassName() + "." + locInfo.getMethodName());
			
			try {
				builder.srcLine(Integer.parseInt(locInfo.getLineNumber()));
			} catch (Throwable e) {
			}
		}
		
		return builder.build();
	}

	/**
	 * Gets the log message from the event
	 * @param event The event
	 * @return The log message
	 */
	public String getMessage(final LoggingEvent event) {
		
		Object message = event.getMessage();
		
		if (message != null) {
			if (message instanceof String) {
				return (String) message;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets properties from the event's MDC and MDC
	 * @param event The logging event
	 * @return Map assembled from the event's MDC and NDC
	 */
	public Map<String, String> getProperties(final LoggingEvent event) {
		
		Map<String, String> properties = new HashMap<String, String>();
		
		// unload the MDC
		
		Map<?, ?> mdc = event.getProperties();
		
		if (mdc != null) {
		    Iterator<?> mdcIterator = mdc.entrySet().iterator();
		    
		    while (mdcIterator.hasNext()) {
		        Map.Entry<?, ?> entryPair = (Map.Entry<?, ?>) mdcIterator.next();
		        
		        Object key = entryPair.getKey();
		        Object value = entryPair.getValue();
		        
		        properties.put(key.toString(), value != null ? value.toString() : null);
		    }
		}
		
		// unload the NDC
		
		String ndc = event.getNDC();
		
		if (ndc != null) {
			if (!ndc.isEmpty()) {
				properties.put("NDC", ndc);
			}
		}
		
		// return the properties
		
		return properties;		
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#isErrorLevel(java.lang.Object)
	 */
	@Override
	public boolean isErrorLevel(final LoggingEvent event) {
		return event.getLevel().isGreaterOrEqual(Level.ERROR);
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getClassName(java.lang.Object)
	 */
	@Override
	public String getClassName(final LoggingEvent event) {
		LocationInfo locInfo = event.getLocationInformation();

		if (locInfo != null) {			
			return locInfo.getClassName();
		}
		
		return null;
	}
}
