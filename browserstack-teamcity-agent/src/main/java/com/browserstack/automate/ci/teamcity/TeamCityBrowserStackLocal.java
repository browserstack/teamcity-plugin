package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.ci.common.local.BrowserStackLocal;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.BuildProgressLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TeamCityBrowserStackLocal extends BrowserStackLocal {
    private final BuildProgressLogger buildLogger;

    TeamCityBrowserStackLocal(String options, BuildProgressLogger buildLogger) {
        super(options);
        this.buildLogger = buildLogger;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected LocalProcess executeCommand(String binPath, List<String> arguments) throws IOException {
        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(binPath);
        cmd.addParameters(arguments);

        final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
        final int exitCode = result.getExitCode();
        final String error;

        if (result.getException() != null || exitCode != 0) {
            buildLogger.message(("Failed to find " + binPath + ". Exit code: " + exitCode + "\n " + result.getStdout() + "\n" + result.getStderr()).trim());
            error = "Failed to launch BrowserStack Local: " + exitCode;
        } else {
            error = result.getStderr();
        }

        final String output = result.getStdout();
        return new LocalProcess() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(output.getBytes());
            }

            @Override
            public InputStream getErrorStream() {
                return new ByteArrayInputStream(error.getBytes());
            }

            @Override
            public int waitFor() throws Exception {
                return exitCode;
            }
        };
    }
}
