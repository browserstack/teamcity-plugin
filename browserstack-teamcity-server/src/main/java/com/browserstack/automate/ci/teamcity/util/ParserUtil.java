package com.browserstack.automate.ci.teamcity.util;

import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.tests.TestName;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class ParserUtil {

    private static final String PACKAGE_DEFAULT = "(root)";

    public static String getFullClassName(final STestRun testRun) {
        TestName testName = testRun.getTest().getName();
        return String.format("%s.%s", testName.hasPackage() ? testName.getPackageName() : PACKAGE_DEFAULT, testName.getClassName());
    }

    public static String getTestName(final STestRun testRun) {
        return String.format("%s.%s", getFullClassName(testRun), testRun.getTest().getName().getTestMethodName());
    }

    @SuppressWarnings("unchecked")
    public static List<Element> parseResultFile(final InputStream inputStream) throws IOException, JDOMException {
        String artifactData = IOUtils.toString(inputStream);
        if (artifactData != null) {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new ByteArrayInputStream(artifactData.getBytes()));
            return document.getRootElement().getChildren("testcase");
        }

        return null;
    }

    public static Map<String, STestRun> processTestResults(final List<STestRun> allTests) {
        Map<String, STestRun> testStatusMap = new HashMap<String, STestRun>();
        Map<String, Long> testCaseIndices = new HashMap<String, Long>();

        for (STestRun testRun : allTests) {
            String testCaseName = ParserUtil.getTestName(testRun);
            Long testIndex = testCaseIndices.containsKey(testCaseName) ? testCaseIndices.get(testCaseName) : -1L;
            testCaseIndices.put(testCaseName, ++testIndex);

            String testId = String.format("%s{%d}", testCaseName, testIndex);
            if (!testStatusMap.containsKey(testId)) {
                testStatusMap.put(testId, testRun);
            }
        }

        testCaseIndices.clear();
        return testStatusMap;
    }
}
