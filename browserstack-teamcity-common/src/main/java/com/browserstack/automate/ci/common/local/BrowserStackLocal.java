package com.browserstack.automate.ci.common.local;

import com.browserstack.local.Local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class BrowserStackLocal extends Local {
    private static final String OPTION_LOCAL_IDENTIFIER = "localIdentifier";

    private final List<String> options;
    private String localIdentifier;

    public BrowserStackLocal(String options) {
        this.options = processLocalOptions((options != null) ? options.trim() : "");
    }

    protected abstract LocalProcess executeCommand(String binPath, List<String> command) throws IOException;

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected LocalProcess runCommand(List<String> command) throws IOException {
        if (command.size() < 2) {
            return super.runCommand(command);
        }

        List<String> cmdArgs = command.subList(1, command.size());
        DaemonAction daemonAction = DaemonAction.detectDaemonAction(stringifyCommand(command));
        if (daemonAction != null) {
            cmdArgs.addAll(options);
        }

        return executeCommand(command.get(0), cmdArgs);
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    private List<String> processLocalOptions(final String optionsString) {
        String[] args = optionsString.split("\\s+");
        int localIdPos = 0;
        List<String> options = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains(OPTION_LOCAL_IDENTIFIER)) {
                localIdPos = i;
                if (i < args.length - 1 && args[i + 1] != null && !args[i + 1].startsWith("-")) {
                    localIdentifier = args[i + 1];
                    if (localIdentifier != null && localIdentifier.trim().length() > 0) {
                        return Arrays.asList(args);
                    }

                    // skip next, since already processed
                    i += 1;
                }

                continue;
            }

            String arg = args[i].trim();
            if (arg.length() > 0) {
                options.add(arg);
            }
        }

        localIdentifier = UUID.randomUUID().toString().replaceAll("\\-", "");
        options.add(localIdPos, localIdentifier);
        options.add(localIdPos, "-" + OPTION_LOCAL_IDENTIFIER);
        return options;
    }

    private static String stringifyCommand(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String arg : command) {
            sb.append(arg.trim()).append(" ");
        }

        return sb.toString().trim();
    }
}
