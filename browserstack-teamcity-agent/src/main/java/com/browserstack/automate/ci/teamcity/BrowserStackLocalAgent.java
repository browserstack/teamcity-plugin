package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters.EnvVars;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Component that runs on the Agent side.
 * Setups and tears down BrowserStackLocal, if configured.
 * Also responsible for copying report files into artifacts after build completion.
 */
public class BrowserStackLocalAgent extends AgentLifeCycleAdapter {
    private static final String REPORT_FILE_PATTERN = "**/browserstack-reports/REPORT-*.xml";

    private boolean isEnabled;

    private TeamCityBrowserStackLocal browserstackLocal;

    private boolean isLocalRunning;

    private String localIdentifier;

    private AgentBuildFeature buildFeature;

    @NotNull
    private final ArtifactsWatcher artifactsWatcher;

    public BrowserStackLocalAgent(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                                  @NotNull ArtifactsWatcher watcher) {
        artifactsWatcher = watcher;
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        AgentRunningBuild build = runner.getBuild();
        loadBuildFeature(build);
        if (!isEnabled) {
            return;
        }

        BuildProgressLogger buildLogger = build.getBuildLogger();
        Map<String, String> config = buildFeature.getParameters();
        browserstackLocal = new TeamCityBrowserStackLocal(config.get(BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS), buildLogger);

        if (config.containsKey(EnvVars.BROWSERSTACK_ACCESSKEY)) {
            Map<String, String> localOptions = new HashMap<String, String>();
            localOptions.put("key", config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
            buildLogger.message("Starting BrowserStack Local");

            try {
                browserstackLocal.start(localOptions);
                isLocalRunning = browserstackLocal.isRunning();
                localIdentifier = browserstackLocal.getLocalIdentifier();
            } catch (Exception e) {
                buildLogger.error(e.getMessage());
                runner.getBuild().stopBuild(e.getMessage());
                return;
            }

            if (isLocalRunning) {
                buildLogger.message("Launched BrowserStack Local");
                exportEnvVars(runner, config);
            } else {
                String errMessage = "Failed to launch BrowserStack Local";
                buildLogger.error(errMessage);
                runner.getBuild().stopBuild(errMessage);
            }
        } else {
            buildLogger.message(EnvVars.BROWSERSTACK_ACCESSKEY + " not configured.");
        }
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        AgentRunningBuild build = runner.getBuild();
        if (isEnabled) {
            killLocal(build);
        }

        // find and collect report files that match our Ant pattern
        // if found, artifacts are published to the artifact directory by appending "=> <path>"
        List<File> reportFiles = new ArrayList<File>();
        FileUtil.collectMatchedFiles(build.getCheckoutDirectory(),
                Pattern.compile(FileUtil.convertAntToRegexp(REPORT_FILE_PATTERN)),
                reportFiles);

        for (File reportFile : reportFiles) {
            artifactsWatcher.addNewArtifactsPath(reportFile.getAbsolutePath() + "=>" + BrowserStackParameters.ARTIFACT_DIR);
        }
    }

    @Override
    public void buildFinished(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        if (isEnabled) {
            killLocal(build);
        }
    }

    /**
     * Loads the build feature for the current build and checks if BrowserStackLocal is enabled
     *
     * @param build Current build.
     */
    private void loadBuildFeature(final AgentRunningBuild build) {
        Collection<AgentBuildFeature> buildFeatures = build.getBuildFeaturesOfType(BrowserStackParameters.BUILD_FEATURE_TYPE);
        isEnabled = !buildFeatures.isEmpty();
        if (isEnabled) {
            buildFeature = buildFeatures.iterator().next();
            if (buildFeature.getParameters().containsKey(EnvVars.BROWSERSTACK_LOCAL)) {
                isEnabled = buildFeature.getParameters().get(EnvVars.BROWSERSTACK_LOCAL).equalsIgnoreCase("true");
            }
        }
    }

    /**
     * Adds environment variables to the build environment.
     *
     * @param runner Represents current build runner.
     * @param config
     */
    private void exportEnvVars(final BuildRunnerContext runner, final Map<String, String> config) {
        if (!config.containsKey(EnvVars.BROWSERSTACK_USER) || !config.containsKey(EnvVars.BROWSERSTACK_ACCESSKEY)) {
            return;
        }

        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_USER, config.get(EnvVars.BROWSERSTACK_USER));
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_ACCESSKEY, config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_LOCAL, config.get(EnvVars.BROWSERSTACK_LOCAL));

        BuildProgressLogger buildLogger = runner.getBuild().getBuildLogger();
        buildLogger.message(EnvVars.BROWSERSTACK_USER + "=" + config.get(EnvVars.BROWSERSTACK_USER));
        buildLogger.message(EnvVars.BROWSERSTACK_ACCESSKEY + "=" + config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
        buildLogger.message(EnvVars.BROWSERSTACK_LOCAL + "=" + config.get(EnvVars.BROWSERSTACK_LOCAL));

        if (localIdentifier != null) {
            runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_LOCAL_IDENTIFIER, localIdentifier);
            buildLogger.message(EnvVars.BROWSERSTACK_LOCAL_IDENTIFIER + "=" + localIdentifier);
        }

        String buildId = getBuildId(runner);
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_BUILD, buildId);
        buildLogger.message(EnvVars.BROWSERSTACK_BUILD + "=" + buildId);
    }

    /**
     * Returns a unique Build Id for the currently running build.
     *
     * @param runner Represents current build runner.
     * @return String Unique build Id.
     */
    private static String getBuildId(final BuildRunnerContext runner) {
        return runner.getBuild().getBuildTypeName() +
                "-" +
                runner.getBuild().getBuildId() +
                "-" +
                runner.getBuild().getBuildNumber();
    }

    /**
     * Terminates the BrowserStackLocal binary.
     *
     * @param build Represents running build on the agent side.
     */
    private void killLocal(final AgentRunningBuild build) {
        if (browserstackLocal != null) {
            BuildProgressLogger buildLogger = build.getBuildLogger();

            if (isLocalRunning) {
                buildLogger.message("Stopping BrowserStack Local");

                try {
                    browserstackLocal.stop();
                    browserstackLocal = null;
                } catch (Exception e) {
                    buildLogger.warning(e.getMessage());
                }
            }
        }
    }
}
