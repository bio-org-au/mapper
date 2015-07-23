<%--
  User: pmcneil
  Date: 3/02/15
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Broker - current identity for ${uri}</title>
</head>

<body>

<h1>Broker - current identity for ${uri}</h1>
<h2>resources</h2>
<ul>
  <g:each in="${identities}" var="identity">
    <li>
      <a href="${g.createLink(controller: 'identifier', action: 'show', id: identity.id)}">${identity}</a>
    </li>
  </g:each>
</ul>
</body>
</html>