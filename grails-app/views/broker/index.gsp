<%--
  User: pmcneil
  Date: 3/02/15
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Broker - ${requested}</title>
</head>

<body>

<h1>Broker - ${requested}</h1>
<h2>resources</h2>
<ul>
  <g:each in="${links}" var="link">
    <li>
      <a href="${link}.html" type="text/html">${link}</a>
      <a href="${link}.json" type="text/json">JSON</a>
      <a href="${link}.xml" type="text/xml">XML</a>
    </li>
  </g:each>
</ul>
</body>
</html>