<%@ page import="com.optum.itl.UserProfile" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - View User Profiles</title>
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
<form action="/ITL/user" method="get">
	<section>
		<div class="uitkPanel">
			<table width="80%">
				<tbody>
				<tr align="left">
					<td>
						<label for="selectedAll">Get All User Profiles</label><input type="checkbox" id="selectedAll" name="selectedAll" value="selectedAll">
						&nbsp;&nbsp;&nbsp;Get the User Profile for the Entered UserId: <input type="text" id="selectedUserid" name="selectedUserid" width="30"/>
						<input type="hidden" id="requestType" name="requestType" value="viewUserProfiles">
						&nbsp;&nbsp;&nbsp;<input type="submit" value="Get User Profiles"/>&nbsp;&nbsp;

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
			<a href="/ITL/user?requestType=viewUserProfiles"><img src="refresh.png" style="width:30px;height:30px;"></a>
		</td>
	</tr>
	<tr bgcolor="00FF7F">
		<th><b>User Id</b></th>
		<th><b>Status</b></th>
		<th><b>User Name</b></th>
		<th><b>User Group</b></th>
		<th><b>User Email Address</b></th>
		<th><b>Allowed VMs</b></th>
		<th><b>Current VMs</b></th>
		<th><b>Maintain Action</b></th>
		<th><b>Delete Action</b></th>
	</tr>
	<%
		ArrayList<UserProfile> userProfileArray = (ArrayList) request.getAttribute("userProfiles");
		for(UserProfile userProfile : userProfileArray){%>
	<tr>
		<td><a href="/ITL/user?requestType=viewUserProfile&userid=<%=userProfile.getUserId()%>"><%=userProfile.getUserId()%></a></td>
		<td><%=userProfile.getStatus()%></td>
		<td><%=userProfile.getUserName()%></td>
		<td><%=userProfile.getUserGroup()%></td>
		<td><%=userProfile.getEmailAddress()%></td>
		<td><%=userProfile.getAllowedVmTotal()%></td>
		<td><%=userProfile.getCurrentVmTotal()%></td>
		<td><a href="/ITL/user?requestType=maintainUserProfile&userid=<%=userProfile.getUserId()%>">Maintain User Profile</a>
		<td><a href="/ITL/user?requestType=deleteUserProfile&userid=<%=userProfile.getUserId()%>">Delete User Profile</a></td>
	</tr>
	<%}%>
</table>
</body>
</html>
