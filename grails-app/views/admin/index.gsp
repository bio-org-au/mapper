<%--
  User: pmcneil
  Date: 15/09/14
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Gateway Dashboard</title>
</head>

<body>
<div class="container">

  <h2>Statistics</h2>
  <ul>
    <g:each in="${stats}" var="info">
      <li><g:camelToLabel camel="${info.key}"/>: <span class="text-success"><g:linkedData val="${info.value}"/></span></li>
    </g:each>
  </ul>

</div>
</body>
</html>