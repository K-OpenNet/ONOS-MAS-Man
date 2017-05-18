<%@ page import="kr.postech.monet.config.bean.SiteBean" %>
<%@ page import="java.util.List" %>
<%@ page import="kr.postech.monet.config.bean.VMBean" %>
<%@ page import="kr.postech.monet.core.database.bean.NumSwitchesBean" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="kr.postech.monet.core.database.bean.ControlTrafficBean" %>
<%@ page import="kr.postech.monet.core.database.bean.CPULoadBean" %>
<%@ page import="kr.postech.monet.config.bean.PMBean" %><%--
  Created by IntelliJ IDEA.
  User: woojoong
  Date: 2016-11-10
  Time: 오후 2:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<SiteBean> siteBeanList = (List<SiteBean>) request.getAttribute("confList");
    HashMap<VMBean, List<ControlTrafficBean>> controlTrafficResults = (HashMap<VMBean, List<ControlTrafficBean>>) request.getAttribute("controlTrafficResults");
    HashMap<VMBean, List<CPULoadBean>> cpuLoadResults = (HashMap<VMBean, List<CPULoadBean>>) request.getAttribute("cpuLoadResults");
    HashMap<VMBean, List<NumSwitchesBean>> numSwitchesResults = (HashMap<VMBean, List<NumSwitchesBean>>) request.getAttribute("numSwitchesResults");
%>
<html>
<head>
    <title>Mastership</title>
    <script type="text/javascript" src="/resources/js/jquery-3.1.1.js"></script>
    <script type="text/javascript" src="/resources/js/Chart.js"></script>
    <script>
        function pageRefresh() {
            window.setTimeout("pageReload()", 30000);
        }

        function pageReload() {
            location.reload();
        }
    </script>
</head>
<body onload="pageRefresh()">

<%
    for (int index1 = 0; index1 < siteBeanList.size(); index1++){
        SiteBean tmpSite = siteBeanList.get(index1);
        List<PMBean> pmBeanList = tmpSite.getPmConfPool().getPmBeans();
%>
<br />

<table width="900" border="2" cellspacing="0" cellpadding="0" style="border-collapse: collapse" rules="rows" align="center" frame="hsides">
    <tr style="background-color: lightgray; height: 20pt">
        <td width="900" colspan="3" align="center">
            Site monitoring - mastership: <%=tmpSite.getSiteAlias()%>
        </td>
    </tr>
    <tr style="background-color: white; height: 20pt">
        <td width="300" align="center">
            # Control Packets
        </td>
        <td width="300" align="center">
            CPU Load [%]
        </td>
        <td width="300" align="center">
            # Switches
        </td>
    </tr>
    <tr style="background-color: white; height: 20pt">
        <td width="300" align="center">
            <canvas id="cp_<%=tmpSite.getSiteAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("cp_<%=tmpSite.getSiteAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: [
                            <%
                                for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                    PMBean tmpPm = pmBeanList.get(index2);
                                    List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                    for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                        VMBean tmpVm = vmBeanList.get(index3);
                            %>
                            '<%=tmpVm.getVmAlias()%>'
                            <%
                                        if (index3 != vmBeanList.size()-1){
                                        %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                        ],
                        datasets: [{
                            label: '# Control Packets',
                            data: [
                                <%
                            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                PMBean tmpPm = pmBeanList.get(index2);
                                List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                    VMBean tmpVm = vmBeanList.get(index3);
                        %>
                                <%=controlTrafficResults.get(tmpVm).get(0).getNumTotalPackets()%>
                                <%
                                            if (index3 != vmBeanList.size()-1){
                                            %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                            ],
                            backgroundColor: [
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)',
                                'rgba(255, 99, 132, 0.2)'
                            ],
                            borderColor: [
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)',
                                'rgba(255,99,132,1)'
                            ],
                            borderWidth: 1
                        }]
                    },
                    options: {
                        scales: {
                            yAxes: [{
                                ticks: {
                                    beginAtZero: true
                                }
                            }]
                        }
                    }
                });
            </script>
        </td>
        <td width="300" align="center">
            <canvas id="cpu_<%=tmpSite.getSiteAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("cpu_<%=tmpSite.getSiteAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: [
                            <%
                                for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                    PMBean tmpPm = pmBeanList.get(index2);
                                    List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                    for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                        VMBean tmpVm = vmBeanList.get(index3);
                            %>
                            '<%=tmpVm.getVmAlias()%>'
                            <%
                                        if (index3 != vmBeanList.size()-1){
                                        %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                        ],
                        datasets: [{
                            label: 'CPU Load [%]',
                            data: [
                                <%
                            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                PMBean tmpPm = pmBeanList.get(index2);
                                List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                    VMBean tmpVm = vmBeanList.get(index3);
                        %>
                                <%=cpuLoadResults.get(tmpVm).get(0).getCpuLoad()%>
                                <%
                                            if (index3 != vmBeanList.size()-1){
                                            %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                            ],
                            backgroundColor: [
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)',
                                'rgba(54, 162, 235, 0.2)'
                            ],
                            borderColor: [
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)',
                                'rgba(54, 162, 235, 1)'
                            ],
                            borderWidth: 1
                        }]
                    },
                    options: {
                        scales: {
                            yAxes: [{
                                ticks: {
                                    beginAtZero: true
                                }
                            }]
                        }
                    }
                });
            </script>
        </td>
        <td width="300" align="center">
            <canvas id="sw_<%=tmpSite.getSiteAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("sw_<%=tmpSite.getSiteAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: [
                            <%
                                for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                    PMBean tmpPm = pmBeanList.get(index2);
                                    List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                    for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                        VMBean tmpVm = vmBeanList.get(index3);
                            %>
                            '<%=tmpVm.getVmAlias()%>'
                            <%
                                        if (index3 != vmBeanList.size()-1){
                                        %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                        ],
                        datasets: [{
                            label: '# Switches',
                            data: [
                                <%
                            for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
                                PMBean tmpPm = pmBeanList.get(index2);
                                List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
                                for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
                                    VMBean tmpVm = vmBeanList.get(index3);
                        %>
                                <%=numSwitchesResults.get(tmpVm).get(0).getNumSwitches()%>
                                <%
                                            if (index3 != vmBeanList.size()-1){
                                            %>,<%
                                    }
                                }
                                if (index2 != pmBeanList.size()-1) {
                                %>,<%
                                }
                            }
                        %>
                            ],
                            backgroundColor: [
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)',
                                'rgba(255, 206, 86, 0.2)'
                            ],
                            borderColor: [
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(255, 206, 86, 1)'

                            ],
                            borderWidth: 1
                        }]
                    },
                    options: {
                        scales: {
                            yAxes: [{
                                ticks: {
                                    beginAtZero: true
                                }
                            }]
                        }
                    }
                });
            </script>
        </td>
    </tr>
    <%
        for (int index2 = 0; index2 < pmBeanList.size(); index2++) {

        }
    %>

</table>
<%
    }
%>

</body>
</html>
