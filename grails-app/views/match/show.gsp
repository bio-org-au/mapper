<%@ page import="au.org.biodiversity.nsl.Match" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <g:set var="entityName" value="${message(code: 'match.label', default: 'Match')}"/>
  <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
  <ul>
    <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
  </ul>
</div>

<div id="show-match" class="content scaffold-show" role="main">
  <h1><g:message code="default.show.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
  </g:if>
  <ol class="property-list match">

    <g:if test="${matchInstance?.uri}">
      <li class="fieldcontain">
        <span id="uri-label" class="property-label"><g:message code="match.uri.label" default="Uri"/></span>

        <span class="property-value" aria-labelledby="uri-label">
          <a href="${request.contextPath}/${matchInstance.uri}">
            <g:fieldValue bean="${matchInstance}" field="uri"/>
          </a>
        </span>

      </li>
    </g:if>
    <li class="fieldcontain">
      <span id="uri-label" class="property-label">Hosts</span>

      <ul>
        <g:each in="${matchInstance.hosts}" var="host">
          <li>
            Host: ${host.hostName}
          </li>
        </g:each>
      </ul>
    </li>

    <li class="fieldcontain">
      <span id="deprecated-label" class="property-label"><g:message code="match.uri.label" default="Deprecated"/></span>

      <span class="property-value" aria-labelledby="deprecated-label">
        <g:fieldValue bean="${matchInstance}" field="deprecated"/>
      </span>

    </li>

    <g:if test="${matchInstance?.identifiers}">

      <li class="fieldcontain">
        <span id="alias-label" class="property-label">aka</span>
        <span class="property-value" aria-labelledby="identifiers-label">
          <ul>
            <g:each in="${matchInstance.identifiers.collect { it.identities*.uri }.flatten()}" var="alias">
              <g:if test="${alias != matchInstance.uri}">
                <li>
                  <a href="${request.contextPath}/${alias}">
                    ${alias}
                  </a>
                </li>
              </g:if>
            </g:each>
          </ul>
        </span>
      </li>

      <li class="fieldcontain">
        <span id="identifiers-label" class="property-label">
          <g:message code="match.identifiers.label" default="Identifiers"/>
        </span>

        <g:each in="${matchInstance.identifiers}" var="i">
          <span class="property-value" aria-labelledby="identifiers-label">
            <g:link controller="identifier" action="show" id="${i.id}">${i?.encodeAsHTML()}</g:link>
          </span>
        </g:each>

      </li>
    </g:if>

  </ol>
</div>
</body>
</html>
