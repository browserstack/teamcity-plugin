<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="com.browserstack.automate.ci.teamcity.beans.AppUploadConstantsBean"/>

<l:settingsGroup title="BrowserStack App upload section">

  <tr id="bStackPathSection">
    <th><label for="${constants.filePath}">App path: <l:star/></label></th>
    <td><props:textProperty name="${constants.filePath}" className="longField"/>
      <span class="error" id="error_${constants.filePath}"></span>
      <span class="smallNote">Absolute path to apk or ipa file.</span>
    </td>
  </tr>

</l:settingsGroup>

