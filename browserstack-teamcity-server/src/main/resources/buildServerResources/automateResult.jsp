<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@include file="/include.jsp"%>
<%@ page import="com.browserstack.automate.ci.teamcity.BrowserStackParameters" %>

<style type="text/css">
.automate-result {
    background: url('//www.browserstack.com/images/layout/ajax-loader-main.gif') 50% 15% no-repeat;
}
</style>

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
                    <td class="test-status" style="text-align: left; vertical-align: top">${test.getChild("status").getText()}</td>
                    <td class="nameT" style="text-align: left; vertical-align: top">
                        ${test.getChild("package").getText()}. ${test.getChild("class").getText()}.
                        <a class="session-link" data-session="${test.getChild("session").getText()}" href="<%= request.getAttribute("javax.servlet.forward.request_uri") %>?<%= request.getQueryString() %>&session=${test.getChild("session").getText()}">
                            ${test.getAttribute("name").getValue()}
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
     </table>

    <script type="text/javascript">
    // disabled; to enable, rename class selector to .session-link
    $j('.session-link1').on("click", function (event) {
         event.preventDefault();

         var el = $j(this);
         var resultFrame = el.next('.automate-result');
         if (resultFrame && resultFrame.length) {
              resultFrame.remove();
         } else {
             el.after('<div class="automate-result-loading">Loading...</div>');

             $j.ajax({
                 url: '<%= BrowserStackParameters.SESSIONS_CONTROLLER_PATH %>?session=' + el.data("session") + '&<%= request.getQueryString() %>',
                 cache: false,
                 dataType: 'json',
                 success: function (res) {
                    if (res) {
                        $j('.automate-result').remove();
                        el.after('<iframe class="automate-result" src="' + res.public_url + '" frameborder="0" style="width: 100%; height: 800px; border: 0;"></iframe>');
                    }
                 },
                 error: function (xhr) {
                    if (xhr.responseJSON) {
                        el.after('<div class="automate-result">' + xhr.responseJSON.error + '</div>');
                    }
                 },
                 complete: function () {
                    $j('.automate-result-loading').remove();
                 }
             });
         }
    });
    </script>
</c:if>
<c:if test="${not empty session}">
    <c:if test="${not empty resultsUrl}">
        <a href="${resultsUrl}">Back to test results</a>
        <a href="${session.browserUrl}" target="_blank" style="margin-left: 20px; float: right;">View this on BrowserStack Automate Dashboard</a><br/>
    </c:if>
    <br/>
    <iframe class="automate-result" src="${session.publicUrl}" frameborder="0" style="width: 100%; height: 800px; border: 1px solid #000;"></iframe>
</c:if>
<c:if test="${not empty error}">
    <h3>${error}</h3>
</c:if>