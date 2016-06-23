package com.browserstack.automate.ci.teamcity.analytics;

import com.browserstack.automate.ci.common.analytics.Analytics;
import com.browserstack.automate.ci.common.analytics.AnalyticsDataProvider.ProviderName;
import com.browserstack.automate.ci.teamcity.BrowserStackParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateSessionController extends BaseController {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Analytics analytics;

    public AutomateSessionController(@NotNull SBuildServer server,
                                     @NotNull WebControllerManager manager) {
        super(server);
        manager.registerController(BrowserStackParameters.SESSIONS_CONTROLLER_PATH, this);
        analytics = Analytics.getAnalytics(ProviderName.TEAMCITY);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        result.put("success", "false");
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        String loadTime = request.getParameter("loadTime");
        Loggers.SERVER.info("AutomateSessionController: trackIframeLoad: received loadTime: " + loadTime);

        if (StringUtil.isNotEmpty(loadTime) && analytics != null) {
            try {
                analytics.trackIframeLoad(Integer.parseInt(loadTime));
                result.put("success", "true");
                Loggers.SERVER.info("AutomateSessionController: trackIframeLoad: done");
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(mapper.writeValueAsString(result));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return null;
    }
}
