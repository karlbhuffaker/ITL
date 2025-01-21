<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Add User Profile</title>
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
<form action="/ITL/user" method="post">
<section>
	<div class="uitkPanel">
		<p>Add a User Profile - Enter the data, make your choices and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Enter the User Id:</td>
					<td align="left"><input type="text" name="userid" id="userid" width="32" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Enter the User Name:</td>
					<td align="left"><input type="text" name="userName" id="userName" width="50" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Select a User Group:</td>
					<td align="left">
						<select name="userGroup" id="userGroup" required>
							<option value=""></option>
							<option value="development">Development</option>
							<option value="QA_Support">QA Support</option>
							<option value="devops">DevOps</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">Enter the User E-Mail Address:</td>
					<td align="left"><input type="text" name="emailAddress" id="emailAddress" width="50" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Select the Users Status:</td>
					<td align="left">
						<select name="status" id="status" required>
							<option value=""></option>
							<option value="active">Active</option>
							<option value="inactive">Inactive</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">Enter the Users Allowed VM Total:</td>
					<td align="left"><input type="text" name="allowedVMTotal" id="allowedVMTotal" width="3" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Enter the Users Current VM Total:</td>
					<td align="left"><input type="text" name="currentVMTotal" id="currentVMTotal" width="3" required/></td>
				</tr>
			</tbody>
		</table>
	</div>
</section>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="addUserProfile">
		<input type="submit" value="Add User Profile"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
