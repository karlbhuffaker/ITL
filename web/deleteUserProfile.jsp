<%@ page import="com.optum.itl.UserProfile" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Delete User Profile</title>
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
	if (request.getAttribute("userProfile") != null) {
		UserProfile userProfile = (UserProfile) request.getAttribute("userProfile");
%>
<form action="/ITL/user" method="post">
<section>
	<div class="uitkPanel">
		<p>Delete a User User Profile - Verify User Profile is to be deleted and click Delete User Profile</p>
		<table width="80%">
			<tbody>
			<tr align="center">
				<td align="right">User Id:</td>
				<td align="left"><input type="text" name="userid" id="userid" width="15" value="<%= userProfile.getUserId()%>" readonly/></td>
			</tr>
			<tr align="center">
				<td align="right">User Name:</td>
				<td align="left"><%= userProfile.getUserName()%></td>
			</tr>
			<tr align="center">
				<td align="right">User Group:</td>
				<td align="left"><%= userProfile.getUserGroup()%></td>
			</tr>
			<tr align="center">
				<td align="right">Email Address:</td>
				<td align="left"><%= userProfile.getEmailAddress()%></td>
			</tr>
			<tr align="center">
				<td align="right">Status:</td>
				<td align="left"><%= userProfile.getStatus()%></td>
			</tr>
			<tr align="center">
				<td align="right">Allowed VM Total:</td>
				<td align="left"><%= userProfile.getAllowedVmTotal()%></td>
			</tr>
			<tr align="center">
				<td align="right">Current VM Total:</td>
				<td align="left"><%= userProfile.getCurrentVmTotal()%></td>
			</tr>
			</tbody>
		</table>
	</div>
</section>
<%
}else{
%>
<h1>Nouserprofilerecordfound.</h1>
<%}%>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="deleteUserProfile">
		<input type="submit" value="Delete User Profile" onclick="javascript:return confirm('Are you sure you want to delete this User Profile?')"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
