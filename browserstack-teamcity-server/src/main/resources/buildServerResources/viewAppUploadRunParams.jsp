<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="com.browserstack.automate.ci.teamcity.beans.AppUploadConstantsBean"/>

<div class="parameter">
  App path: <strong><props:displayValue name="${constants.filePath}" emptyValue="not specified"/></strong>
</div>


