# BrowserStack TeamCity Plugin
==============================

This plugin allows you to integrate your Selenium tests in TeamCity with BrowserStack Automate.

## Features
1. Setup and teardown BrowserStackLocal for testing internal,dev or staging environments. 
2. Embed BrowserStack Automate Reports as a new tab in Build results.
3. Manage BrowserStack credentials in a central location for all your BrowserStack builds.

## Prerequisites
1. Minimum TeamCity version supported is v9+.
2. Minimum Java version supported is JDK 6+.
3. For viewing BrowserStack Automate report in your TeamCity test results:
  * Project must be a Java project.
  * Project must be built with Maven and use either TestNG or JUnit for testing.

## Resources
* Check out BrowserStack's [complete documentation](https://www.browserstack.com/automate/teamcity) for running tests on BrowserStack using TeamCity.

Please email [BrowserStack Support](mailto:support@browserstack.com) to report issues.
