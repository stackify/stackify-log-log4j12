# stackify-log-log4j12

[![Build Status](https://travis-ci.org/stackify/stackify-log-log4j12.png)](https://travis-ci.org/stackify/stackify-log-log4j12)
[![Coverage Status](https://coveralls.io/repos/stackify/stackify-log-log4j12/badge.png?branch=master)](https://coveralls.io/r/stackify/stackify-log-log4j12?branch=master)

Log4j 1.2 appenders for sending log messages and exceptions to Stackify.

Errors and Logs Overview:

http://docs.stackify.com/m/7787/l/189767

Sign Up for a Trial:

http://www.stackify.com/sign-up/

## Usage

Example appender configuration (*.properties file):
```
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
LogManager.shutdown();
```

## Installation

Add it as a maven dependency:
```xml
<dependency>
    <groupId>com.stackify</groupId>
    <artifactId>stackify-log-log4j12</artifactId>
    <version>1.0.14</version>
</dependency>
```

Note: *We are dependent on the Guava project from Google. We require version 14.0.1 (or beyond) for the background thread that sends data back to Stackify.*

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
