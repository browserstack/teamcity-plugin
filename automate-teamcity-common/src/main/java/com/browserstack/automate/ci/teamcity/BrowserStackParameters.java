package com.browserstack.automate.ci.teamcity;

public class BrowserStackParameters {

    public interface EnvVars {
        String BROWSERSTACK_USER = "BROWSERSTACK_USER";
        String BROWSERSTACK_ACCESSKEY = "BROWSERSTACK_ACCESSKEY";
        String BROWSERSTACK_LOCAL = "BROWSERSTACK_LOCAL";
        String BROWSERSTACK_LOCAL_IDENTIFIER = "BROWSERSTACK_LOCAL_IDENTIFIER";
    }

    public static final String BUILD_FEATURE_TYPE = "browserstack-automate";

    public static final String DISPLAY_NAME = "BrowserStack Automate";

    public static final String BROWSERSTACK_LOCAL_OPTIONS = "BROWSERSTACK_LOCAL_OPTIONS";


    private BrowserStackParameters() {
    }
}