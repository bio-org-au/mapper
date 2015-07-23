<%@ page import="au.org.biodiversity.nsl.Identifier" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <g:set var="entityName" value="${message(code: 'identifier.label', default: 'Identifier')}"/>
  <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav" role="navigation">
  <ul>
    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
    <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
  </ul>
</div>

<div id="show-identifier" class="content scaffold-show" role="main">
  <h1><g:message code="default.show.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
  </g:if>
  <ol class="property-list identifier">

    <g:if test="${identifierInstance?.nameSpace}">
      <li class="fieldcontain">
        <span id="nameSpace-label" class="property-label"><g:message code="identifier.nameSpace.label"
                                                                     default="Name Space"/></span>

        <span class="property-value" aria-labelledby="nameSpace-label"><g:fieldValue bean="${identifierInstance}"
                                                                                     field="nameSpace"/></span>

      </li>
    </g:if>

    <g:if test="${identifierInstance?.idNumber}">
      <li class="fieldcontain">
        <span id="idNumber-label" class="property-label"><g:message code="identifier.idNumber.label"
                                                                    default="Id Number"/></span>

        <span class="property-value" aria-labelledby="idNumber-label"><g:fieldValue bean="${identifierInstance}"
                                                                                    field="idNumber"/></span>

      </li>
    </g:if>

    <g:if test="${identifierInstance?.identities}">
      <li class="fieldcontain">
        <span id="identities-label" class="property-label"><g:message code="identifier.identities.label"
                                                                      default="Identities"/></span>

        <g:each in="${identifierInstance.identities}" var="i">
          <span class="property-value" aria-labelledby="identities-label"><g:link controller="match" action="show"
                                                                                  id="${i.id}">${i?.encodeAsHTML()}</g:link></span>
        </g:each>

      </li>
    </g:if>

    <g:if test="${identifierInstance?.objectType}">
      <li class="fieldcontain">
        <span id="objectType-label" class="property-label"><g:message code="identifier.objectType.label"
                                                                      default="Object Type"/></span>

        <span class="property-value" aria-labelledby="objectType-label"><g:fieldValue bean="${identifierInstance}"
                                                                                      field="objectType"/></span>

      </li>
    </g:if>

    <g:if test="${identifierInstance?.deleted}">
      <li class="fieldcontain">
        <span id="deleted-label" class="property-label"><g:message code="identifier.deleted.label"
                                                                      default="Deleted"/></span>

        <span class="property-value" aria-labelledby="deleted-label"><g:fieldValue bean="${identifierInstance}"
                                                                                      field="deleted"/></span>

      </li>
    </g:if>

    <g:if test="${identifierInstance?.reasonDeleted}">
      <li class="fieldcontain">
        <span id="reason-label" class="property-label"><g:message code="identifier.reason.label"
                                                                   default="Reason deleted"/></span>

        <span class="property-value" aria-labelledby="reason-label"><g:fieldValue bean="${identifierInstance}"
                                                                                   field="reasonDeleted"/></span>

      </li>
    </g:if>
  </ol>
</div>
</body>
</html>
