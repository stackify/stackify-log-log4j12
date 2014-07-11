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

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.stackify.api.EnvironmentDetail;
import com.stackify.api.LogMsg;
import com.stackify.api.StackifyError;
import com.stackify.api.common.lang.Throwables;
import com.stackify.api.common.log.EventAdapter;

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
	public Optional<Throwable> getThrowable(final LoggingEvent event) {

		ThrowableInformation throwableInfo = event.getThrowableInformation();
		
		if (throwableInfo != null) {
			Throwable t = throwableInfo.getThrowable();
			
			if (t != null) {
				return Optional.of(t);
			}
		}

		Object message = event.getMessage();
		
		if (message != null) {
			if (message instanceof Throwable) {
				return Optional.of((Throwable) message);
			}
		}
		
		return Optional.absent();
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getStackifyError(java.lang.Object, java.lang.Throwable)
	 */
	@Override
	public StackifyError getStackifyError(final LoggingEvent event, final Throwable exception) {
		
		StackifyError.Builder builder = StackifyError.newBuilder();
		builder.environmentDetail(envDetail);		
		builder.occurredEpochMillis(new Date(event.getTimeStamp()));
		builder.error(Throwables.toErrorItem(getLogMessage(event), exception));
		
		return builder.build();
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getLogMsg(java.lang.Object, com.google.common.base.Optional)
	 */
	@Override
	public LogMsg getLogMsg(final LoggingEvent event, final Optional<StackifyError> error) {
		
		LogMsg.Builder builder = LogMsg.newBuilder();
		
		builder.msg(getLogMessage(event));

		Map<String, String> props = getProperties(event);
		
		if (!props.isEmpty()) {
			builder.data(props.toString());
		}
				
		builder.ex(error.orNull());
		builder.th(event.getThreadName());
		builder.epochMs(event.getTimeStamp());
		builder.level(event.getLevel().toString().toLowerCase());

		LocationInfo locInfo = event.getLocationInformation();
		
		if (locInfo != null) {			
			builder.srcMethod(locInfo.getMethodName());
			
			try {
				builder.srcLine(Integer.parseInt(locInfo.getLineNumber()));
			} catch (Throwable e) {
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Returns the log message
	 * @param event The log event
	 * @return The log message or null
	 */
	public String getLogMessage(final LoggingEvent event) {
		
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
}
