<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>NSL Gateway Export</title>
</head>

<body>
<div class="container">

  <h2>Export identifier mappings by object type</h2>

  <p>Export object identifiers. This provides our resource to identifier mapping.</p>

  <p>An object may have multiple identifiers for a number of reasons:</p>
  <ul>
    <li>an identity (URI) has moved to a different resource object because it was a duplicate of another resource</li>
    <li>resources have moved to another system that describes more or slightly different information and maintaining the
    identifier (URI) scheme would cause clashes or prevent an objective. The URI is remapped to the resource that matches
    it and maintained as a legacy identifier.</li>
    <li>An identifier (URI) may represent multiple smaller resources as a resource group. Resolving these (via the mapper)
    will return multipe resource links for different object types.</li>
  </ul>

  <p>An identities resource may be deleted (gone) for a reason such as the resource was in error (fake news).</p>
  <p>Exports are large and dynamically generated, so they may take a little time to respond. If you experience timeouts
  from our proxy please let us know.</p>
  
  <h3>Export object type:</h3>
  <ul>
    <g:each in="${objectTypes}" var="type">
      <li><a href="${createLink(controller: 'export', action: 'identifiersByType', params: [type: type])}">${type}</a></li>
    </g:each>
    <li><a href="${createLink(controller: 'export', action: 'identifiersByType')}">All types (large)</a></li>
  </ul>

</div>
</body>
</html>