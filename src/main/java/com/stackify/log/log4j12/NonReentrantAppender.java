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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j appender that guards against reentering the same instance of the log4j appender
 * 
 * @author Eric Martin
 */
public abstract class NonReentrantAppender extends AppenderSkeleton {

	/**
	 * Guard against re-entering an appender from the same appender
	 */
    private final ThreadLocal<Boolean> guard =
        new ThreadLocal<Boolean>() {
            @Override protected Boolean initialValue() {
                return Boolean.FALSE;
        }
    };
	
	/**
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected synchronized void append(final LoggingEvent event) {
		
		if (guard == null) {
			return;
		}
		
		if (guard.get() == null) {
			return;
		}
		
		if (guard.get().equals(Boolean.TRUE)) {
			return;
		}

		try {
			guard.set(Boolean.TRUE);
			subAppend(event);
		} finally {
			guard.remove();
		}
	}
	
	/**
	 * Performs the logging of the event after the reentrant guard has been verified
	 * @param event The logging event
	 */
	protected abstract void subAppend(final LoggingEvent event);
}
