package com.browserstack.automate.ci.teamcity.ui;

import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.util.ParserUtil;
import jetbrains.buildServer.log.Loggers;
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
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
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
                long testIndex = getTestIndex(testRuns, testRun);

                try {
                    String sessionId = findSessionId(testRun, testIndex);
                    if (StringUtils.isNotBlank(sessionId)) {
                        model.put("session", sessionId);
                    }
                } catch (IOException e) {
                    Loggers.SERVER.info(e.getMessage());
                    model.put("error", e.getMessage());
                }
            }
        }
    }

    private int getTestIndex(final List<STestRun> testRuns, final STestRun testRun) {
        // TODO: This iterates through all test results with the same name as the test case being queries
        // and figure out index for the test case w.r.t other tests with the same name (but different parameters)

        String currentTestCaseName = ParserUtil.getTestName(testRun);
        int testIndex = -1;

        for (STestRun tr : testRuns) {
            String testCaseName = ParserUtil.getTestName(tr);
            if (testCaseName.equals(currentTestCaseName)) {
                testIndex++;

                if (tr.equals(testRun)) {
                    break;
                }
            }
        }

        return testIndex;
    }

    @Override
    public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return true;
    }

    @SuppressWarnings("unchecked")
    private static String findSessionId(final STestRun testRun, final long testIndex) throws IOException {
        String testNameFull = ParserUtil.getTestName(testRun);
        final String testId = String.format("%s{%d}", testNameFull, Math.max(0, testIndex));
        BuildArtifacts buildArtifacts = testRun.getBuild().getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
        final String[] sessions = new String[]{null};

        buildArtifacts.iterateArtifacts(new BuildArtifacts.BuildArtifactsProcessor() {
            @NotNull
            @Override
            public Continuation processBuildArtifact(@NotNull BuildArtifact artifact) {
                // TODO: Fix logic of picking up files as artifacts
                if (artifact.isFile() && artifact.getRelativePath().contains(BrowserStackParameters.BROWSERSTACK_ARTIFACT_DIR)) {
                    InputStream inputStream = null;

                    try {
                        inputStream = artifact.getInputStream();
                        List<Element> elements = ParserUtil.parseResultFile(inputStream);
                        if (elements != null) {
                            for (Element elem : elements) {
                                if (elem.getAttribute("id") != null && testId.equals(elem.getAttribute("id").getValue())) {
                                    if (elem.getChild("session") != null) {
                                        sessions[0] = elem.getChild("session").getText();
                                        return Continuation.BREAK;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Loggers.SERVER.warn("Failed to parse artifact: " + e.getMessage());
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }

                return Continuation.CONTINUE;
            }
        });

        return sessions[0];
    }
}
