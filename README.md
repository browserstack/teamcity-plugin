# BrowserStack TeamCity Plugin
==============================

This plugin allows you to integrate your Selenium tests in TeamCity with BrowserStack Automate.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Building the Plugin](#building-the-plugin)
  - [For Testing](#for-testing)
  - [For Release](#for-release)
- [Reporting Issues](#reporting-issues)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Features
1. Setup and teardown BrowserStackLocal for testing internal,dev or staging environments. 
2. Embed BrowserStack Automate Reports as a new tab in Build results.
3. Manage BrowserStack credentials in a central location for all your BrowserStack builds.

## Prerequisites
1. Minimum TeamCity version supported is v9+.
2. Minimum Java version supported is JDK 6+.
3. The build must have the BrowserStack build tool plugin. Currently there are plugins for the following build tools,
  * maven
  
## Building the Plugin

### For Testing

When building the plugin package for internal testing build the *zip* package using `mvn clean package`. This will compile the code, 
run unit tests and build the *zip* package. The Google Analytics tracking id that will be used by default will be the one for testing.

### For Release

When building the plugin package for users to install in their Jenkins instance using the command `mvn clean package -Prelease`. 
This will do the same thing as when building the plugin [For Testing](#for-testing) but the production Google Analytics tracking id will be used for 
tracking analytics, if the user has it enabled.

## Reporting Issues

Please email [BrowserStack Support](mailto:support@browserstack.com) to report issues.
