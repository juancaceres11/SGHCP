<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>Registrar paciente</title>
  <link rel="stylesheet" href="registrar paciente.css">
</head>

<body>
                                    <!-- Menú lateral -->
  <nav class="menu lateral">
    <h1>SGHC - PODOLOGICAS</h1>
    <div class="contenedor-botones">
      <button class="Dashboard">Dashboard</button>
      <button class="Registrar">Registrar paciente</button>
      <button class="Cerrar">Cerrar sesión</button>
    </div>
  </nav>


                                    <!-- Contenido principal -->
  <div class="contenido-principal">
    <h1>Registrar paciente</h1>

    <!-- Formulario -->
    <form class="formulario-registro" action="RegistrarPacienteServlet" method="post">


      <div class="grupo-campo">
        <label for="nombre">Nombre</label>
        <input type="text" id="Nombre" name="Nombre" placeholder="Ej. Julian" required>
      </div>

      <div class="grupo-campo">
        <label for="apellidos">Apellidos</label>
        <input type="text" id="Apellidos" name="Apellidos" placeholder="Ej. Obando Peña" required>
      </div>

      <div class="grupo-campo">
        <label for="fecha-nacimiento">Fecha de Nacimiento</label>
        <input type="date" id="Fecha_Nacimiento" name="Fecha_Nacimiento" required>
      </div>
<!--   como ya se habia definido el formulario en evidencias pasadas, se creo sin tener en cuenta los parametros de la base de datos-error
      <div class="grupo-campo">
        <label for="tipo-id">Tipo de Identificación</label>
        <select id="tipo-id" name="tipo_id" required>
          <option value="">Seleccione una opción...</option>
          <option value="CC">Cédula de Ciudadanía</option>
          <option value="TI">Tarjeta de Identidad</option>
          <option value="CE">Cédula de Extranjería</option>
          <option value="PA">Pasaporte</option>
        </select>
      </div> por ahora lo quiero dejar como comentario con el fin que se pueda implementar mas campos en el formulario, por ahora no lo utilizaremos para el desarrollo de esta evidencia
-->

<div class="grupo-campo">
  <label for="Tipo_sangre">Tipo de Sangre</label>
  <select id="Tipo_Sangre" name="Tipo_Sangre" required>
    <option value="">Seleccione...</option>
    <option value="A+">A+</option>
    <option value="A-">A-</option>
    <option value="B+">B+</option>
    <option value="B-">B-</option>
    <option value="O+">O+</option>
    <option value="O-">O-</option>
    <option value="AB+">AB+</option>
    <option value="AB-">AB-</option>
  </select>
</div>


      <div class="grupo-campo">
        <label for="numero-id">Número de Identificación</label>
        <input type="number" id="numero-id" name="Identificacion" required>
      </div>

      <div class="grupo-campo">
        <label for="telefono">Teléfono</label>
        <input type="tel" id="Telefono" name="Telefono" placeholder="Ej. 1234567890">
      </div>

    <div class="grupo-campo">
     <label for="sexo">Sexo</label>
     <select id="Sexo" name="Sexo" required>
    <option value="">Seleccione...</option>
    <option value="M">Masculino</option>
    <option value="F">Femenino</option>
     </select>
    </div>

      <div class="grupo-campo">
        <label for="ocupacion">Ocupación</label>
        <input type="text" id="Ocupacion" name="Ocupacion">
      </div>

      <div class="grupo-campo fila-completa">
        <label for="direccion">Dirección</label>
        <input type="text" id="Direccion" name="Direccion">
      </div>

      <button type="submit" class="guardar">Guardar</button>
    </form>
  </div>

  <script src="registrar paciente.js"></script>
</body>
</html>
