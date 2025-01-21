<%@ page import="com.optum.itl.Request" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - View Requests</title>
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
<% String userGroupName = (String) request.getSession().getAttribute("usergroup"); %>
<%if (userGroupName.equalsIgnoreCase("devops")) { %>
	<form action="/ITL/request" method="get">
		<section>
			<div class="uitkPanel">
				<table width="80%">
					<tbody>
					<tr align="left">
						<td>
							<label for="selectedAll">Get All Requests</label><input type="checkbox" id="selectedAll" name="selectedAll" value="selectedAll">
							&nbsp;&nbsp;&nbsp;Get Requests for Selected UserId: <input type="text" id="selectedUserid" name="selectedUserid" width="30"/>
							<input type="hidden" id="requestType" name="requestType" value="viewRequests">
							&nbsp;&nbsp;&nbsp;<input type="submit" value="Get Requests"/>&nbsp;&nbsp;

						</td>
					</tr>
					</tbody>
				</table>
			</div>
		</section>
	</form>
<%}%>
<table border ="1" width="1000" align="center">
	<tr align="center">
		<td align="center">
			<a href="/ITL/request?requestType=viewRequests"><img src="refresh.png" style="width:30px;height:30px;"></a>
		</td>
	</tr>
	<tr bgcolor="00FF7F">
		<th><b>Request Id</b></th>
		<th><b>Status</b></th>
		<th><b>Request Type</b></th>
		<th><b>VM Name</b></th>
		<th><b>User Id</b></th>
		<th><b>Template</b></th>
		<th><b>Product File</b></th>
		<th><b>Job Start Time</b></th>
		<th><b>Job End Time</b></th>
	</tr>
	<%
		ArrayList<Request> requestArray = (ArrayList) request.getAttribute("requests");
		for(Request singleRequest : requestArray){%>
	<tr>
		<td><%=singleRequest.getRequestId()%></td>
		<td><%=singleRequest.getStatus()%></td>
		<td><%=singleRequest.getRequestType()%></td>
		<td><%=singleRequest.getVmName()%></td>
		<td><%=singleRequest.getUserId()%></td>
		<td><%=singleRequest.getTemplate() == null ? "" : singleRequest.getTemplate() %></td>
		<td><%=singleRequest.getProductInstall() == null ? "" : singleRequest.getProductInstall() %></td>
		<td><%=singleRequest.getStartTimestamp() == null ? "" : singleRequest.getStartTimestamp() %></td>
		<td><%=singleRequest.getEndTimestamp() == null ? "" : singleRequest.getEndTimestamp() %></td>
	</tr>
	<%}%>
</table>
</body>
</html>
