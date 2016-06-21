package com.browserstack.automate.ci.teamcity;

import java.io.File;

public class BrowserStackParameters {

    public interface EnvVars {
        String BROWSERSTACK_USER = "BROWSERSTACK_USER";
        String BROWSERSTACK_ACCESSKEY = "BROWSERSTACK_ACCESSKEY";
        String BROWSERSTACK_LOCAL = "BROWSERSTACK_LOCAL";
        String BROWSERSTACK_LOCAL_IDENTIFIER = "BROWSERSTACK_LOCAL_IDENTIFIER";
        String BROWSERSTACK_BUILD = "BROWSERSTACK_BUILD";
    }

    public static final String BUILD_FEATURE_TYPE = "browserstack-automate";

    public static final String DISPLAY_NAME = "BrowserStack";

    public static final String BROWSERSTACK_LOCAL_OPTIONS = "BROWSERSTACK_LOCAL_OPTIONS";

    public static final String ARTIFACT_FILE_NAME = "automate-result.xml";

    public static final String AUTOMATE_NAMESPACE = "automate-results";

    public static final String SESSIONS_CONTROLLER_PATH = "/" + BrowserStackParameters.AUTOMATE_NAMESPACE + "/sessions.html";

    public static final String BROWSERSTACK_ARTIFACT_DIR = ".browserstack";

    public static final String ARTIFACT_LOCATION_PATTERN = "**/.browserstack/";

    public static final String ARTIFACT_DIR = ".teamcity" + File.separator + BROWSERSTACK_ARTIFACT_DIR + File.separator;

    private BrowserStackParameters() {
    }

    public static String getArtifactPath(String fileName) {
        return ARTIFACT_DIR + File.separator + fileName;
    }

    public static String getArtifactPath() {
        return getArtifactPath(ARTIFACT_FILE_NAME);
    }
}