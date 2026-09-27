window.addEventListener("pageshow", function (event) {
  if (event.persisted) {
    window.location.reload();
  }
});//Ayuda a recargar la pagina, el JSP vuelve a ejecutar la validación de sesión, y si se cerró sesión,nos manda al login correctamente.
document.addEventListener("DOMContentLoaded", () => {
  // Seleccionamos los botones
  const btnDashboard = document.querySelector(".Dashboard");
  const btnRegistrar = document.querySelector(".Registrar");
  const btnCerrar = document.querySelector(".Cerrar");

  // Acción al hacer clic en "Dashboard"
  btnDashboard.addEventListener("click", () => {
    window.location.href = "dashboard.jsp"; // recarga o lleva al dashboard
  });

  // Acción al hacer clic en "Registrar paciente"
  btnRegistrar.addEventListener("click", () => {
    window.location.href = "RegistrarPaciente.jsp"; 
  });

  // Acción al hacer clic en "Cerrar sesión"
  btnCerrar.addEventListener("click", () => {
  window.location.href = "LogoutServlet"; // ahora se destruye la sesión real
});
});
