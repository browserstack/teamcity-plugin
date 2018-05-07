<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@include file="/include.jsp"%>

<c:if test="${not empty tests}">
    <jsp:useBean id="tests" type="java.util.List" scope="request"/>
    <h2>Selenium/JSTest Results</h2><br/>
    <table class="testList">
        <thead>
            <tr>
                <th class="test-status" style="text-align: left">Status</th>
                <th style="text-align: left; vertical-align: top">Test name</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${tests}" var="test">
                <tr>
                    <td class="test-status" style="text-align: left; vertical-align: top">${test.getAttribute("status").getValue()}</td>
                    <td class="nameT" style="text-align: left; vertical-align: top">
                        ${test.getAttribute("package").getValue()}. ${test.getAttribute("class").getValue()}.
                        <a class="session-link" data-session="${test.getChild("session").getText()}" href="<%= request.getAttribute("javax.servlet.forward.request_uri") %>?<%= request.getQueryString() %>&session=${test.getChild("session").getText()}&projectType=${test.getChild("projectType").getText()}">
                            ${test.getAttribute("testname").getValue()}
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
     </table>
</c:if>
<c:if test="${not empty session}">
    <%@ page import="com.browserstack.automate.ci.teamcity.BrowserStackParameters" %>

    <style type="text/css">
    #automate-result {
        background: url('//www.browserstack.com/images/layout/ajax-loader-main.gif') 50% 15% no-repeat;
        width: 100%;
        height: 800px;
        border: 1px solid #bbb;
    }
    </style>

    <c:if test="${not empty resultsUrl}">
        <a href="${resultsUrl}">Back to test results</a>
        <a href="${session.browserUrl}" target="_blank" style="margin-left: 20px; float: right;">View this on BrowserStack Dashboard</a><br/>
    </c:if>
    <br/>
    <iframe id="automate-result" src="${session.publicUrl}" frameborder="0"></iframe>
    <script type="text/javascript">
    //<![CDATA[
    var startTime = (new Date).valueOf();

    $j(function () {
        $j('#automate-result').on('load', function () {
            var loadTime = ((new Date).valueOf() - startTime);
            $j.ajax({
                url: '<%= BrowserStackParameters.SESSIONS_CONTROLLER_PATH %>?loadTime=' + loadTime + '&<%= request.getQueryString() %>',
                cache: false,
                dataType: 'json'
            });
        });
    });
    //]]>
</script>
</c:if>
<c:if test="${not empty error}">
    <h3>${error}</h3>
</c:if>