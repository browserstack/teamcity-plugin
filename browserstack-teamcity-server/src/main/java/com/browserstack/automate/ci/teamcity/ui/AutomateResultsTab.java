package com.browserstack.automate.ci.teamcity.ui;

import com.browserstack.appautomate.AppAutomateClient;
import com.browserstack.automate.AutomateClient;
import com.browserstack.automate.ci.common.analytics.Analytics;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters.BrowserStackProjectType;
import com.browserstack.automate.ci.teamcity.config.AutomateBuildFeature;
import com.browserstack.automate.ci.teamcity.util.ParserUtil;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.exception.BrowserStackException;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Controller for the "BrowserStack" tab displayed in the UI with test results.
 * This handles both views: the list of test cases and the iframe for a single test.
 *
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateResultsTab extends ViewLogTab {

  private static final String TAB_TITLE = "BrowserStack";

  private static final Pattern PATTERN_PARAM_SESSION = Pattern.compile("&session=.*");

  private final Analytics analytics;

  /**
   * Creates and registers tab for Build Results pages
   *
   * @param pagePlaces used to register the tab
   * @param server server object
   * @param descriptor plugin descriptor
   */
  public AutomateResultsTab(@NotNull PagePlaces pagePlaces, @NotNull final SBuildServer server,
      @NotNull PluginDescriptor descriptor) {
    super(TAB_TITLE, BrowserStackParameters.AUTOMATE_NAMESPACE, pagePlaces, server);
    setPlaceId(PlaceId.BUILD_RESULTS_TAB);
    setIncludeUrl(descriptor.getPluginResourcesPath("automateResult.jsp"));
    register();

    analytics = Analytics.getInstance();
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
    String sessionId = request.getParameter("session");
    String projectType = request.getParameter("projectType"); // Either null, or AUTOMATE or APP_AUTOMATE.

    // This is for backward compatibility, as earlier only AUTOMATE was supported.
    if (StringUtils.isBlank(projectType)) {
      projectType = BrowserStackProjectType.AUTOMATE;
    }

    if (StringUtils.isNotBlank(sessionId)) {
      Loggers.SERVER.info("Rendering session: " + sessionId);
      // render one test session
      fillModelSession(sessionId, model, request, build, projectType);
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

    List<File> reportFiles = new ArrayList<File>();
    FileUtil.collectMatchedFiles(build.getArtifactsDirectory(),
            Pattern.compile(FileUtil.convertAntToRegexp(BrowserStackParameters.ARTIFACT_LOCATION_PATTERN)),
            reportFiles);

    Loggers.SERVER.info("AutomateResultsTab.isAvailable: done: " + !reportFiles.isEmpty());
    return !reportFiles.isEmpty();
  }

  private void fillModelSession(final String sessionId, final Map<String, Object> model,
      final HttpServletRequest request, final SBuild build, final String projectType) {
    if (analytics != null) {
      analytics.trackIframeRequest();
      Loggers.SERVER.info("AutomateResultsTab: trackIframeRequest");
    }

    BrowserStackClient browserStackClient = newBrowserStackClient(build, projectType);

    if (browserStackClient != null) {
      try {
        model.put("session", browserStackClient.getSession(sessionId));
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
      } catch (BrowserStackException e) {
        model.put("error", e.getMessage());
        Loggers.SERVER.info("Session fetch failed: " + sessionId);
      }
    } else {
      model.put("error", "Failed to configure AutomateClient");
      Loggers.SERVER.info("browserStackClient == null");
    }
  }

  private void fillModelSessionList(final Map<String, Object> model, final SBuild build) {
    if (analytics != null) {
      analytics.trackReportView();
      Loggers.SERVER.info("AutomateResultsTab: trackReportView");
    }

    BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
    BuildStatistics buildStatistics = build.getBuildStatistics(BuildStatisticsOptions.ALL_TESTS_NO_DETAILS);
    final Map<String, STestRun> testResultMap = ParserUtil.processTestResults(buildStatistics.getAllTests());

    final List<Element> testResults = new ArrayList<Element>();
    buildArtifacts.iterateArtifacts(new BuildArtifacts.BuildArtifactsProcessor() {
      @NotNull
      @Override
      public Continuation processBuildArtifact(@NotNull BuildArtifact artifact) {
        // TODO: Fix logic of picking up files as artifacts
        if (artifact.isFile() && artifact.getRelativePath().contains(BrowserStackParameters.BROWSERSTACK_ARTIFACT_DIR)) {
          InputStream inputStream = null;

          try {
            inputStream = artifact.getInputStream();
            List<Element> results = ParserUtil.parseResultFile(inputStream);
            if (results != null) {
              for (Element elem : results) {
                String testCaseId = elem.getAttribute("id").getValue();

                // Checks if test case in our report exists in test report XMLs parsed by TeamCity
                if (testCaseId != null && testResultMap.containsKey(testCaseId)) {
                  STestRun testRun = testResultMap.get(testCaseId);
                  elem.setAttribute("status", testRun.getStatusText());

                  // Use test name as recorded by Teamcity's result parser
                  elem.setAttribute("testname", testRun.getTest().getName().getTestNameWithParameters());
                  testResults.add(elem);
                }
              }
            }
          } catch (IOException e) {
            model.put("error", e.getMessage());
          } catch (JDOMException e) {
            model.put("error", e.getMessage());
          } finally {
            IOUtils.closeQuietly(inputStream);
          }
        }

        return Continuation.CONTINUE;
      }
    });

    model.put("tests", testResults.isEmpty() ? Collections.emptyList() : testResults);
  }

  /**
   * Creates a new browserstack client according to the projectType(AUTOMATE or APP_AUTOMATE).
   * @param build build object
   * @param projectType browserstack project type, will be either AUTOMATE or APP_AUTOMATE.
   * @return BrowserStackClient , Client for calling BrowserStack's apis.
   */
  public static BrowserStackClient newBrowserStackClient(final SBuild build, String projectType) {
    SBuildFeatureDescriptor featureDescriptor = AutomateBuildFeature.findFeatureDescriptor(build);
    if (featureDescriptor != null) {
      Map<String, String> params = featureDescriptor.getParameters();
      String username = params.get(BrowserStackParameters.EnvVars.BROWSERSTACK_USERNAME);
      String accessKey = params.get(BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESS_KEY);

      if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(accessKey)) {
        if (projectType.equals(BrowserStackProjectType.APP_AUTOMATE)) {
          return new AppAutomateClient(username, accessKey);
        } else {
          return new AutomateClient(username, accessKey);
        }
      }
    }
    return null;
  }
}
