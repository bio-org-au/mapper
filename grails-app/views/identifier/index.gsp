
<%@ page import="au.org.biodiversity.nsl.Identifier" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'identifier.label', default: 'Identifier')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="list-identifier" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="nameSpace" title="${message(code: 'identifier.nameSpace.label', default: 'Name Space')}" />
					
						<g:sortableColumn property="idNumber" title="${message(code: 'identifier.idNumber.label', default: 'Id Number')}" />
					
						<g:sortableColumn property="objectType" title="${message(code: 'identifier.objectType.label', default: 'Object Type')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${identifierInstanceList}" status="i" var="identifierInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${identifierInstance.id}">${fieldValue(bean: identifierInstance, field: "nameSpace")}</g:link></td>
					
						<td>${fieldValue(bean: identifierInstance, field: "idNumber")}</td>
					
						<td>${fieldValue(bean: identifierInstance, field: "objectType")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${identifierInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
