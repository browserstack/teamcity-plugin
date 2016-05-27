package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters.EnvVars;
import com.browserstack.local.Local;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrowserStackLocalAgent extends AgentLifeCycleAdapter {

    private boolean isEnabled;

    private Local browserstackLocal;

    private boolean isLocalRunning;

    private String localIdentifier;

    private AgentBuildFeature buildFeature;

    public BrowserStackLocalAgent(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
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
        browserstackLocal = new BrowserStackLocal(buildLogger);

        Map<String, String> config = buildFeature.getParameters();
        if (config.containsKey(EnvVars.BROWSERSTACK_ACCESSKEY)) {
            localIdentifier = newLocalIdentifier();

            Map<String, String> localOptions = new HashMap<String, String>();
            localOptions.put("key", config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
            localOptions.put("localIdentifier", localIdentifier);
            buildLogger.message("Starting BrowserStack Local");

            try {
                browserstackLocal.start(localOptions);
                isLocalRunning = browserstackLocal.isRunning();
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
        if (isEnabled) {
            killLocal(runner.getBuild());
        }
    }

    @Override
    public void buildFinished(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        if (isEnabled) {
            killLocal(build);
        }
    }

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

    private void exportEnvVars(final BuildRunnerContext runner, final Map<String, String> config) {
        if (!config.containsKey(EnvVars.BROWSERSTACK_USER) || !config.containsKey(EnvVars.BROWSERSTACK_ACCESSKEY)) {
            return;
        }

        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_USER, config.get(EnvVars.BROWSERSTACK_USER));
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_ACCESSKEY, config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_LOCAL, config.get(EnvVars.BROWSERSTACK_LOCAL));
        runner.addEnvironmentVariable(EnvVars.BROWSERSTACK_LOCAL_IDENTIFIER, localIdentifier);

        BuildProgressLogger buildLogger = runner.getBuild().getBuildLogger();
        buildLogger.message(EnvVars.BROWSERSTACK_USER + "=" + config.get(EnvVars.BROWSERSTACK_USER));
        buildLogger.message(EnvVars.BROWSERSTACK_ACCESSKEY + "=" + config.get(EnvVars.BROWSERSTACK_ACCESSKEY));
        buildLogger.message(EnvVars.BROWSERSTACK_LOCAL + "=true");
        buildLogger.message(EnvVars.BROWSERSTACK_LOCAL_IDENTIFIER + "=" + localIdentifier);
    }

    private String newLocalIdentifier() {
        return UUID.randomUUID().toString().replaceAll("\\-", "");
    }

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
