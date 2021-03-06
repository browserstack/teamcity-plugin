<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@include file="/include.jsp"%>
<%@ page import="com.browserstack.automate.ci.teamcity.BrowserStackParameters" %>

<c:if test="${not empty session}">
    <div style="padding: 10;">
        <h3>BrowserStack</h3>
        <a class="session-link" href="#" target="_self">View BrowserStack report of this test</a>

        <script>
        <!-- JSP request page: /change/testDetails.html -->

        $j(function () {
            $j('.session-link').each(function () {
                var currentUrl = window.location.href.replace(/([&|\?])tab=[^&]+/, '$1tab=<%= BrowserStackParameters.AUTOMATE_NAMESPACE %>');
                if (currentUrl.indexOf('automate-result') !== -1) {
                    currentUrl += '&session=${session}'
                    $j(this).attr('href', currentUrl);
                }
            });
        });

        </script>
    </div>
</c:if>
<c:if test="${not empty error}">
    <div style="padding: 10;">
        <h3>BrowserStack</h3>
        <p>${error}</p>
    </div>
</c:if>