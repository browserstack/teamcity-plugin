package com.browserstack.automate.ci.common.analytics;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsRequest;
import com.brsanthu.googleanalytics.TimingHit;

import java.io.IOException;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class Analytics {

    protected static final String DEFAULT_CLIENT_ID = "unknown-client";

    protected static final GoogleAnalytics ga = new GoogleAnalytics("UA-79358556-2");

    private String clientId;

    private boolean isEnabled;

    private VersionTracker versionTracker;

    private final AnalyticsDataProvider dataProvider;

    private static Analytics analyticsInstance;

    public Analytics(AnalyticsDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.versionTracker = new VersionTracker(dataProvider.getRootDir());
        this.isEnabled = true;

        trackInstall();
    }

    protected void postAsync(GoogleAnalyticsRequest request) {
        if (isEnabled && dataProvider.isEnabled()) {
            ga.postAsync(request);
        }
    }

    protected EventHit newEventHit(String category, String action) {
        EventHit eventHit = new EventHit(category, action);
        attachGlobalProperties(eventHit);
        return eventHit;
    }

    protected TimingHit newTimingHit(String category, String variable, int time) {
        TimingHit timingHit = new TimingHit()
                .userTimingCategory(category)
                .userTimingVariableName(variable)
                .userTimingTime(time);
        attachGlobalProperties(timingHit);
        return timingHit;
    }

    protected void attachGlobalProperties(GoogleAnalyticsRequest gaRequest) {
        gaRequest.clientId((clientId != null) ? clientId : getClientId());
        gaRequest.applicationName(dataProvider.getApplicationName() + "-" + dataProvider.getApplicationVersion());
        gaRequest.applicationId(dataProvider.getPluginName());
        gaRequest.applicationVersion(dataProvider.getPluginVersion());
    }

    public void trackInstall() {
        clientId = getClientId();

        String version = dataProvider.getPluginVersion();
        try {
            if (versionTracker.init(version)) {
                postAsync(newEventHit("install", "install"));
            } else if (versionTracker.updateVersion(version)) {
                postAsync(newEventHit("install", "update"));
            }
        } catch (IOException e) {
            System.out.println("Failed to track install: " + e.getMessage());
        }
    }

    public void trackBuildRun(boolean localEnabled, boolean localPathSet,
                              boolean localOptionsSet, boolean isReportEnabled) {
        EventHit eventHit = newEventHit((localEnabled ? "with" : "without") + "Local", "buildRun");
        if (isReportEnabled) {
            eventHit.eventLabel("embedTrue");
        } else {
            eventHit.eventLabel("embedFalse");
        }

        if (localPathSet) {
            eventHit.customDimension(1, "withLocalPath");
        } else {
            eventHit.customDimension(2, "withoutLocalPath");
        }

        if (localOptionsSet) {
            eventHit.customDimension(3, "withLocalOptions");
        } else {
            eventHit.customDimension(4, "withoutLocalOptions");
        }

        postAsync(eventHit);
    }

    public void trackIframeRequest() {
        postAsync(newEventHit("iframeRequested", "iframe"));
    }

    public void trackIframeLoad(int loadTime) {
        postAsync(newTimingHit("iframeLoadTimeMs", "iframe", loadTime));
    }

    protected String getClientId() {
        if (versionTracker != null) {
            try {
                return versionTracker.getClientId();
            } catch (IOException e) {
                return DEFAULT_CLIENT_ID;
            }
        }

        return null;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public static Analytics createInstance(final AnalyticsDataProvider dataProvider) {
        analyticsInstance = new Analytics(dataProvider);
        return analyticsInstance;
    }

    public static Analytics getInstance() {
        return analyticsInstance;
    }
}
