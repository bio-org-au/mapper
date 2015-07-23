<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>NSL Gateway</title>
	</head>
	<body>

		<div id="page-body" role="main">
			<h1>NSL Gateway</h1>
			<p>This gateway is a broker for the resources of the NSL, it translates unique identifiers or links with
      NSL resources. It should work transparently for the URLs that you have, e.g.
      <a href="/nsl-mapper/boa/apni.taxon/743619">http://biodiversity.org.au/apni.taxon/743619</a></p>

			<div id="controller-list" role="navigation">
				<h2>Available Controllers:</h2>
				<ul>
					<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
						<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
					</g:each>
				</ul>
			</div>
		</div>
	</body>
</html>
