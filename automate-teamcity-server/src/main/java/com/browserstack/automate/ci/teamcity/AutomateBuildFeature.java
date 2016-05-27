package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters.EnvVars;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AutomateBuildFeature extends BuildFeature {

    private final PluginDescriptor pluginDescriptor;

    public AutomateBuildFeature(@NotNull final PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
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
}
