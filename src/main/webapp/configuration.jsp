<%@ page import="java.util.List" %>
<%@ page import="kr.postech.monet.config.bean.SiteBean" %>
<%@ page import="kr.postech.monet.config.bean.PMBean" %>
<%@ page import="kr.postech.monet.config.bean.VMBean" %><%--
  Created by IntelliJ IDEA.
  User: woojoong
  Date: 2016-11-09
  Time: 오후 2:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<SiteBean> siteBeanList = (List<SiteBean>) request.getAttribute("confList");
%>
<html>
<head>
    <title>Configuration</title>
</head>
<body>
<br/>
<table width="1000" border="2" cellspacing="0" cellpadding="0" style="border-collapse: collapse" rules="rows" align="center" frame="hsides">
    <tr style="background-color: lightgray; height: 20pt">
        <td width="1000" colspan="5" align="center">
            Total configuration parameters
        </td>
    </tr>
    <tr>
        <td width="200" align="center">Site name</td>
        <td width="200" align="center">PM name</td>
        <td width="200" align="center">VM name</td>
        <td width="400" align="center" colspan="2">Parameters</td>
    </tr>
    <%
        for (int index1 = 0; index1 < siteBeanList.size(); index1++) {
            SiteBean tmpSite = siteBeanList.get(index1);
            List<PMBean> tmpPMBeanList = tmpSite.getPmConfPool().getPmBeans();
            for (int index2 = 0; index2 < tmpPMBeanList.size(); index2++) {
                PMBean tmpPMBean = tmpPMBeanList.get(index2);
                List<VMBean> tmpVMBeanList = tmpPMBean.getVmConfPool().getVmBeans();
                for (int index3 = 0; index3 < tmpVMBeanList.size(); index3++) {
                    VMBean tmpVMBean = tmpVMBeanList.get(index3);
    %>

    <tr>
        <td width="200" align="center">
            <%=tmpSite.getSiteAlias()%>
        </td>
        <td width="200" align="center">
            <%=tmpPMBean.getPmAlias()%>
        </td>
        <td width="200" align="center">
            <%=tmpVMBean.getVmAlias()%>
        </td>
        <td width="400" align="left" colspan="2">
            <ul>
                <li>VM IP address: <%=tmpVMBean.getIpAddress()%></li>
                <li>VM SSH port: <%=tmpVMBean.getSshPort()%></li>
                <li>VM HTTP port: <%=tmpVMBean.getHttpPort()%></li>
                <li>Accessible VM IP address: <%=tmpVMBean.getAccessIPAddress()%></li>
                <li>Accessible VM SSH port: <%=tmpVMBean.getAccessSSHPort()%></li>
                <li>Accessible VM HTTP port: <%=tmpVMBean.getAccessHTTPPort()%></li>
            </ul>
        </td>
    </tr>
    <%
                }
            }

        }
    %>

    <tr style="height: 20pt">
        <td width="1000" colspan="5" align="right">
            <input type="button" value="ADD"> &nbsp;&nbsp;&nbsp;
            <input type="button" value="MOD"> &nbsp;&nbsp;&nbsp;
            <input type="button" value="DEL">
        </td>
    </tr>
</table>
${results}
</body>
</html>
