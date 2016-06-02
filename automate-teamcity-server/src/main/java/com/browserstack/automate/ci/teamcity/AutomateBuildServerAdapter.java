package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.common.AutomateTestCase;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.BuildStatisticsOptions;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutomateBuildServerAdapter extends BuildServerAdapter {

    public AutomateBuildServerAdapter(@NotNull final EventDispatcher<BuildServerListener> buildServerListener) {
        buildServerListener.addListener(this);
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        super.buildFinished(build);
        SBuildFeatureDescriptor featureDescriptor = AutomateBuildFeature.findFeatureDescriptor(build);
        if (featureDescriptor == null) {
            Loggers.SERVER.error("Build feature missing");
            return;
        }

        List<AutomateTestCase> capturedTestCases = new ArrayList<AutomateTestCase>();
        Iterator<LogMessage> messagesIterator = build.getBuildLog().getMessagesIterator();
        while (messagesIterator.hasNext()) {
            AutomateTestCase testCase = AutomateTestCase.parse(messagesIterator.next().getText());
            if (testCase != null) {
                capturedTestCases.add(testCase);
            }
        }

        List<STestRun> allTests = build.getBuildStatistics(BuildStatisticsOptions.ALL_TESTS_NO_DETAILS).getAllTests();
        AutomateReportBuilder reportBuilder = new AutomateReportBuilder(allTests, capturedTestCases);

        try {
            File outDir = FileUtil.createDir(new File(build.getArtifactsDirectory(), BrowserStackParameters.ARTIFACT_DIR));
            if (outDir.exists() && outDir.isDirectory()) {
                reportBuilder.generateReportArtifact(outDir);
            } else {
                Loggers.SERVER.error("ERROR: Failed to create artifact directory at: " + outDir.getAbsolutePath());
            }
        } catch (IOException e) {
            Loggers.SERVER.error("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
