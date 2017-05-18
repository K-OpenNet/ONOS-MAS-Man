<%--
  Created by IntelliJ IDEA.
  User: woojoong
  Date: 2016-11-10
  Time: 오후 2:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Dash Board</title>
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
DashBoard
<br>
${dashboard}
</body>
</html>
