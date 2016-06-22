package com.browserstack.automate.ci.teamcity.ui;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateSessionLink extends SimplePageExtension {


    public AutomateSessionLink(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor pluginDescriptor) {
        super(pagePlaces);
        setPlaceId(PlaceId.TEST_DETAILS_BLOCK);
        setPosition(PositionConstraint.first());
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("automateSessionLink.jsp"));
        setPluginName(BrowserStackParameters.AUTOMATE_NAMESPACE);
        register();

        Loggers.SERVER.info("AutomateSessionLink");
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        Object testAttr = request.getAttribute("test");
        Object testRunsAttr = request.getAttribute("testRuns");
        if (testAttr != null && testRunsAttr != null) {
            STest test = (STest) testAttr;

            @SuppressWarnings("unchecked")
            List<STestRun> testRuns = (List<STestRun>) testRunsAttr;
            STestRun testRun = null;

            for (STestRun tr : testRuns) {
                if (tr.getTest().getTestNameId() == test.getTestNameId()) {
                    testRun = tr;
                    break;
                }
            }

            if (testRun != null) {
                try {
                    String sessionId = findSessionId(testRun);
                    if (StringUtils.isNotBlank(sessionId)) {
                        model.put("session", sessionId);
                    }
                } catch (IOException e) {
                    Loggers.SERVER.info(e.getMessage());
                    model.put("error", e.getMessage());
                }
            }
        }

        model.put("pgClazz", this.getClass().getSimpleName());
        model.put("pgPlace", this.getPlaceId().toString());
    }

    @Override
    public boolean isAvailable(@NotNull final HttpServletRequest request) {
        Loggers.SERVER.info("AutomateSessionLink.isAvailable");
        return true;
    }

    private static String findSessionId(final STestRun testRun) throws IOException {
        SBuild build = testRun.getBuild();
        BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL);
        BuildArtifact buildArtifact = buildArtifacts.getArtifact(BrowserStackParameters.getArtifactPath());
        if (buildArtifact == null) {
            throw new IOException("Failed to fetch build artifact");
        }

        InputStream inputStream = null;

        try {
            inputStream = buildArtifact.getInputStream();
            String artifactData = IOUtils.toString(inputStream);
            if (artifactData == null) {
                throw new IOException("Failed to read build artifact");
            }

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new ByteArrayInputStream(artifactData.getBytes()));

            @SuppressWarnings("unchecked")
            List<Element> testElementList = document.getRootElement().getChildren("test");
            String testName = testRun.getTest().getName().getAsString();
            for (Element el : testElementList) {
                Loggers.SERVER.info("Compare: " + el.getAttribute("id").getValue() + " / " + testName);

                if (el.getAttribute("id").getValue().equals(testName)) {
                    Loggers.SERVER.info("Matched: " + el.getAttribute("id").getValue() + " / " + testName);
                    return el.getChild("session").getValue();
                }
            }
        } catch (JDOMException e) {
            throw new IOException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return null;
    }
}
