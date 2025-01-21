<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Add User VM</title>
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
		<p>Add a User VM - Enter a userid, enter a vm name and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Enter the User Id:</td>
					<td align="left"><input type="text" name="userid" id="userid" width="32" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Enter the VM name:</td>
					<td align="left"><input type="text" name="vmName" id="vmName" width="15" required/></td>
				</tr>
			</tbody>
		</table>
	</div>
</section>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="addUserVM">
		<input type="submit" value="Add User VM"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
