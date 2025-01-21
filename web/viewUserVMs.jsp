<%@ page import="com.optum.itl.UserVm" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - View User VMs</title>
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
						<label for="selectedAll">Get All Users VMs</label><input type="checkbox" id="selectedAll" name="selectedAll" value="selectedAll">
						&nbsp;&nbsp;&nbsp;Get the User VMs for the Entered UserId: <input type="text" id="selectedUserid" name="selectedUserid" width="30"/>
						<input type="hidden" id="requestType" name="requestType" value="viewUserVMs">
						&nbsp;&nbsp;&nbsp;<input type="submit" value="Get User VMs"/>&nbsp;&nbsp;

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
			<a href="/ITL/user?requestType=viewUserVMs"><img src="refresh.png" style="width:30px;height:30px;"></a>
		</td>
	</tr>
	<tr bgcolor="00FF7F">
		<th><b>User Id</b></th>
		<th><b>VM Name</b></th>
		<th><b>Delete Action</b></th>
	</tr>
	<%
		ArrayList<UserVm> userVMs = (ArrayList) request.getAttribute("userVMs");
		for(UserVm userVM : userVMs){%>
	<tr>
		<td><%=userVM.getUserId()%></td>
		<td><%=userVM.getVmName()%></td>
		<td><a href="/ITL/user?requestType=deleteUserVM&userid=<%=userVM.getUserId()%>&vmName=<%=userVM.getVmName()%>">Delete User VM</a></td>
	</tr>
	<%}%>
</table>
</body>
</html>
