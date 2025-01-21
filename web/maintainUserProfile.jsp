<%@ page import="com.optum.itl.UserProfile" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Maintain User Profile</title>
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
		<p>Maintain a User Profile - Enter the data, make your choices and click submit</p>
		<table width="80%">
			<tbody>
			<tr align="center">
				<td align="right">User Id:</td>
				<td align="left"><input type="text" name="userid" id="userid" width="15" value="<%= userProfile.getUserId()%>" readonly/></td>
			</tr>
			<tr align="center">
				<td align="right">Enter the User Name:</td>
				<td align="left"><input type="text" name="userName" id="userName" width="50" value="<%= userProfile.getUserName()%>" required/></td>
			</tr>
			<tr align="center">
				<td align="right">Select a User Group:</td>
				<td align="left">
					<select name="userGroup" id="userGroup" required>
						<option value=""></option>
						<%
							if ("development".equals(userProfile.getUserGroup())) {
						%>
						<option value="development" selected>Development</option>
						<%
						} else {
						%>
						<option value="development">Development</option>
						<%
							}
						%>
						<%
							if ("QA_Support".equals(userProfile.getUserGroup())) {
						%>
						<option value="QA_Support" selected>QA Support</option>
						<%
						} else {
						%>
						<option value="QA_Support">QA Support</option>
						<%
							}
						%>
						<%
							if ("devops".equals(userProfile.getUserGroup())) {
						%>
						<option value="devops" selected>DevOps</option>
						<%
						} else {
						%>
						<option value="devops">DevOps</option>
						<%
							}
						%>
					</select>
				</td>
			</tr>
			<tr align="center">
				<td align="right">Enter the User E-Mail Address:</td>
				<td align="left"><input type="text" name="emailAddress" id="emailAddress" width="50" value="<%= userProfile.getEmailAddress()%>" required/></td>
			</tr>
			<tr align="center">
				<td align="right">Select the Users Status:</td>
				<td align="left">
					<select name="status" id="status" required>
						<option value=""></option>
						<%
							if ("active".equals(userProfile.getStatus())) {
						%>
						<option value="active" selected>Active</option>
						<%
						} else {
						%>
						<option value="active">Active</option>
						<%
							}
						%>
						<%
							if ("inactive".equals(userProfile.getStatus())) {
						%>
						<option value="inactive" selected>Inactive</option>
						<%
						} else {
						%>
						<option value="inactive">Inactive</option>
						<%
							}
						%>
					</select>
				</td>
			</tr>
			<tr align="center">
				<td align="right">Enter the Users Allowed VM Total:</td>
				<td align="left"><input type="text" name="allowedVMTotal" id="allowedVMTotal" width="3" value="<%= userProfile.getAllowedVmTotal()%>" required/></td>
			</tr>
			<tr align="center">
				<td align="right">Enter the Users Current VM Total:</td>
				<td align="left"><input type="text" name="currentVMTotal" id="currentVMTotal" width="3" value="<%= userProfile.getCurrentVmTotal()%>" required/></td>
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
		<input type="hidden" id="requestType" name="requestType" value="maintainUserProfile">
		<input type="submit" value="Maintain User Profile"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
