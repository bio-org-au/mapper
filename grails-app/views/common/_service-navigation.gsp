<%@ page import="org.apache.shiro.SecurityUtils" %>
<div class="navbar navbar-inverse" role="navigation">
  <div class="">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="${createLink(uri: '/')}">
        <g:message code="default.application.name" default="NSL Application"/>
      </a>
    </div>

    <div class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <li class="active"><a class="home" href="${createLink(uri: '/')}">Home</a></li>
        <g:each in="${links}" var="link">
          <li class="active"><a class="${link.class}" href="${link.url}"><i class="fa ${link.icon}"></i> ${link.label}
          </a></li>
        </g:each>
      </ul>

      <ul class="nav navbar-nav navbar-right">

        <shiro:isLoggedIn>
          <shiro:hasRole name="admin">
            <li>
              <a class="home" href="${createLink(controller: 'admin', action: 'index')}">
                <i class="fa fa-gears"></i> admin
              </a>
            </li>
          </shiro:hasRole>
          <li class="active">
            <a class="home" href="${createLink(controller: 'auth', action: 'signOut')}">
              <i class="fa fa-user${shiro.hasRole(name: 'admin') {
                '-plus'
              }}"></i> <span>${SecurityUtils.subject?.principal}</span>
              -
              <i class="fa fa-power-off"></i> Logout
            </a>
          </li>
        </shiro:isLoggedIn>
        <shiro:isNotLoggedIn>
          <li class="dropdown">
            <a id="dLabel" data-target="#" href="${createLink(controller: 'auth', action: 'login')}"
               data-toggle="dropdown" aria-haspopup="true" role="button" aria-expanded="false">
              <i class="fa fa-power-off"></i> Login
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
              <li>
                <g:form controller="auth" action="signIn">
                  <input type="hidden" name="targetUri" value="${request.forwardURI - request.contextPath}"/>
                  <table>
                    <tbody>
                    <tr>
                      <td>Username:</td>
                      <td><input type="text" name="username" value=""/></td>
                    </tr>
                    <tr>
                      <td>Password:</td>
                      <td><input type="password" name="password" value=""/></td>
                    </tr>
                    <tr>
                      <td>Remember me?:</td>
                      <td><g:checkBox name="rememberMe" value=""/></td>
                    </tr>
                    <tr>
                      <td/>
                      <td><input class="btn btn-default" type="submit" value="Login"/></td>
                    </tr>
                    </tbody>
                  </table>
                </g:form>
              </li>
            </ul>
          </li>
        </shiro:isNotLoggedIn>
      </ul>
    </div><!--/.nav-collapse -->
  </div>
</div>
