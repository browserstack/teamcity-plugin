package com.browserstack.automate.ci.teamcity;

import com.browserstack.local.Local;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.BuildProgressLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BrowserStackLocal extends Local {

    private final BuildProgressLogger buildLogger;

    BrowserStackLocal(BuildProgressLogger buildLogger) {
        this.buildLogger = buildLogger;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected LocalProcess runCommand(List<String> command) throws IOException {
        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(command.get(0));
        cmd.addParameters(command.subList(1, command.size()));
        // buildLogger.message("Executing: " + command.toString());

        final ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
        final int exitCode = result.getExitCode();
        final String error;

        if (result.getException() != null || exitCode != 0) {
            buildLogger.message(("Failed to find " + command.get(0) + ". Exit code: " + exitCode + "\n " + result.getStdout() + "\n" + result.getStderr()).trim());
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
