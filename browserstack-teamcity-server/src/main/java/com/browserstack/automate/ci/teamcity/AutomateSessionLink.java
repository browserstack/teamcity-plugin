package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.common.AutomateTestCase;
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

        return findSessionId(testRun.getFullText());
    }

    private static String findSessionId(final String testOutput) {
        if (testOutput != null) {
            String[] testOutputLines = testOutput.split("(\\r)*\\n");

            for (String line : testOutputLines) {
                AutomateTestCase automateTestCase = AutomateTestCase.parse(line);
                if (automateTestCase != null) {
                    /*
                    String resultUrl = request.getRequestURI();
                    if (resultUrl != null) {
                        resultUrl += "?" + request.getQueryString()
                                .replaceAll("&tab=[^&]+", "&tab=" + BrowserStackParameters.AUTOMATE_NAMESPACE);
                        resultUrl += "&session=" + automateTestCase.sessionId;
                        model.put("resultUrl", resultUrl);
                    }
                    */
                    // http://localhost:8111/viewLog.html?buildId=95&buildTypeId=AutomateJunitCiSample2_CiSample2&tab=buildResultsDiv
                    // http://localhost:8111/change/testDetails.html?testNameId=821971475507187665&builds=95.&projectId=AutomateJunitCiSample2&session=c8928d782f6aed23f365ad3e5d0d62483a5bb3e2
                    // http://localhost:8111/viewLog.html?buildId=95&buildTypeId=AutomateJunitCiSample2_CiSample2&tab=automate-results&session=c8928d782f6aed23f365ad3e5d0d62483a5bb3e2
                    Loggers.SERVER.info("automateTestCase: " + automateTestCase.sessionId);
                    return automateTestCase.sessionId;
                }
            }
        }

        return null;
    }
}
