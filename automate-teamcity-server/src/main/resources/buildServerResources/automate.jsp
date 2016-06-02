<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@include file="/include.jsp"%>
<%@ page import="com.browserstack.automate.ci.teamcity.BrowserStackParameters" %>
<jsp:useBean id="tests" type="java.util.List" scope="request"/>

<c:if test="${not empty tests}">
    <table class="testList">
        <thead>
            <tr>
                <th class="test-status" style="text-align: left">Status</th>
                <th style="text-align: left; vertical-align: top">Test</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${tests}" var="test">
                <tr>
                    <td class="test-status" style="text-align: left; vertical-align: top">${test.getChild("status").getText()}</td>
                    <td class="nameT" style="text-align: left; vertical-align: top">
                        ${test.getChild("package").getText()}. ${test.getChild("class").getText()}.
                        <a class="session-link" data-session="${test.getChild("session").getText()}" href="#">
                            ${test.getAttribute("name").getValue()}
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
     </table>

    <script type="text/javascript">
    $j('.session-link').on("click", function (event) {
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
                        el.after('<iframe class="automate-result" src="' + res.public_url + '" style="width: 100%; height: 800px"></iframe>');
                    }
                 },
                 error: function (xhr) {
                    if (xhr.responseJSON) {
                        alert(JSON.stringify(xhr));
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
