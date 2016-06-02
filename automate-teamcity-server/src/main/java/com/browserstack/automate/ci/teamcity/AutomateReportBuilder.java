package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.common.AutomateTestCase;
import com.browserstack.automate.ci.common.Utils;
import com.browserstack.automate.model.Session;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.tests.TestName;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateReportBuilder {

    private final List<STestRun> allTests;
    private final List<AutomateTestCase> capturedTests;

    public AutomateReportBuilder(List<STestRun> allTests, List<AutomateTestCase> capturedTests) {
        this.allTests = allTests;
        this.capturedTests = capturedTests;
    }

    public void generateReportArtifact(File dir) throws IOException {
        File artifactFile = new File(dir, BrowserStackParameters.ARTIFACT_FILE_NAME);
        Element company = new Element("tests");
        Document doc = new Document(company);
        doc.setRootElement(company);

        Element rootElement = doc.getRootElement();
        int testCount = 0;
        int sessionCount = 0;
        Map<String, Long> testCaseIndices = new HashMap<String, Long>();

        for (STestRun test : allTests) {
            testCount++;

            String srcTestName = test.getTest().getName().getNameWithoutParameters();
            String srcTestHash = Utils.generateHash(srcTestName);
            Long testIndex = testCaseIndices.containsKey(srcTestHash) ? testCaseIndices.get(srcTestHash) : -1L;
            testCaseIndices.put(srcTestHash, ++testIndex);

            for (AutomateTestCase automateTestCase : capturedTests) {
                if (testIndex == automateTestCase.testIndex && srcTestHash.equals(automateTestCase.testHash)) {
                    Loggers.SERVER.info("MATCH: " + srcTestHash);
                    rootElement.addContent(newTestElement(automateTestCase, test));
                    sessionCount++;
                    break;
                } else {
                    Loggers.SERVER.info("MATCH FAILED: " + test.getTest().getName().getAsString() + " | " +
                            srcTestHash + " != " + automateTestCase.testHash);
                }
            }
        }

        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriter(artifactFile));

        Loggers.SERVER.info("Tests captured: " + testCount);
        Loggers.SERVER.info("Tests matched: " + sessionCount);
    }

    private Element newTestElement(AutomateTestCase testCase, STestRun test) {
        TestName testName = test.getTest().getName();
        Element element = new Element("test");
        element.setAttribute(new Attribute("id", testName.getAsString()));
        element.setAttribute(new Attribute("name", testName.getTestNameWithParameters()));
        element.addContent(new Element("session").setText(testCase.sessionId));
        element.addContent(new Element("package").setText(testName.getPackageName()));
        element.addContent(new Element("class").setText(testName.getClassName()));
        element.addContent(new Element("method").setText(testName.getTestMethodName()));
        element.addContent(new Element("order").setText("" + test.getOrderId()));
        element.addContent(new Element("duration").setText("" + test.getDuration()));
        element.addContent(new Element("build").setText("" + test.getBuild().getBuildNumber()));
        element.addContent(new Element("status").setText("" + test.getStatusText()));
        return element;
    }
}
