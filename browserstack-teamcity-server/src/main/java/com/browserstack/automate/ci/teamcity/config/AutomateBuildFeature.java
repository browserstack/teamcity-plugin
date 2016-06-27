package com.browserstack.automate.ci.teamcity.config;

import com.browserstack.automate.ci.common.analytics.Analytics;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters.EnvVars;
import com.browserstack.automate.ci.teamcity.analytics.TeamcityAnalyticsDataProvider;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AutomateBuildFeature extends BuildFeature {

    private static final String ERR_INVALID_PATH = "Invalid path";

    private final PluginDescriptor pluginDescriptor;

    public AutomateBuildFeature(@NotNull final SBuildServer server, @NotNull final PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
        Analytics.createInstance(new TeamcityAnalyticsDataProvider(server, pluginDescriptor));
    }

    @NotNull
    @Override
    public String getType() {
        return BrowserStackParameters.BUILD_FEATURE_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return BrowserStackParameters.DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return pluginDescriptor.getPluginResourcesPath("automateSettings.jsp");
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params) {
        boolean hasCredentials = params.containsKey(EnvVars.BROWSERSTACK_USER) &&
                params.containsKey(EnvVars.BROWSERSTACK_ACCESSKEY);
        if (!hasCredentials) {
            return "Requires configuration of credentials.";
        }

        boolean hasLocalEnabled = params.containsKey(EnvVars.BROWSERSTACK_LOCAL) &&
                params.get(EnvVars.BROWSERSTACK_LOCAL).toLowerCase().equals("true");

        return "Local Enabled: " + (hasLocalEnabled ? "true" : "false");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters() {
        Map<String, String> defaults = super.getDefaultParameters();
        if (defaults == null) {
            defaults = new HashMap<String, String>();
        }

        defaults.put(BrowserStackParameters.ENABLE_ANALYTICS, "true");
        return defaults;
    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor() {
        return new PropertiesProcessor() {
            @Override
            public Collection<InvalidProperty> process(Map<String, String> properties) {
                Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
                if (properties == null) {
                    return result;
                }

                String browserstackLocalPath = properties.get(BrowserStackParameters.BROWSERSTACK_LOCAL_PATH);
                if (StringUtil.isNotEmpty(browserstackLocalPath)) {
                    File browserstackLocalFile = new File(browserstackLocalPath);
                    if (!browserstackLocalFile.isFile() || !browserstackLocalFile.canExecute()) {
                        result.add(new InvalidProperty(BrowserStackParameters.BROWSERSTACK_LOCAL_PATH, ERR_INVALID_PATH));
                    }
                }

                return result;
            }
        };
    }

    public static SBuildFeatureDescriptor findFeatureDescriptor(SBuild build) {
        Collection<SBuildFeatureDescriptor> buildFeatures = build.getBuildFeaturesOfType(BrowserStackParameters.BUILD_FEATURE_TYPE);
        return (!buildFeatures.isEmpty()) ? buildFeatures.iterator().next() : null;
    }
}
