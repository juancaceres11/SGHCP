<%-- 
    Document   : login
    Created on : 26/08/2026, 2:46:26 p. m.
    Author     : Admin
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SGHCP - Inicio de Sesión</title>
    <link rel="stylesheet" href="inicio_sesion.css">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <h1>SGHCP - PODOLOGICAS</h1>
            
            <!-- Formulario apunta al servlet -->
            <form action="LoginServlet" method="post">
                <input type="email" id="correo" name="Correo" placeholder="Ingrese su correo" required>
                <input type="password" id="tarjeta" name="Tarjeta_Profesional" placeholder="Ingrese su contraseña" required>

                <a href="#" class="forgot">¿Olvidaste tu contraseña?</a>
                <button type="submit">Ingresar</button>
            </form>

            <!-- Mostrar mensaje de error si viene ?error=1 -->
            <%
                String error = request.getParameter("error");
                if (error != null && error.equals("1")) {
            %>
                <p style="color:red; text-align:center;">Credenciales inválidas. Intente nuevamente.</p>
            <%
                }
            %>
        </div>
    </div>
    <script src="inicio_sesion.js"></script>
</body>
</html>
