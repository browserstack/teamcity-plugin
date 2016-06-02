package com.browserstack.automate.ci.teamcity;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateResultsTab extends ViewLogTab {

    private static final String TAB_TITLE = "BrowserStack Automate";

    private final PluginDescriptor pluginDescriptor;

    /**
     * Creates and registers tab for Build Results pages
     *
     * @param pagePlaces used to register the tab
     * @param server     server object
     * @param descriptor plugin descriptor
     */
    public AutomateResultsTab(@NotNull PagePlaces pagePlaces, @NotNull final SBuildServer server,
                              @NotNull PluginDescriptor descriptor) {
        super(TAB_TITLE, BrowserStackParameters.AUTOMATE_NAMESPACE, pagePlaces, server);
        setPlaceId(PlaceId.BUILD_RESULTS_TAB);
        setIncludeUrl(descriptor.getPluginResourcesPath("automate.jsp"));
        register();

        pluginDescriptor = descriptor;
    }

    @Override
    protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
        BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL);
        BuildArtifact buildArtifact = buildArtifacts.getArtifact(BrowserStackParameters.getArtifactPath());
        if (buildArtifact != null) {
            InputStream inputStream = null;

            try {
                inputStream = buildArtifact.getInputStream();
                String artifactData = IOUtils.toString(inputStream);
                if (artifactData != null) {
                    SAXBuilder builder = new SAXBuilder();
                    Document document = builder.build(new ByteArrayInputStream(artifactData.getBytes()));
                    model.put("tests", document.getRootElement().getChildren("test"));
                }
            } catch (IOException e) {
                model.put("error", e.getMessage());
            } catch (JDOMException e) {
                model.put("error", e.getMessage());
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        if (!model.containsKey("tests")) {
            model.put("tests", Collections.emptyList());
        }
    }

    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        if (!super.isAvailable(request, build)) {
            return false;
        }

        if (build.getBuildFeaturesOfType(BrowserStackParameters.BUILD_FEATURE_TYPE).isEmpty()) {
            return false;
        }

        BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT);
        BuildArtifact buildArtifact = buildArtifacts.getArtifact(BrowserStackParameters.getArtifactPath());
        return (buildArtifact != null);
    }
}
