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
    window.location.href = "RegistrarPaciente.jsp"; // asegúrate de crear este JSP
  });

  // Acción al hacer clic en "Cerrar sesión"
  btnCerrar.addEventListener("click", () => {
    window.location.href = "login.jsp"; // vuelve al inicio de sesión
  });
});
