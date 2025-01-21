<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<meta http-equiv="refresh" content="<%=session.getMaxInactiveInterval()%>;url=/ITL/login.jsp" />
<link rel="icon" href="ITL.ico" />
<head>
	<title>ICP Tech Lab - Update Config Property Table</title>
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
<form id="configproperty" action="/ITL/configProperty" method="post">
<%--<form action="/ITL/configProperty" method="post">--%>
	<section>
		<div class="uitkPanel">
			<p>Update Config Property Records - Enter the data, make your choices and click submit</p>
			<table width="80%">
				<tbody>
<%--					<tr align="center">--%>
<%--						<td align="right">Enter the Product:</td>--%>
<%--						<td align="left">--%>
<%--							<select name="product" id="product" required>--%>
<%--								<option value=""></option>--%>
<%--								<option value="CM">CM</option>--%>
<%--								<option value="CES">CES</option>--%>
<%--							</select>--%>
<%--						</td>--%>
<%--					</tr>--%>
<%--					<tr align="center">--%>
<%--						<td align="right">Enter the Product Version:</td>--%>
<%--						<td align="left">--%>
<%--							<select name="version" id="version" required>--%>
<%--								<option value=""></option>--%>
<%--								<option value="5.4">5.4</option>--%>
<%--								<option value="6.0">6.0</option>--%>
<%--							</select>--%>
<%--						</td>--%>
<%--					</tr>--%>

<%--					<tr align="center">--%>
<%--						<td align="right">Enter the Product Version:</td>--%>
<%--						<td align="left">--%>
<%--							<label for="myfile">Select a file:</label>--%>
<%--							<input type="file" id="myfile" name="myfile"><br><br>--%>
<%--						</td>--%>
<%--					</tr>--%>

					<tr align="center">
						<td align="right">Enter the Property Group:</td>
						<td align="left">
							<select name="propertyGroup" id="propertyGroup" required>
								<option value=""></option>
								<option value="base">Base Version</option>
								<option value="kb">Knowledge Base</option>
								<option value="ddrkb">DDR Knowledge Base</option>
								<option value="cu">Cumulative Update</option>
								<option value="lcd">National Coverage Determination</option>
								<option value="template">VSphire Template</option>
							</select>
						</td>
					</tr>
					<tr align="center">
						<td align="right">Enter the Property Name:</td>
<%--						<td align="left"><input type="text" onblur="validatePropertyName()" name="propertyName" id="propertyName" width="100" required/></td>--%>
						<td align="left"><input type="text" name="propertyName" id="propertyName" width="100" required/></td>
					</tr>
					<tr align="center">
						<td align="right">Enter the Property Value:</td>
						<td align="left"><input type="text" name="propertyValue" id="propertyValue" width="50" required/></td>
					</tr>

					<script>
						// Function to change label text with HTML content
						function changeTextWithHTML() {
							let labelElement = document
									.getElementById("labelWithHTML");
							labelElement.innerHTML =
									"<em>New Text</em> using <strong>innerHTML</strong>";
						}
					</script>

				</tbody>
			</table>
		</div>
	</section>
	<section>
		<br>
		<div style="text-align:center;">
			<input type="hidden" id="requestType" name="requestType" value="updateConfigProperty">
			<input type="submit" value="Update Config Property" />
			<input type="reset" value="Reset"/>
		</div>
	</section>

	<script>
		function validatePropertyName() {
			const regex = /^KB_\d{4}_[A-Z0-9]+_\d+\.\d+-\d+\.\d+$/;
			const propertyName = document.getElementById("propertyName").value;
			let isValid = regex.test(propertyName);

			if (isValid) {
				alert("PropertyName is valid!");
			} else {
				document.getElementById("propertyName").focus();
				alert("PropertyName is invalid!");

				propertyName.value = '';
				propertyName.focus();
				isValid = true;
			}
		}

		function updatePropertyNameText() {
			alert("PropertyGroup = " + document.getElementById('propertyGroup').value);
			document.getElementById('configproperty').innerHTML = propertyGroup.value;
		}

	</script>
</form>
<p style="color:red">${responseMessage}</p>
</body>
</html>
