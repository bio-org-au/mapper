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

<g:if test="${error}">
  <div class="alert alert-danger" role="alert">
    <span class="fa fa-warning" aria-hidden="true"></span>
    ${error}
  </div>
</g:if>

<h2>Links</h2>
<ul>
  <g:each in="${links}" var="link">
    <li>
      <a href="${link.link}.html">${link.link}</a> x ${link.resourceCount} resources.
      <a href="${link.link}.json">JSON</a>
      <a href="${link.link}.xml">XML</a>
    </li>
  </g:each>
</ul>
</body>
</html>