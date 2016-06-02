package com.browserstack.automate.ci.teamcity;

import com.browserstack.local.Local;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.BuildProgressLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BrowserStackLocal extends Local {
    private static final String OPTION_LOCAL_IDENTIFIER = "localIdentifier";

    private final BuildProgressLogger buildLogger;
    private final String[] arguments;
    private String localIdentifier;

    BrowserStackLocal(BuildProgressLogger buildLogger, String options) {
        this.buildLogger = buildLogger;
        this.arguments = processLocalArguments((options != null) ? options.trim() : "");
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected LocalProcess runCommand(List<String> command) throws IOException {
        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(command.get(0));
        cmd.addParameters(command.subList(1, command.size()));

        DaemonAction daemonAction = detectDaemonAction(command);
        if (daemonAction != null) {
            buildLogger.message("Adding args: " + Arrays.toString(arguments));
            cmd.addParameters(arguments);
        }

        buildLogger.message("Executing: " + cmd.toString());

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

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    private String[] processLocalArguments(final String argString) {
        String[] args = argString.split("\\s+");
        int localIdPos = 0;
        List<String> arguments = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains(OPTION_LOCAL_IDENTIFIER)) {
                localIdPos = i;
                if (i < args.length - 1 && args[i + 1] != null && !args[i + 1].startsWith("-")) {
                    localIdentifier = args[i + 1];
                    if (localIdentifier != null && localIdentifier.trim().length() > 0) {
                        return args;
                    }

                    // skip next, since already processed
                    i += 1;
                }

                continue;
            }

            String arg = args[i].trim();
            if (arg.length() > 0) {
                arguments.add(arg);
            }
        }

        localIdentifier = UUID.randomUUID().toString().replaceAll("\\-", "");
        arguments.add(localIdPos, localIdentifier);
        arguments.add(localIdPos, "-" + OPTION_LOCAL_IDENTIFIER);
        return arguments.toArray(new String[]{});
    }

    private static DaemonAction detectDaemonAction(List<String> command) {
        if (command.size() > 2) {
            String action = command.get(2).toLowerCase();
            if (action.equals("start")) {
                return DaemonAction.START;
            } else if (action.equals("stop")) {
                return DaemonAction.STOP;
            }
        }

        return null;
    }

    private enum DaemonAction {
        START, STOP
    }
}
