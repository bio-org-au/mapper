<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <title><g:layoutTitle default="Grails"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="shortcut icon" href="${assetPath(src: 'link.png')}?v=2.1">
  <script type="application/javascript">
    baseContextPath = "${request.getContextPath()}";
  </script>
  <asset:stylesheet src="application.css"/>
  <asset:javascript src="application.js"/>
  <g:layoutHead/>
</head>

<body>
<g:render template="/common/service-navigation" model="[links: [
    [class: 'dashboard', url: createLink(controller: 'admin', action: 'index'), label: 'Dash', icon: 'fa-bar-chart-o']
]]"/>
<st:systemNotification/>
<div class="container-fluid">

  <div class="col-md-11">
    <g:layoutBody/>
  </div>
</div>

<div class="footer text-muted container-fluid" role="contentinfo">
  <div class="row">
    Version: <g:meta name="app.version"/>
  </div>
</div>

<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
</body>
</html>
