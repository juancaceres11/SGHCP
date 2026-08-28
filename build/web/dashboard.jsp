<%-- 
    Document   : dashboard
    Created on : 26/08/2026, 2:47:17 p. m.
    Author     : Admin
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SGHCP - Dashboard</title>
    <link rel="stylesheet" href="Dashboard.css">
</head>
<body>
<%
    HttpSession sesion = request.getSession(false);
    String correo = (sesion != null) ? (String) sesion.getAttribute("correo") : null;
    if (correo == null) {
        response.sendRedirect("login.jsp");
    }
%>

<!-- Menú lateral -->
<nav class="menu lateral">
    <h1>SGHC - PODOLOGICAS</h1>
 <div class="contenedor-botones">
    <button class="btn Dashboard">Dashboard</button>
    <button class="btn Registrar">Registrar paciente</button>
    <button class="btn Cerrar">Cerrar sesión</button>
</div>

</nav>

<h1 class="contenido-principal">Pacientes Registrados</h1>

<form>
    <!-- Panel de búsqueda -->
    <label for="campo-busqueda"></label>
    <input type="search" id="campo-busqueda" name="q" placeholder="Buscar pacientes" required>
    <button type="submit">Buscar</button>
</form>

<!-- Tabla principal -->
<table>
    <thead>
        <tr>
            <th>N.#</th>
            <th>Nombre completo</th>
            <th>Documento</th>
            <th>Teléfono</th>
            <th>Última consulta</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>01</td>
            <td>Gisel Obando</td>
            <td>1006437546</td>
            <td>3212497655</td>
            <td>10/06/2026</td>
        </tr>
        <tr>
            <td>02</td>
            <td>Karla Umaña</td>
            <td>9876784</td>
            <td>3219512234</td>
            <td>10/06/2026</td>
        </tr>
        <tr>
            <td>03</td>
            <td>Sonia María</td>
            <td>1007890345</td>
            <td>3234567876</td>
            <td>10/06/2026</td>
        </tr>
    </tbody>
</table>

<script src="Dashboard.js"></script>
</body>
</html>
