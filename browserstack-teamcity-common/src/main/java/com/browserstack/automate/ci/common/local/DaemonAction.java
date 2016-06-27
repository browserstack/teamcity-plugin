package com.browserstack.automate.ci.common.local;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public enum DaemonAction {
    START, STOP;

    public static DaemonAction detectDaemonAction(String action) {
        if (action != null) {
            if (action.contains(" -d start")) {
                return DaemonAction.START;
            } else if (action.contains(" -d stop")) {
                return DaemonAction.STOP;
            }
        }

        return null;
    }
}
