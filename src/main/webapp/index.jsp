<%@ page import="kr.postech.monet.config.bean.SiteBean" %>
<%@ page import="java.util.List" %>
<%@ page import="kr.postech.monet.config.GeneralConf" %>
<%@ page import="kr.postech.monet.config.bean.PMBean" %>
<%@ page import="kr.postech.monet.config.bean.VMBean" %>
<%--
  Created by IntelliJ IDEA.
  User: woojoong
  Date: 2016-11-07
  Time: 오후 3:34
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<SiteBean> siteBeanList = GeneralConf.siteConfPoolList.getSiteBeans();
%>
<html>
<head>
    <title>[K-ICT] Orchestration Software for ONOS controllers</title>
    <link rel="stylesheet" type="text/css" href="resources/css/topMenu.css">
</head>
<body>
<header id="index-header-Section" role="header" class="">
    <!-- menu section -->
    <div align="center">
        <nav id="index-header-menu" role="navigation">
            <ul>
                <li class="index-header-menu-top">
                    <a href="index.jsp" class="index-header-menu-top-link">Orchestration SW v1.0</a>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="/monitoring.kone" class="index-header-menu-top-link" target="article-main">Monitoring</a>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="/configuration.kone" class="index-header-menu-top-link" target="article-main">Configure</a>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <!--add a database admin page-->
                    <a href="http://***.***.***.***:*****/" class="index-header-menu-top-link" target="article-main">Database</a> <!-- non KOREN-->

                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="#" class="index-header-menu-top-link">ONOS UI</a>
                    <ul class="index-header-menu-top-submenu">
                        <%
                            for (int index = 0; index < siteBeanList.size(); index++) {
                                SiteBean tmpSiteBean = siteBeanList.get(index);
                                PMBean tmpPMBean = tmpSiteBean.getPmConfPool().getPmBeans().get(0);
                                VMBean tmpVMBean = tmpPMBean.getVmConfPool().getVmBeans().get(0);
                                String tmpONOSAddr = "http://" + tmpVMBean.getAccessIPAddress() +
                                        ":" + tmpVMBean.getAccessHTTPPort() + "/" +
                                        "onos/ui";
                        %>
                        <li>
                            <a href="<%=tmpONOSAddr%>" class="index-header-menu-top-submenu-link" target="article-main"><%=tmpSiteBean.getSiteAlias()%></a>
                        </li>
                        <%
                            }
                        %>
                    </ul>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="/mastership.kone" class="index-header-menu-top-link" target="article-main">Mastership&Scaling</a>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="/shell.kone" class="index-header-menu-top-link" target="article-main">Shell</a>
                </li>
                <li>|</li>
                <li class="index-header-menu-top">
                    <a href="https://sites.google.com/site/woojoong88/" class="index-header-menu-top-link"
                       target="_blank">Contact</a>
                </li>
            </ul>
        </nav>
    </div>
</header>

<br />
<br />


<div>
    <iframe width="1223" height="85%" name="article-main" frameborder="0" src="/dashboard.kone"></iframe>
</div>

<br />
<br />


<footer>
    <small>Involved in K-ONE project, sponsored by IITP</small><br />
    <small>Implemented by Woojoong Kim from MoNet Lab.@POSTECH, South Korea</small>
</footer>
</body>
</html>
