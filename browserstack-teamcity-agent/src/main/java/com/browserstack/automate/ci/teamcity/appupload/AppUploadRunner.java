package com.browserstack.automate.ci.teamcity.appupload;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters.UploadRunner;
import com.browserstack.automate.ci.teamcity.appupload.AppUploadBuildProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

/**
 * Agent for runner which uploads app to BrowserStack server.
 */
public class AppUploadRunner implements AgentBuildRunner, AgentBuildRunnerInfo {

  @NotNull
  @Override
  public BuildProcess createBuildProcess(@NotNull AgentRunningBuild agentRunningBuild,
      @NotNull BuildRunnerContext buildRunnerContext) throws RunBuildException {
    return new AppUploadBuildProcess(agentRunningBuild, buildRunnerContext);
  }

  @NotNull
  @Override
  public AgentBuildRunnerInfo getRunnerInfo() {
    return this;
  }

  @NotNull
  @Override
  public String getType() {
    return UploadRunner.TYPE;
  }

  @Override
  public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
    return true;
  }
}
