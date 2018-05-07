package com.browserstack.automate.ci.teamcity.helpers;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters.UploadRunner;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;

/**
 * Validates given app path param in app upload runner.
 */
public class AppUploadPropertiesProcessor implements PropertiesProcessor {

  @Override
  public Collection<InvalidProperty> process(Map<String, String> properties) {
    List<InvalidProperty> result = new ArrayList<InvalidProperty>();

    String filePath = properties.get(BrowserStackParameters.UploadRunner.FILE_PATH).trim();

    if (PropertiesUtil.isEmptyOrNull(filePath)) {
      result.add(new InvalidProperty(BrowserStackParameters.UploadRunner.FILE_PATH,
          UploadRunner.FILE_PATH_EMPTY_ERROR_MSG));
    }

    if (!(filePath.endsWith(".apk") || filePath.endsWith(".ipa"))) {
      result.add(new InvalidProperty(BrowserStackParameters.UploadRunner.FILE_PATH,
          UploadRunner.INVALID_FILE_EXTENSION_ERROR_MSG));
    }

    File file = new File(filePath);
    if (!file.exists()) {
      result.add(
          new InvalidProperty(BrowserStackParameters.UploadRunner.FILE_PATH,
              UploadRunner.FILE_NOT_FOUND_ERROR_MSG));
    }

    return result;
  }
}
