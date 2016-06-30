<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%@ page import="com.browserstack.automate.ci.teamcity.BrowserStackParameters" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<tr id="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_USER %>.container">
    <th><label for="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_USER %>">Username:</label></th>
    <td>
        <props:textProperty name="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_USER %>"/>

        <span class="smallNote">
            Set your BrowserStack username. You can get it from <a href="https://www.browserstack.com/accounts/settings" target="_blank">here</a>.
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.EnvVars.BROWSERSTACK_USER %>"/>
    </td>
</tr>
<tr id="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESSKEY %>.container">
    <th><label for="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESSKEY %>">Access Key:</label></th>
    <td>
        <props:textProperty name="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESSKEY %>"/>

        <span class="smallNote">
            Set your BrowserStack access key. You can get it from <a href="https://www.browserstack.com/accounts/settings" target="_blank">here</a>.
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.EnvVars.BROWSERSTACK_ACCESSKEY %>"/>
    </td>
</tr>
<tr id="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL %>.container">
    <th><label for="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL %>">Enable BrowserStack Local:</label></th>
    <td>
        <props:checkboxProperty name="<%= BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL %>"
            treatFalseValuesCorrectly="${true}"
            uncheckedValue="false"/>

        <span class="smallNote">
            Use BrowserStack Local to securely access internal servers on BrowserStack VMs and Mobile devices.<br/>
            <strong>Skip this if you are using BrowserStack Local bindings in your code.</strong><br/>
            <ul>
                <li><i>BROWSERSTACK_LOCAL</i> environment variable is set with value <i>true</i></li>
                <li><i>BROWSERSTACK_LOCAL_IDENTIFIER</i> environment variable contains the unique identifier set for the local testing connection</li>
            </ul>
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.EnvVars.BROWSERSTACK_LOCAL %>"/>
    </td>
</tr>
<tr id="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_PATH %>.container">
    <th><label for="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_PATH %>">BrowserStack Local Path:</label></th>
    <td>
        <props:textProperty name="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_PATH %>" className="longField"/>

        <span class="smallNote">
            If left blank BrowserStack Local binary will be downloaded at default location by bindings.
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.BROWSERSTACK_LOCAL_PATH %>"/>
    </td>
</tr>
<tr id="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS %>.container">
    <th><label for="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS %>">BrowserStack Local Options:</label></th>
    <td>
        <props:textProperty name="<%= BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS %>" className="longField"/>

        <span class="smallNote">
            Specify the command line options for BrowserStack Local binary. e.g: -force -forcelocal. See <a href="https://www.browserstack.com/local-testing#modifiers" target="_blank">this link</a> to know more about these options.
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.BROWSERSTACK_LOCAL_OPTIONS %>"/>
    </td>
</tr>
<tr id="<%= BrowserStackParameters.ENABLE_ANALYTICS %>.container">
    <th><label for="<%= BrowserStackParameters.ENABLE_ANALYTICS %>">Send build usage data to BrowserStack:</label></th>
    <td>
        <props:checkboxProperty name="<%= BrowserStackParameters.ENABLE_ANALYTICS %>"
            treatFalseValuesCorrectly="${true}"
            uncheckedValue="false"/>

        <span class="smallNote">
            If checked, usage statistics will be shared with BrowserStack.
        </span>
        <span class="error" id="error_<%= BrowserStackParameters.ENABLE_ANALYTICS %>"/>
    </td>
</tr>
