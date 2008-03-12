<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="redback" uri="http://plexus.codehaus.org/redback/taglib-1.0" %>
<%@ page import="org.apache.maven.archiva.web.startup.ArchivaVersion" %>
<%@ page import="java.util.Calendar" %>

<html>
<head>
  <title>Apache Archiva \
    <decorator:title default="Apache Archiva"/>
  </title>

  <link rel="stylesheet" href="<c:url value="/css/maven-base.css"/>" type="text/css" media="all"/>
  <link rel="stylesheet" href="<c:url value="/css/maven-theme.css"/>" type="text/css" media="all"/>
  <link rel="stylesheet" href="<c:url value="/css/redback/table.css"/>" type="text/css" media="all"/>
  <link rel="stylesheet" href="<c:url value="/css/site.css"/>" type="text/css" media="all"/>
  <link rel="stylesheet" href="<c:url value="/css/print.css"/>" type="text/css" media="print"/>
  <link rel="shortcut icon" href="<c:url value="/favicon.ico" />"/>
  <script type="text/javascript" src="<c:url value="/js/scriptaculous/prototype.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/scriptaculous/scriptaculous.js"/>"></script>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
</head>

<body onload="<decorator:getProperty property="body.onload" />" class="composite">
<div id="banner">
  <span id="bannerLeft">
    <my:currentWWUrl action="index" namespace="/">
      <img src="<c:url value='/images/archiva.png' />" alt="" width="188" height="69"/>
    </my:currentWWUrl>
  </span>
  <span id="bannerRight">
    <ww:action namespace="/components" name="companyInfo" executeResult="true"/>
  </span>

  <div class="clear">
    <hr/>
  </div>
</div>

<div id="breadcrumbs">
  <div class="xleft">
    <%@ include file="/WEB-INF/jsp/redback/include/securityLinks.jsp" %>
  </div>

  <div class="xright">
    <a href="http://maven.apache.org/archiva">Archiva</a> |
    <a href="http://maven.apache.org/">Maven</a> |
    <a href="http://www.apache.org/">Apache</a>
  </div>

  <div class="clear">
    <hr/>
  </div>
</div>

<div id="leftColumn">

  <div id="navcolumn">

    <h5>Find</h5>
    <ul>
      <li class="none">
        <my:currentWWUrl action="index" namespace="/">Search</my:currentWWUrl>
      </li>

      <ww:if test="${applicationScope.uiOptions.showFindArtifacts}">
        <li class="none">
          <my:currentWWUrl action="findArtifact" namespace="/">Find Artifact</my:currentWWUrl>
        </li>
      </ww:if>

      <li class="none">
        <my:currentWWUrl action="browse" namespace="/">Browse</my:currentWWUrl>
      </li>
      
      <li class="none">
        <my:currentWWUrl action="upload" namespace="/">Upload Artifact</my:currentWWUrl>
      </li>
    </ul>

    <redback:ifAnyAuthorized permissions="archiva-manage-users,archiva-access-reports,archiva-manage-configuration">
      <h5>Manage</h5>
      <ul>
        <redback:ifAuthorized permission="archiva-access-reports">
          <li class="none">
            <my:currentWWUrl action="pickReport" namespace="/report">Reports</my:currentWWUrl>
          </li>
        </redback:ifAuthorized>
          <%-- POSTPONED to 1.1 series
                <li class="none">
                  <a href="#">Synchronisation</a>
                </li>
          --%>
        <redback:ifAuthorized permission="archiva-manage-users">
          <li class="none">
            <my:currentWWUrl action="userlist" namespace="/security">User Management</my:currentWWUrl>
          </li>
        </redback:ifAuthorized>
        <redback:ifAuthorized permission="archiva-manage-configuration">
          <li class="none">
            <my:currentWWUrl action="configureAppearance" namespace="/admin">Appearance</my:currentWWUrl>
          </li>
        </redback:ifAuthorized>
          <%-- TODO: future options here.
             * Repository Statistics.
             * Web Services Statistics.
          --%>
      </ul>
    </redback:ifAnyAuthorized>

    <redback:ifAuthorized permission="archiva-manage-configuration">
      <h5>Administration</h5>
      <ul>
        <li class="none">
          <my:currentWWUrl action="repositories" namespace="/admin">Repositories</my:currentWWUrl>
        </li>
        <li class="none">
          <my:currentWWUrl action="proxyConnectors" namespace="/admin">Proxy Connectors</my:currentWWUrl>
        </li>
        <li class="none">
          <my:currentWWUrl action="legacyArtifactPath" namespace="/admin">Legacy support</my:currentWWUrl>
        </li>
        <li class="none">
          <my:currentWWUrl action="networkProxies" namespace="/admin">Network Proxies</my:currentWWUrl>
        </li>
        <li class="none">
          <my:currentWWUrl action="repositoryScanning" namespace="/admin">Repository Scanning</my:currentWWUrl>
        </li>
        <li class="none">
          <my:currentWWUrl action="database" namespace="/admin">Database</my:currentWWUrl>
        </li>
          <%-- TODO: future options here.
               * Repository Syncing Connectors. (rsync, ftp, scp, etc...)
               * Web Services (enable / disable), role based?
            --%>
      </ul>
    </redback:ifAuthorized>

  </div>
</div>

<div id="bodyColumn">
  <div id="contentBox">
    <decorator:body/>
  </div>
</div>

<div class="clear">
  <hr/>
</div>

<%
  int inceptionYear = 2005;
  int currentYear = Calendar.getInstance().get( Calendar.YEAR );
  String copyrightRange = String.valueOf( inceptionYear );
  if ( inceptionYear != currentYear )
  {
    copyrightRange = copyrightRange + "-" + String.valueOf( currentYear );
  }
%>
<div id="footer">
  <div class="xleft">
    Apache Archiva <%= ArchivaVersion.getVersion() %>
  </div>
  <div class="xright">
    Copyright &#169; <%= copyrightRange %> The Apache Software Foundation
  </div>

  <div class="clear">
    <hr/>

  </div>
</div>
</body>
</html>
