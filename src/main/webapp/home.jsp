<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<title>Autenticação</title>

</head>

<body>

	<br />
	<br />
	<br />
	<hr >
	<br />
	<br />
	<br />
	<br />
	<br />
	<form action="j_spring_security_check" method="post">
		<CENTER>
		<div style="width: 50%">
		<fieldset>
			<legend>
				Monitoramento Caderneta
			</legend>
			
			<div style="background-color: #379BC6">
			<br />
			<table>
				<tr>
					<td>
						<%
							if(request.getParameter("error") != null){
							if (request.getParameter("error").equals("invalido")){
						%>
					
						<p>
							<span style="color:red">
								<strong>Usuário ou Senha invalidos</strong>
							</span>
						</p>
					
						<%
					
							} //fim do if equals
							
							}//fim do if null
					
						%>
					</td>
				</tr>
				<tr>
					<td style="color: #FFFFFF">
						<strong>Usuário:</strong>
					</td>
					<td>
						 <input name="j_username" type="text" value="${not empty login_error ? SPRING_SECURITY_LAST_USERNAME : ''}" />
					</td>
				</tr>
				<tr>
					<td style="color: #FFFFFF">
						<strong>Senha:</strong>
					</td>
					<td>
						 <input type="password" name="j_password">
					</td>
					<td>
						<input type="submit" value="Efetuar Login">
					</td>
				</tr>
			</table>
			<br />
			</div>
		</fieldset>
		</div>
		</CENTER>
	</form>
	
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<hr>
</body>

</html>
