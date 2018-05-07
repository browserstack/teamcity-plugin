package com.browserstack.automate.ci.teamcity;

import java.io.File;

public class BrowserStackParameters {

  public interface EnvVars {

    String BROWSERSTACK_USERNAME = "BROWSERSTACK_USERNAME";
    String BROWSERSTACK_USER = "BROWSERSTACK_USER";
    String BROWSERSTACK_ACCESS_KEY = "BROWSERSTACK_ACCESS_KEY";
    String BROWSERSTACK_ACCESSKEY = "BROWSERSTACK_ACCESSKEY";
    String BROWSERSTACK_LOCAL = "BROWSERSTACK_LOCAL";
    String BROWSERSTACK_LOCAL_IDENTIFIER = "BROWSERSTACK_LOCAL_IDENTIFIER";
    String BROWSERSTACK_BUILD = "BROWSERSTACK_BUILD";
    String BROWSERSTACK_APP_ID = "BROWSERSTACK_APP_ID";
  }

  public static final String BUILD_FEATURE_TYPE = "browserstack-automate";

  public static final String DISPLAY_NAME = "BrowserStack";

  public static final String BROWSERSTACK_LOCAL_PATH = "BROWSERSTACK_LOCAL_PATH";

  public static final String BROWSERSTACK_LOCAL_OPTIONS = "BROWSERSTACK_LOCAL_OPTIONS";

  public static final String ENABLE_ANALYTICS = "ENABLE_ANALYTICS";

  public static final String ARTIFACT_FILE_NAME = "automate-result.xml";

  public static final String AUTOMATE_NAMESPACE = "automate-results";

  public static final String SESSIONS_CONTROLLER_PATH =
      "/" + BrowserStackParameters.AUTOMATE_NAMESPACE + "/sessions.html";

  public static final String BROWSERSTACK_ARTIFACT_DIR = ".browserstack";

  public static final String BROWSERSTACK_REPORT_PATTERN = "REPORT-*.xml";

  public static final String ARTIFACT_LOCATION_PATTERN =
      "**" + File.separator + ".browserstack" + File.separator;

  public static final String ARTIFACT_DIR =
      ".teamcity" + File.separator + BROWSERSTACK_ARTIFACT_DIR + File.separator;

  private BrowserStackParameters() {
  }

  public static String getArtifactPath(String fileName) {
    return ARTIFACT_DIR + File.separator + fileName;
  }

  public static String getArtifactPath() {
    return getArtifactPath(ARTIFACT_FILE_NAME);
  }

  public interface UploadRunner {

    String TYPE = "browserstack-app-upload";
    String FILE_PATH = "file.path";
    String DISPLAY_NAME = "BrowserStack App Upload";
    String DESCRIPTION = "Enter absolute path to the app(.apk pr .ipa) to be uploaded.";

    String FILE_PATH_EMPTY_ERROR_MSG = "File path should not be empty.";
    String INVALID_FILE_EXTENSION_ERROR_MSG = "Specified file is not .apk or .ipa.";
    String FILE_NOT_FOUND_ERROR_MSG = "File not found.";
  }

  public interface BrowserStackProjectType {

    String AUTOMATE = "AUTOMATE";
    String APP_AUTOMATE = "APP_AUTOMATE";
  }
}
