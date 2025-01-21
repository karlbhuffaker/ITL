<%@ page import="com.optum.itl.UserVm" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Delete User VM</title>
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
<%
	if (request.getAttribute("userVm") != null) {
		UserVm userVm = (UserVm) request.getAttribute("userVm");
%>
<form action="/ITL/user" method="post">
<section>
	<div class="uitkPanel">
		<p>Delete a User VM - Verify User VM is to be deleted and click Delete User VM</p>
		<table width="80%">
			<tbody>
			<tr align="center">
				<td align="right">User Id:</td>
				<td align="left"><input type="text" name="userid" id="userid" width="15" value="<%= userVm.getUserId()%>" readonly/></td>
			</tr>
			<tr align="center">
				<td align="right">User Id:</td>
				<td align="left"><input type="text" name="vmName" id="vmName" width="15" value="<%= userVm.getVmName()%>" readonly/></td>
			</tr>
			</tbody>
		</table>
	</div>
</section>
<%
}else{
%>
<h1>Nouservmrecordfound.</h1>
<%}%>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="deleteUserVM">
		<input type="submit" value="Delete User VM" onclick="javascript:return confirm('Are you sure you want to delete this User VM?')"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
