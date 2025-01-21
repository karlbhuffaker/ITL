<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<%
		String userid = (String) request.getSession().getAttribute("userid");
		if (null == userid) {
			response.sendRedirect("/ITL/login.jsp");
		}
	%>
	<title>ICP Tech Lab - Dashboard</title>
	<meta charset="UTF-8">
	<link type="text/css" rel="stylesheet" href="uitk.css">
	<link type="text/css" rel="stylesheet" href="itl.css">
	<style type="text/css">
		tbody {
			overflow:hidden;
			overflow-y:auto;
			display:block;
		}
		tbody tr { height:44px}
		thead, tbody tr {
			display:table;
			width:100%;
			table-layout:fixed;
			height:auto;
		}
		footer {position:absolute;bottom:0;}
	</style>
</head>
<body>
	<%@ include file = "header.jsp" %>
	<section>
		<div class="uitkPanel">
			<table>
				<tr>
					<td><p style="font-size:15px"><b>Key Actions</b></p></td>
				</tr>
				<tr>
					<td><p style="font-size:15px"><a href="/ITL/request?requestType=provisionVM">Provision VM Request</a></p></td>
				</tr>
				<tr>
					<td><p style="font-size:15px"><a href="/ITL/request?requestType=viewRequests">View Requests</a></p></td>
				</tr>
			</table>
		</div>
	</section>
	<section>
		<div class="uitkPanel">
			<table>
				<tr>
					<p style="font-size:15px"><b>Grafana RBR & Performance Graphs - Use your OTL credentials</b></p>
				</tr>
				<tr>
					<td><p style="font-size:15px"><a href="http://rbrjm:3000/d/73-79TMSk/rbr-log-stats?orgId=1&from=now-30d&to=now" target="_blank">RBR CPM Graphs</a></p></td>
				</tr>
				<tr>
					<td><p style="font-size:15px"><a href="http://rbrjm:3000/d/s70LzBJSk/icp-auto-performance-metrics?orgId=1&from=now-7d&to=now" target="_blank">ICP Auto Performance Metrics</a></p></td>
				</tr>
			</table>
		</div>
	</section>
</body>
</html>
