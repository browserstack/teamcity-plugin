package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.common.AutomateTestCase;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

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

        List<AutomateTestCase> capturedTestCases = new ArrayList<AutomateTestCase>();
        Iterator<LogMessage> messagesIterator = build.getBuildLog().getMessagesIterator();
        while (messagesIterator.hasNext()) {
            AutomateTestCase testCase = AutomateTestCase.parse(messagesIterator.next().getText());
            if (testCase != null) {
                capturedTestCases.add(testCase);
            }
        }

        for (AutomateTestCase testCase : capturedTestCases) {
            System.out.println(">>>> found test case: " + testCase.sessionId + " | " + testCase.testFullPath);
        }

        List<STestRun> allTests = build.getBuildStatistics(BuildStatisticsOptions.ALL_TESTS_NO_DETAILS).getAllTests();
        for (STestRun test : allTests) {
            System.out.println(">> " + test.getTest().getName().getAsString());
        }
    }
}
