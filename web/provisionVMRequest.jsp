<%@ page import="com.optum.itl.ConfigProperty" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Provision VM Request</title>
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
		<p>Provision VM Request - make your choices and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Select a template:</td>
					<td align="left">
						<select name="template" id="template" required>
							<option value=""></option>
							<%
								ArrayList<ConfigProperty> configPropertyGroup = (ArrayList) request.getAttribute("configPropertyGroup");
								for(ConfigProperty configProperty : configPropertyGroup){%>
								<%=configProperty.getPropertyName()%><%=configProperty.getPropertyValue()%></option>
							<%}%>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">Enter the VM name (15 characters max):</td>
					<td align="left"><input type="text" name="vmName" id="vmName" width="15" required/></td>
				</tr>
		</tbody>
		</table>
	</div>
</section>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="provisionVM">
		<input type="submit" value="Submit Request"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
