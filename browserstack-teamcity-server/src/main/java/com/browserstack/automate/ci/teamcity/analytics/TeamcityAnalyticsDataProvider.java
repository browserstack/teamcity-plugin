package com.browserstack.automate.ci.teamcity.analytics;

import com.browserstack.automate.ci.common.analytics.AnalyticsDataProvider;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Provides data for Analytics to gather and submit.
 *
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class TeamcityAnalyticsDataProvider implements AnalyticsDataProvider {

    private final String appVersion;

    private final String pluginName;

    private final String pluginVersion;

    private final File pluginRootDir;

    private static boolean isEnabled;

    public TeamcityAnalyticsDataProvider(@NotNull final SBuildServer sBuildServer, @NotNull final PluginDescriptor pluginDescriptor) {
        appVersion = sBuildServer.getFullServerVersion();
        pluginName = pluginDescriptor.getPluginName();
        pluginVersion = pluginDescriptor.getPluginVersion();
        pluginRootDir = pluginDescriptor.getPluginRoot();
        isEnabled = true;
    }

    @Override
    public File getRootDir() {
        return pluginRootDir;
    }

    @Override
    public String getApplicationName() {
        return "teamcity";
    }

    @Override
    public String getApplicationVersion() {
        return appVersion;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String getPluginVersion() {
        return pluginVersion;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
