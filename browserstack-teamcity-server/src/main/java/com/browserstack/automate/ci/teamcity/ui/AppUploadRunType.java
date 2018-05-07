package com.browserstack.automate.ci.teamcity.ui;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.helpers.AppUploadPropertiesProcessor;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * RunType for adding app uploading runner.
 */
public class AppUploadRunType extends RunType {

  private final PluginDescriptor pluginDescriptor;

  public AppUploadRunType(RunTypeRegistry runTypeRegistry, PluginDescriptor pluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor;
    runTypeRegistry.registerRunType(this);
  }

  @NotNull
  @Override
  public String getType() {
    return BrowserStackParameters.UploadRunner.TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return BrowserStackParameters.UploadRunner.DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String getDescription() {
    return BrowserStackParameters.UploadRunner.DESCRIPTION;
  }

  @Nullable
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new AppUploadPropertiesProcessor();
  }

  @Nullable
  @Override
  public String getEditRunnerParamsJspFilePath() {
    return this.pluginDescriptor.getPluginResourcesPath("editAppUploadRunParams.jsp");
  }

  @Nullable
  @Override
  public String getViewRunnerParamsJspFilePath() {
    return this.pluginDescriptor.getPluginResourcesPath("viewAppUploadRunParams.jsp");
  }

  @Nullable
  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return new HashMap<String, String>();
  }
}
