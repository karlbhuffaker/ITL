<%@ page import="com.optum.itl.UserVm" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Delete VM Request</title>
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
<form action="/ITL/request" method="post">
<section>
	<div class="uitkPanel">
		<p>Delete VM Request - make your choice and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Select the VM name to be deleted:</td>
					<td align="left">
						<select name="vmName" id="vmName" required>
							<option value=""></option>
							<%
								ArrayList<UserVm> userVmArray = (ArrayList) request.getAttribute("userVMs");
								for(UserVm userVm : userVmArray){%>
									<option value="<%=userVm.getVmName()%>"><%=userVm.getVmName()%></option>
							<%}%>
						</select>
					</td>
				</tr>
		</tbody>
		</table>
	</div>
</section>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="deleteVM">
		<input type="submit" value="Submit Request" onclick="javascript:return confirm('Are you sure you want to delete this VM?')"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
