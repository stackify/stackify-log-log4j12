# stackify-log-log4j12

[![Maven Central](https://img.shields.io/maven-central/v/com.stackify/stackify-log-log4j12.svg)](http://mvnrepository.com/artifact/com.stackify/stackify-log-log4j12)
[![Build Status](https://travis-ci.org/stackify/stackify-log-log4j12.png)](https://travis-ci.org/stackify/stackify-log-log4j12)
[![Coverage Status](https://coveralls.io/repos/stackify/stackify-log-log4j12/badge.png?branch=master)](https://coveralls.io/r/stackify/stackify-log-log4j12?branch=master)

Log4j 1.2 appender for sending log messages and exceptions to Stackify.

Errors and Logs Overview:

http://support.stackify.com/errors-and-logs-overview/

Sign Up for a Trial:

http://www.stackify.com/sign-up/

## Installation

Add it as a maven dependency:
```xml
<dependency>
    <groupId>com.stackify</groupId>
    <artifactId>stackify-log-log4j12</artifactId>
    <version>INSERT_LATEST_MAVEN_CENTRAL_VERSION</version>
    <scope>runtime</scope>
</dependency>
```

## Usage

Example appender configuration (*.properties file):
```properties
log4j.appender.STACKIFY=com.stackify.log.log4j12.StackifyLogAppender
log4j.appender.STACKIFY.apiKey=YOUR_API_KEY
log4j.appender.STACKIFY.application=YOUR_APPLICATION_NAME
log4j.appender.STACKIFY.environment=YOUR_ENVIRONMENT
```

Example appender configuration (*.xml file):
```xml
<appender name="STACKIFY" class="com.stackify.log.log4j12.StackifyLogAppender">
    <param name="apiKey" value="YOUR_API_KEY"/>
    <param name="application" value="YOUR_APPLICATION_NAME"/>
    <param name="environment" value="YOUR_ENVIRONMENT"/>  
</appender>
```

Note: *If you are logging from a device that has the stackify-agent installed, the environment setting is optional. We will use the environment associated to your device in Stackify.*

Be sure to shutdown Log4j to flush this appender of any errors and shutdown the background thread:
```java
org.apache.log4j.LogManager.shutdown();
```


## Masking 

The Stackify appender has built-in data masking for credit cards and social security number values.

**Enable Masking:**

Add `<param name="maskEnabled" value="true"/>` inside the `<appender> ... </appender>` tag.

**Customize Masking:**

The example below has the following customizations: 

1. Credit Card value masking is disabled (`<param name="maskCreditCard" value="false"/>`)
2. IP Address masking is enabled (`<param name="maskIP" value="true"/>`).
3. Custom masking to remove vowels using a regex (`<param name="maskCustom" value="[aeiou]"/> `)

```properties
log4j.appender.STACKIFY=com.stackify.log.log4j12.StackifyLogAppender
log4j.appender.STACKIFY.apiKey=YOUR_API_KEY
log4j.appender.STACKIFY.application=YOUR_APPLICATION_NAME
log4j.appender.STACKIFY.environment=YOUR_ENVIRONMENT
 
log4j.appender.STACKIFY.maskEnabled=true
log4j.appender.STACKIFY.maskCreditCard=false
log4j.appender.STACKIFY.maskSSN=true
log4j.appender.STACKIFY.maskIP=true
log4j.appender.STACKIFY.maskCustom=[aeiou]
```
 
```xml
<appender name="STACKIFY" class="com.stackify.log.log4j12.StackifyLogAppender">
    <param name="apiKey" value="YOUR_API_KEY"/>
    <param name="application" value="YOUR_APPLICATION_NAME"/>
    <param name="environment" value="YOUR_ENVIRONMENT"/>
      
    <param name="maskEnabled" value="true"/>
    <param name="maskCreditCard" value="false"/>
    <param name="maskSSN" value="true"/>
    <param name="maskIP" value="true"/>
    <param name="maskCustom" value="[aeiou]"/> 
</appender>
```


## License

Copyright 2014 Stackify, LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
