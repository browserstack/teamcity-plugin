package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.AutomateClient;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.SessionNotFound;
import jetbrains.buildServer.log.Loggers;
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
import org.apache.commons.lang.StringUtils;
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
import java.util.regex.Pattern;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateResultsTab extends ViewLogTab {

    private static final String TAB_TITLE = "BrowserStack";

    private static final Pattern PATTERN_PARAM_SESSION = Pattern.compile("&session=.*");

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
        setIncludeUrl(descriptor.getPluginResourcesPath("automateResult.jsp"));
        register();
    }

    @Override
    protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
        String sessionId = request.getParameter("session");

        if (StringUtils.isNotBlank(sessionId)) {
            Loggers.SERVER.info("Rendering session: " + sessionId);
            // render one test session
            fillModelSession(sessionId, model, request, build);
        } else {
            // list all tests
            fillModelSessionList(model, build);
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

    private void fillModelSession(final String sessionId, final Map<String, Object> model,
                                  final HttpServletRequest request, final SBuild build) {
        AutomateClient automateClient = AutomateSessionController.newAutomateClient(build);
        if (automateClient != null) {
            try {
                model.put("session", automateClient.getSession(sessionId));
                Loggers.SERVER.info("Session fetch success: " + sessionId);

                String resultsUrl = request.getRequestURI();
                if (resultsUrl != null) {
                    resultsUrl += "?" + PATTERN_PARAM_SESSION.matcher(request.getQueryString()).replaceAll("");
                    model.put("resultsUrl", resultsUrl);
                    Loggers.SERVER.info("Session fetch resultsUrl: " + resultsUrl);
                }
            } catch (SessionNotFound sessionNotFound) {
                model.put("error", sessionNotFound.getMessage());
                Loggers.SERVER.info("Session not found: " + sessionId);
            } catch (AutomateException e) {
                model.put("error", e.getMessage());
                Loggers.SERVER.info("Session fetch failed: " + sessionId);
            }
        } else {
            model.put("error", "Failed to configure AutomateClient");
            Loggers.SERVER.info("automateClient == null");
        }
    }

    private void fillModelSessionList(final Map<String, Object> model, final SBuild build) {
        Loggers.SERVER.info("Rendering test list");

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
}
