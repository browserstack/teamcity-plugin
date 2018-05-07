package com.browserstack.automate.ci.teamcity.appupload;

import com.browserstack.appautomate.AppAutomateClient;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters.EnvVars;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters.UploadRunner;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.model.AppUploadResponse;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * BuildProcess for uploading app to BrowserStack's server.
 */
public class AppUploadBuildProcess implements BuildProcess {

  private final AgentRunningBuild agentRunningBuild;
  private final BuildRunnerContext buildRunnerContext;
  private final BuildProgressLogger logger;
  private boolean hasFinished = false;
  private boolean isInterrupted = false;

  public AppUploadBuildProcess(@NotNull AgentRunningBuild agentRunningBuild,
      @NotNull BuildRunnerContext buildRunnerContext) {
    this.agentRunningBuild = agentRunningBuild;
    this.buildRunnerContext = buildRunnerContext;
    this.logger = agentRunningBuild.getBuildLogger();
  }

  @Override
  public void start() throws RunBuildException {
    logger.progressMessage("Starting process for app upload.");
    String buildFilePath = getParameter(UploadRunner.FILE_PATH);
    if (StringUtils.isBlank(buildFilePath)) {
      logger.progressMessage("File path is empty, Aborting!!!");
      throw new RunBuildException("File path is empty.");
    }

    logger.progressMessage("Uploading app " + buildFilePath + " to Browserstack.");

    Collection<AgentBuildFeature> buildFeatures = agentRunningBuild
        .getBuildFeaturesOfType(BrowserStackParameters.BUILD_FEATURE_TYPE);
    AgentBuildFeature buildFeature = buildFeatures.iterator().next();
    Map<String, String> map = buildFeature.getParameters();

    String username = map.get(EnvVars.BROWSERSTACK_USERNAME);
    String accessKey = map.get(EnvVars.BROWSERSTACK_ACCESS_KEY);

    // Validate username and accessKey
    if (StringUtils.isEmpty(username)) {
      throw new RunBuildException("BrowserStack username is empty.");
    }

    if (StringUtils.isEmpty(accessKey)) {
      throw new RunBuildException("BrowserStack accessKey is empty.");
    }

    AppAutomateClient appAutomateClient = new AppAutomateClient(username, accessKey);

    try {
      // Upload app file to BrowserStack
      AppUploadResponse appUploadResponse = appAutomateClient.uploadApp(buildFilePath);
      logger.progressMessage(
          buildFilePath + " uploaded successfully to Browserstack with app_url : "
              + appUploadResponse.getAppUrl());
      agentRunningBuild
          .addSharedEnvironmentVariable(EnvVars.BROWSERSTACK_APP_ID, appUploadResponse.getAppUrl());
      logger.progressMessage(
          "Environment variable BROWSERSTACK_APP_ID set with value : " + appUploadResponse
              .getAppUrl());

      // Mark build process to be successfully finished.
      hasFinished = true;

    } catch (AppAutomateException e) {
      throw new RunBuildException(e.getMessage());
    } catch (FileNotFoundException e) {
      throw new RunBuildException(e.getMessage());
    } catch (InvalidFileExtensionException e) {
      throw new RunBuildException(e.getMessage());
    } catch (Exception e) {
      throw new RunBuildException("Internal error occurred.");
    }
  }

  @Override
  public boolean isInterrupted() {
    return isInterrupted;
  }

  @Override
  public boolean isFinished() {
    return hasFinished;
  }

  @Override
  public void interrupt() {
    isInterrupted = true;
  }

  @NotNull
  @Override
  public BuildFinishedStatus waitFor() throws RunBuildException {
    if (hasFinished) {
      return BuildFinishedStatus.FINISHED_SUCCESS;
    } else if (isInterrupted) {
      return BuildFinishedStatus.INTERRUPTED;
    } else {
      return BuildFinishedStatus.FINISHED_FAILED;
    }
  }

  private String getParameter(@NotNull final String parameterName) {
    final String value = buildRunnerContext.getRunnerParameters().get(parameterName);
    if (value == null || value.trim().length() == 0) {
      return null;
    }
    return value.trim();
  }
}
