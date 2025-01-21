<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<link rel="icon" href="ITL.ico" />
	<head>
		<title>ICP Tech Lab - Login</title>
		<link type="text/css" rel="stylesheet" href="uitk.css">
		<link type="text/css" rel="stylesheet" href="itl.css">
	</head>
	<body>
	<header>
		<h1><span>ICP Tech Lab</span></h1>
	</header>
		<section>
			<div class="login">
				<div class="narrow">
					<br>
					<br>
					<br>
					<br>
					<br>
					<br>
					<br>
					<br>
					<br>
					<br>
					<center><p><h2>Login</h2></p></center>
					<center><form name="form" action="/ITL/login" method="post">
						<p>&nbsp;&nbsp;&nbsp; UserId:&nbsp; <input type="text" name="userid" width="30" required/></p>
						<p>Password: <input type="password" name="password" width="30" required/></p>
						<input type="submit" value="Login"/>
					</form></center>
					<center><p style="color:red">${errorMessage}</p></center>
				</div>
			</div>
		</section>
	</body>
</html>
