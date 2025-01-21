<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Product Install Request</title>
	<meta charset="UTF-8">
	<link type="text/css" rel="stylesheet" href="uitk.css">
	<link type="text/css" rel="stylesheet" href="itl.css">
	<script>
		/*function enableTemplate() {*/
		/*	if(document.getElementById("newVM").checked) {*/
		/*		document.getElementById("template").disabled = false;*/
		/*	} else {*/
		/*		document.getElementById("template").disabled = true;*/
		/*	}*/
		/*}*/
		function clearFiles() {
			document.getElementById("ddr_engine_file").value = "";
			document.getElementById("kb_file").value = "";
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
<section>
<form action="/ITL/request" method="post">
	<section>
	<div class="uitkPanel">
		<p>Product Installation Request - make your choices and click submit</p>
		<table width="80%">
			<tbody>
				<tr align="center">
					<td align="right">Enter the VM name:</td>
					<td align="left"><input type="text" name="vmName" width="16" required/></td>
				</tr>
				<tr align="center">
					<td align="right">Choose if request is for a new product release or a product upgrade, or leave unchecked if neither:</td>
					<td align="left">
						<input type='radio' name='newOrUpgradeProduct' value='newRelease'>New Product Release
						<input type='radio' name='newOrUpgradeProduct' value='upgradeRelease'>Upgrade Product
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a new product install, choose a product release:</td>
					<td align="left">
						<select name="product_install" id="product_install">
							<option value=""></option>
							<option value="ClaimsManager_5.4_SP2-CU03_Base_Install.zip">CM 5.4 SP2 CU03</option>
							<option value="ClaimsManager_5.4_SP2-CU02.3_Base_Install__1.18.0.2.02.3-6004.zip">CM 5.4 SP2 CU02.3</option>
							<option value="ClaimsManager_5.4_SP2-CU02.2_Base_Install__1.18.0.2.02.2-6001.zip">CM 5.4 SP2 CU02.2</option>
							<option value="CM54_SP1-CU05-Base-5799">CM 5.4 SP1 CU05</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a product upgrade, choose a product release:</td>
					<td align="left">
						<select name="product_upgrade" id="product_upgrade">
							<option value=""></option>
							<option value="CM_5.4-SP2_CU03.1.zip">CM 5.4 SP2 CU03.1</option>
							<option value="CM_5.4-SP2_CU03.zip">CM 5.4 SP2 CU03</option>
							<option value="CM_5.4-SP2_CU02.3.zip">CM 5.4 SP2 CU02.3</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">Choose if request is to load a KB with a merged DDR engine, or to load a production KB:</td>
					<td align="left">
						<input type='radio' name='mergeOrProdKB' value='mergeKB'>Load a KB with a merged DDR Engine
						<input type='radio' name='mergeOrProdKB' value='prodKB'>Load a production KB
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a DDR Engine merge, select the DDR Engine zip file:</td>
					<input type="hidden" id="ddr_engine_hidden" name="ddr_engine_hidden">
					<td align="left"><input type="file" id="ddr_engine_file" onchange="if (document.getElementById('ddr_engine_hidden').value) document.getElementById('ddr_engine_hidden').value = this.files.length > 0 ? this.files[0].name : ''"/>File must exist in \\otl-svm0.otl.lab\icp-wvc\provision-files</td>
				</tr>
				<tr align="center">
					<td align="right">For a DDR Engine merge into a KB, select KB zip file:</td>
					<td align="left"><input type="file" id="kb_file" onchange="document.getElementById('kb_file').textContent = this.files.length > 0 ? this.files[0].name : ''"/>File must exist in \\otl-svm0.otl.lab\icp-wvc\provision-files</td>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a production KB load, choose a KB:</td>
					<td align="left">
						<select name="kb" id="kb">
							<option value=""></option>
							<option value="CM_KB_2020_Q2C_5.0-5.4.zip">CM_KB_2020_Q2C</option>
							<option value="CM_KB_2020_Q2B_5.0-5.4.zip">CM_KB_2020_Q2B</option>
							<option value="CM_KB_2020_Q2A_5.0-5.4.zip">CM_KB_2020_Q2A</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a PE LCD load, choose a PE LCD:</td>
					<td align="left">
						<select name="pe_lcd" id="pe_lcd">
							<option value=""></option>
							<option value="0_B_20200515_5.X.zip">0_B_20200515_5.X.zip</option>
							<option value="0_B_20200305_5.X.zip">0_B_20200305_5.X.zip</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">For a FE LCD load, choose a FE LCD:</td>
					<td align="left">
						<select name="fe_lcd" id="fe_lcd">
							<option value=""></option>
							<option value="0_A_20200515_5.X.zip">0_A_20200515_5.X.zip</option>
							<option value="0_A_20200305_5.X.zip">0_A_20200305_5.X.zip</option>
						</select>
					</td>
				</tr>
				<tr align="center">
					<td align="right">Choose if iLog system rules are to be installed:</td>
					<td align="left"><input type="checkbox" id="ilog_system_rules" name="ilog_system_rules" value="no"/></td>
				</tr>
			</tbody>
		</table>
	</div>
</section>
<section>
	<br>
	<div style="text-align:center;">
		<input type="hidden" id="requestType" name="requestType" value="productInstall">
		<input type="submit" value="Submit Request"/>
		<input type="reset" value="Reset"/>
	</div>
</section>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
