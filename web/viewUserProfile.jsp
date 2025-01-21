<%@ page import="com.optum.itl.UserProfile" %>
<%@ page import="com.optum.itl.UserVm" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - View User Profile</title>
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
<section>
	<div class="uitkPanel">
		<p align="center"><b>User Profile</b></p>
		<table width="50%">
			<tbody>
			<tr align="center">
				<td align="right">User Id:</td>
				<td align="left"><%= userProfile.getUserId()%></td>
			</tr>
			<tr align="center">
				<td align="right">Name:</td>
				<td align="left"><%= userProfile.getUserName()%></td>
			</tr>
			<tr align="center">
				<td align="right">Group:</td>
				<td align="left"><%= userProfile.getUserGroup()%></td>
			</tr>
			<tr align="center">
				<td align="right">Status:</td>
				<td align="left"><%= userProfile.getStatus()%></td>
			</tr>
			<tr align="center">
				<td align="right">Email Address:</td>
				<td align="left"><%= userProfile.getEmailAddress()%></td>
			</tr>
			<tr align="center">
				<td align="right">Allowed VMs:</td>
				<td align="left"><%= userProfile.getAllowedVmTotal()%></td>
			</tr>
			<tr align="center">
				<td align="right">Current VMs:</td>
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
	<div class="uitkPanel">
		<p align="center"><b>User VMs</b></p>
		<table width="50%">
			<tbody>
				<tr bgcolor="00FF7F">
					<th style="text-align:center"><b>VM Name</b></th>
				</tr>
				<%
					ArrayList<UserVm> userVmArray = (ArrayList) request.getAttribute("userVMs");
					for(UserVm jsonUserVm : userVmArray){%>
				<tr>
					<td align="center"><%=jsonUserVm.getVmName()%></td>
				</tr>
				<%}%>
			</tbody>
		</table>
	</div>
</section>
</body>
</html>
