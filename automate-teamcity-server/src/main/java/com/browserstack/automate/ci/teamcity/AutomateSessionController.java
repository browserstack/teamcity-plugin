package com.browserstack.automate.ci.teamcity;

import com.browserstack.automate.AutomateClient;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.SessionNotFound;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class AutomateSessionController extends BaseController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AutomateSessionController(@NotNull SBuildServer server, @NotNull WebControllerManager manager) {
        super(server);
        manager.registerController(BrowserStackParameters.SESSIONS_CONTROLLER_PATH, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        String result;
        response.setContentType("application/json");

        SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
        if (build == null) {
            result = newError("Build not found");
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        } else {
            String sessionId = request.getParameter("session");
            Loggers.SERVER.info("sessionId: " + sessionId);

            if (StringUtils.isNotBlank(sessionId)) {
                result = loadSession(build, sessionId, response);
            } else {
                result = newError("Invalid session");
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
            }
        }

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(result);
        } catch (IOException e) {
            newError(e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return null;
    }

    private String loadSession(final SBuild build, final String sessionId, final HttpServletResponse response) {
        String result;
        AutomateClient automateClient = newAutomateClient(build);

        if (automateClient != null) {
            try {
                result = objectMapper.writeValueAsString(automateClient.getSession(sessionId));
                response.setStatus(HttpStatus.SC_OK);
            } catch (SessionNotFound sessionNotFound) {
                result = newError(sessionNotFound.getMessage());
                response.setStatus(HttpStatus.SC_NOT_FOUND);
            } catch (IOException e) {
                result = newError("Failed to load session: " + e.getMessage());
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
            } catch (AutomateException e) {
                result = newError(e.getMessage());
                response.setStatus(HttpStatus.SC_UNPROCESSABLE_ENTITY);
            }
        } else {
            result = newError("Failed to configure AutomateClient");
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return result;
    }

    public static AutomateClient newAutomateClient(final SBuild build) {
        SBuildFeatureDescriptor featureDescriptor = AutomateBuildFeature.findFeatureDescriptor(build);
        if (featureDescriptor != null) {
            Map<String, String> params = featureDescriptor.getParameters();
            String username = params.get(BrowserStackParameters.EnvVars.BROWSERSTACK_USER);
            String accessKey = params.get(BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESSKEY);

            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(accessKey)) {
                return new AutomateClient(username, accessKey);
            }
        }

        return null;
    }

    private String newError(String errorMessage) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", errorMessage);

        try {
            return objectMapper.writeValueAsString(errorJson);
        } catch (JsonProcessingException e) {
            // really?
            return "{ \"error\": \"" + errorMessage + "\" }";
        }
    }
}
