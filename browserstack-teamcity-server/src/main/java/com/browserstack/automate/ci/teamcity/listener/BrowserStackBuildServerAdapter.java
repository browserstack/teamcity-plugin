package com.browserstack.automate.ci.teamcity.listener;

import com.browserstack.automate.ci.common.analytics.Analytics;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.analytics.TeamcityAnalyticsDataProvider;
import com.browserstack.automate.ci.teamcity.config.AutomateBuildFeature;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class BrowserStackBuildServerAdapter extends BuildServerAdapter {

    private final Analytics analytics;

    public BrowserStackBuildServerAdapter(@NotNull final SBuildServer sBuildServer, @NotNull final PluginDescriptor pluginDescriptor) {
        this.analytics = Analytics.createInstance(new TeamcityAnalyticsDataProvider(sBuildServer, pluginDescriptor));
        sBuildServer.addListener(this);
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        super.buildStarted(build);

        trackBuildRun(build);
    }

    private void trackBuildRun(@NotNull SRunningBuild build) {
        if (analytics != null) {
            SBuildFeatureDescriptor featureDescriptor = AutomateBuildFeature.findFeatureDescriptor(build);
            if (featureDescriptor != null) {
                Map<String, String> params = featureDescriptor.getParameters();
                analytics.setEnabled(!params.containsKey(BrowserStackParameters.ENABLE_ANALYTICS) ||
                        params.get(BrowserStackParameters.ENABLE_ANALYTICS).equalsIgnoreCase("true"));

                boolean localEnabled = params.containsKey(BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL) &&
                        params.get(BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL).equalsIgnoreCase("true");

                boolean localOptionsSet = params.containsKey(BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS) &&
                        params.get(BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS).length() > 0;

                analytics.trackBuildRun(localEnabled, false, localOptionsSet, true);
            }
        }
    }
}
