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

import com.stackify.api.common.ApiClients;
import com.stackify.api.common.ApiConfiguration;
import com.stackify.api.common.ApiConfigurations;
import com.stackify.api.common.log.LogAppender;
import com.stackify.api.common.mask.Masker;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j 1.2 logger appender for sending logs to Stackify.
 * <p>
 * <p>
 * Example appender configuration (*.properties file):
 * <pre>
 * {@code
 * log4j.appender.STACKIFY=com.stackify.log.log4j12.StackifyLogAppender
 * log4j.appender.STACKIFY.apiKey=YOUR_API_KEY
 * log4j.appender.STACKIFY.application=YOUR_APPLICATION_NAME
 * log4j.appender.STACKIFY.environment=YOUR_ENVIRONMENT
 * }
 * </pre>
 * <p>
 * <p>
 * Example appender configuration (*.xml file):
 * <pre>
 * {@code
 * <appender name="STACKIFY" class="com.stackify.log.log4j12.StackifyLogAppender">
 *     <param name="apiKey" value="YOUR_API_KEY"/>
 *     <param name="application" value="YOUR_APPLICATION_NAME"/>
 *     <param name="environment" value="YOUR_ENVIRONMENT"/>
 * </appender>
 * }
 * </pre>
 * <p>
 * <p>
 * Be sure to shutdown Log4j to flush this appender of any logs and shutdown the background thread:
 * <pre>
 * LogManager.shutdown();
 * </pre>
 *
 * @author Eric Martin
 */
@Setter
@Getter
public class StackifyLogAppender extends NonReentrantAppender {

    /**
     * API URL (Appender configuration parameter)
     */
    private String apiUrl = "https://api.stackify.com";

    /**
     * Auth API URL
     */
    private String authUrl = "https://auth.stackify.net";

    /**
     * API Key (Appender configuration parameter)
     */
    private String apiKey = null;

    /**
     * Application name (Appender configuration parameter)
     */
    private String application = null;

    /**
     * Environment (Appender configuration parameter)
     */
    private String environment = null;

    /**
     * Generic log appender
     */
    private LogAppender<LoggingEvent> logAppender;

    private String maskEnabled;

    private String maskCreditCard;

    private String maskSSN;

    private String maskIP;

    private String maskCustom;

    /**
     * @see org.apache.log4j.AppenderSkeleton#activateOptions()
     */
    @Override
    public void activateOptions() {
        super.activateOptions();

        // build the api config

        ApiConfiguration apiConfig = ApiConfigurations.fromPropertiesWithOverrides(apiUrl, authUrl, apiKey, application, environment);

        // get the client project name with version

        String clientName = ApiClients.getApiClient(StackifyLogAppender.class, "/stackify-log-log4j12.properties", "stackify-log-log4j12");

        // build the log appender

        try {

            // setup masker

            Masker masker = new Masker();
            if (maskEnabled != null && Boolean.parseBoolean(maskEnabled)) {

                // set default masks
                masker.addMask(Masker.MASK_CREDITCARD);
                masker.addMask(Masker.MASK_SSN);

                if (maskCreditCard != null && !Boolean.parseBoolean(maskCreditCard)) {
                    masker.removeMask(Masker.MASK_CREDITCARD);
                }

                if (maskSSN != null && !Boolean.parseBoolean(maskSSN)) {
                    masker.removeMask(Masker.MASK_SSN);
                }

                if (maskIP != null && Boolean.parseBoolean(maskIP)) {
                    masker.addMask(Masker.MASK_IP);
                }

                if (maskCustom != null) {
                    masker.addMask(maskCustom);
                }

            } else {
                masker.clearMasks();
            }

            this.logAppender = new LogAppender<LoggingEvent>(clientName, new LoggingEventAdapter(apiConfig.getEnvDetail()), masker);
            this.logAppender.activate(apiConfig);
        } catch (Exception e) {
            errorHandler.error("Exception starting the Stackify_LogBackgroundService", e, 0);
        }
    }

    /**
     * @see com.stackify.log.log4j12.NonReentrantAppender#subAppend(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void subAppend(final LoggingEvent event) {
        try {
            this.logAppender.append(event);
        } catch (Exception e) {
            errorHandler.error("Exception appending event to Stackify Log Appender", e, 0);
        }
    }

    /**
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close() {
        try {
            this.logAppender.close();
        } catch (Exception e) {
            errorHandler.error("Exception closing Stackify Log Appender", e, 0);
        }
    }

    /**
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
