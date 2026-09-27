window.addEventListener("pageshow", function (event) {
  if (event.persisted) {
    window.location.reload();
  }
});// Ayuda a recargar la pagina, el JSP vuelve a ejecutar la validación de sesión, y si se cerró sesión,nos manda al login correctamente.
document.addEventListener("DOMContentLoaded", () => {
  // Seleccionamos los botones por su clase
  const btnDashboard = document.querySelector(".Dashboard");
  const btnCerrar = document.querySelector(".Cerrar");
  const btnRegistrar = document.querySelector(".Registrar");
  

  // Acción al hacer clic en "Dashboard"
  btnDashboard.addEventListener("click", () => {
    window.location.href = "dashboard.jsp"; // lleva al módulo de dashboard
  });

  btnCerrar.addEventListener("click", () => {
  window.location.href = "LogoutServlet"; //  se destruye la sesión real
  });

  // Acción al hacer clic en "Registrar paciente"
  btnRegistrar.addEventListener("click", () => {
    window.location.href = "RegistrarPaciente.jsp"; // carga el módulo de registro
  });
});
