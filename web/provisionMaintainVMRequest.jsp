<%@ page import="com.optum.itl.UserVm" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - VM Provision/Maintain Request</title>
	<meta charset="UTF-8">
	<link type="text/css" rel="stylesheet" href="uitk.css">
	<link type="text/css" rel="stylesheet" href="itl.css">
	<script>
		function submitRequest() {
			var requestTypeOptions = document.getElementById("requestType");
			var requestType = requestTypeOptions.options[requestTypeOptions.selectedIndex].value;
			if(requestType == "deleteVM") {
				if (confirm("Are you sure you want to delete this VM?")) {
					return true;
				} else {
					return false;
				}
			}
		}
		function disableEnableTemplate() {
			var b1 = document.getElementById("requestType");
			var f = b1.options[b1.selectedIndex].value;
			var b2 = document.getElementById("template");
			var b3 = document.getElementById("vmName_provision");
			var b4 = document.getElementById("vmName");
			if (f == "ProvisionVM") {
				b2.disabled="";
				b3.disabled="";
				b4.disabled="disabled";
			} else if (f == "StartVM" || f == "RestartVMGuest" || f == "StopVMGuest" || f == "RestartVM" || f == "StopVM" || f == "deleteVM") {
				b2.disabled="disabled";
				b3.disabled="disabled";
				b4.disabled="";
			} else {
				b2.disabled="disabled";
				b3.disabled="disabled";
				b4.disabled="disabled";
			}
		}
	</script>
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
		<p>Provision/Maintain VM Request - make your choices and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Select request type:</td>
					<td align="left">
						<select name="requestType" id="requestType" onchange="disableEnableTemplate()" required>
							<option value=""></option>
							<option value="ProvisionVM" style="color:black">Provision New VM</option>
							<option value="StartVM" style="color:black">Power on existing VM</option>
							<option value="RestartVMGuest" style="color:black">Restart existing VM (gracefully)</option>
							<option value="StopVMGuest" style="color:black">Shutdown existing VM (gracefully)</option>
							<option value="RestartVM" style="color:black">Power off/on existing VM (immediate - use sparingly)</option>
							<option value="StopVM" style="color:black">Power off existing VM (immediate - use sparingly)</option>
							<option value="" disabled style="color:black">***Next option is to delete a VM - please verify - this cannot be undone***</option>
							<option value="deleteVM" style="color:red" >Delete existing VM</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a new VM request, select a template:</td>
					<td align="left">
						<select name="template" id="template" disabled required>
							<option value=""></option>
							<option value="" disabled>Windows 2019 options</option>
							<option value="MS-Windows-2019-beta-template">Windows 2019 (Beta)</option>
							<option value="W19-ORA12.2-Template">Windows 2019 Oracle 12.2c (Beta)</option>
							<option value="Win19-SQL16-Template">Windows 2019 SQL Server 2016 (Beta)</option>
							<option value="" disabled>Windows 2016 options</option>
							<option value="MS-Windows-2016-Template">Windows 2016</option>
							<option value="CES54SP2-ORA12.2c-Win16-Template">Windows 2016 CES54 SP2 Oracle 12.2c</option>
							<option value="CES54SP2-ORA18c-Win16-Template">Windows 2016 CES54 SP2 Oracle 18c</option>
							<option value="CM54SP2-ORA12.2c-Win16-Template"> Windows 2016 CM54 SP2 Oracle 12.2c</option>
							<option value="CM54SP2-ORA18c-Win16-Template">Windows 2016 CM54 SP2 Oracle 18c</option>
							<option value="CES54SP2-SQL16-Win16-Template"> Windows 2016 CES54 SP2 SQL Server 2016</option>
							<option value="CM54SP2-SQL16-Win16-Template"> Windows 2016 CM54 SP2 SQL Server 2016</option>
							<option value="" disabled>Windows 2012 options</option>
							<option value="CES531SP2-SQL12-Win12-Template">Windows 2012 CES531 SP2 SQL Server 2012</option>
							<option value="CES531SP2-ORA11g-Win12-Template">Windows 2012 CES531 SP2 Oracle 11g</option>
							<option value="CM531SP2-ORA11g-Win12-Template">Windows 2012 CM531 SP2 Oracle 11g</option>
							<option value="" disabled>Linux 7 options</option>
							<option value="CentOS-07-OS-Only-Template">Centos 7</option>
							<option value="CES54SP2-ORA12.2c-CentOS7-Template">Centos 7 CES54 SP2 Oracle 12.2c</option>
							<option value="CM54SP2-ORA12.2c-CentOS7-Template">Centos 7 CM54 SP2 Oracle 12.2c</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a new VM, enter the VM name:</td>
					<td align="left"><input type="text" name="vmName_provision" id="vmName_provision" width="15" disabled required/></td>
				</tr>
				<tr align="center">
					<td align="right">Select the VM name:</td>
					<td align="left">
						<select name="vmName" id="vmName" disabled required>
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
		<input type="submit" value="Submit Request" onclick="return submitRequest()"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
