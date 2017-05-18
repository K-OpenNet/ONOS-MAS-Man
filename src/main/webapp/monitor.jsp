<%@ page import="java.util.List" %>
<%@ page import="kr.postech.monet.config.bean.SiteBean" %>
<%@ page import="kr.postech.monet.config.bean.PMBean" %>
<%@ page import="kr.postech.monet.config.bean.VMBean" %>
<%@ page import="kr.postech.monet.utils.SSHConnectionUtil" %>
<%@ page import="kr.postech.monet.core.database.bean.ControlTrafficBean" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="kr.postech.monet.core.database.bean.CPULoadBean" %>
<%@ page import="kr.postech.monet.core.database.bean.NumSwitchesBean" %><%--
  Created by IntelliJ IDEA.
  User: woojoong
  Date: 2016-11-10
  Time: 오후 2:17
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
    <title>Monitoring</title>
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
<table width="800" border="2" cellspacing="0" cellpadding="0" style="border-collapse: collapse" rules="rows" align="center" frame="hsides">
    <tr style="background-color: lightgray; height: 20pt">
        <td width="800" colspan="4" align="center">
            Check Heartbeat
        </td>
    </tr>
    <tr>
        <td width="200" align="center">Site name</td>
        <td width="200" align="center">PM name</td>
        <td width="200" align="center">VM name</td>
        <td width="200" align="center">Status</td>
    </tr>

    <%
        for (int index1 = 0; index1 < siteBeanList.size(); index1++) {
            SiteBean tmpSite = siteBeanList.get(index1);
            List<PMBean> tmpPMBeanList = tmpSite.getPmConfPool().getPmBeans();
            for (int index2 = 0; index2 < tmpPMBeanList.size(); index2++) {
                PMBean tmpPMBean = tmpPMBeanList.get(index2);
                boolean tmpPMHeartBeat = tmpPMBean.isAlive();
                List<VMBean> tmpVMBeanList = tmpPMBean.getVmConfPool().getVmBeans();
                for (int index3 = 0; index3 < tmpVMBeanList.size(); index3++) {
                    VMBean tmpVMBean = tmpVMBeanList.get(index3);
                    boolean tmpVMHeartBeat = tmpVMBean.isAlive();
    %>
    <tr>
        <td width="200" align="center">
            <%=tmpSite.getSiteAlias()%>
        </td>
        <td width="200" align="center">
            <%=tmpPMBean.getPmAlias()%>&nbsp;&nbsp;
            <%
                if(tmpPMHeartBeat == true) {
            %>
            (<font color="green">●</font>)
            <%
            } else {
            %>
            (<font color="red">●</font>)
            <%
                }
            %>
        </td>
        <td width="200" align="center">
            <%=tmpVMBean.getVmAlias()%>
        </td>
        <td width="200" align="center">
            <%
                if(tmpVMHeartBeat == true) {
            %>
            (<font color="green">●</font>)
            <%
            } else {
            %>
            (<font color="red">●</font>)
            <%
                }
            %>
        </td>
    </tr>
    <%
                }
            }

        }
    %>
</table>

<%
    for (int index1 = 0; index1 < siteBeanList.size(); index1++){
        SiteBean tmpSite = siteBeanList.get(index1);
        List<PMBean> pmBeanList = tmpSite.getPmConfPool().getPmBeans();
%>
<br />
<table width="900" border="2" cellspacing="0" cellpadding="0" style="border-collapse: collapse" rules="rows" align="center" frame="hsides">
    <tr style="background-color: lightgray; height: 20pt">
        <td width="900" colspan="3" align="center">
            Site monitoring: <%=tmpSite.getSiteAlias()%>
        </td>
    </tr>

    <%
        for (int index2 = 0; index2 < pmBeanList.size(); index2++) {
            PMBean tmpPm = pmBeanList.get(index2);
            List<VMBean> vmBeanList = tmpPm.getVmConfPool().getVmBeans();
    %>
    <tr style="background-color: lightgray; height: 20pt">
        <td width="900" colspan="3" align="center">
            PM: <%=tmpPm.getPmAlias()%>
        </td>
    </tr>
    <%
        for (int index3 = 0; index3 < vmBeanList.size(); index3++) {
            VMBean tmpVm = vmBeanList.get(index3);
    %>
    <tr style="background-color: lightgray; height: 20pt">
        <td width="900" colspan="3" align="center">
            VM: <%=tmpVm.getVmAlias()%>
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
            <canvas id="cp_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("cp_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
                        datasets: [{
                            label: '# Control Packets',
                            data: [
                                <%
                                for (int index = 0; index < controlTrafficResults.get(tmpVm).size(); index++) {
                                %>
                                <%=controlTrafficResults.get(tmpVm).get(index).getNumTotalPackets()%>
                                <%
                                    if (index != controlTrafficResults.get(tmpVm).size()-1) {
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
            <canvas id="cpu_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("cpu_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
                        datasets: [{
                            label: 'CPU Load [%]',
                            data: [
                                <%
                                for (int index = 0; index < cpuLoadResults.get(tmpVm).size(); index++) {
                                %>
                                <%=cpuLoadResults.get(tmpVm).get(index).getCpuLoad()%>
                                <%
                                    if (index != cpuLoadResults.get(tmpVm).size()-1) {
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
            <canvas id="sw_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>" width="300" height="300" />
            <script>
                var ctx = document.getElementById("sw_<%=tmpPm.getPmAlias()%>_<%=tmpVm.getVmAlias()%>");
                Chart.defaults.global.maintainAspectRatio = false;
                Chart.defaults.global.responsive = false;
                var myChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
                        datasets: [{
                            label: '# Switches',
                            data: [
                                <%
                                for (int index = 0; index < numSwitchesResults.get(tmpVm).size(); index++) {
                                %>
                                <%=numSwitchesResults.get(tmpVm).get(index).getNumSwitches()%>
                                <%
                                    if (index != numSwitchesResults.get(tmpVm).size()-1) {
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
            }
        }
    %>

</table>
<%
    }
%>

</body>
</html>
