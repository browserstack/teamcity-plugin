package com.browserstack.automate.ci.teamcity.beans;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import org.jetbrains.annotations.NotNull;

/**
 * Bean for providing constants for app upload build step.
 */
public class AppUploadConstantsBean {

  @NotNull
  public String getFilePath() {
    return BrowserStackParameters.UploadRunner.FILE_PATH;
  }
}
