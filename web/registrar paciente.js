document.addEventListener("DOMContentLoaded", () => {
  // Seleccionamos los botones por su clase
  const btnDashboard = document.querySelector(".Dashboard");
  const btnRegistrar = document.querySelector(".Registrar");
  const btnCerrar = document.querySelector(".Cerrar");

  // Acción al hacer clic en "Dashboard"
  btnDashboard.addEventListener("click", () => {
    window.location.href = "dashboard.jsp"; // lleva al módulo de dashboard
  });

  // Acción al hacer clic en "Cerrar sesión"
  btnCerrar.addEventListener("click", () => {
    window.location.href = "login.jsp"; // vuelve al inicio de sesión
  });

  // Acción al hacer clic en "Registrar paciente"
  btnRegistrar.addEventListener("click", () => {
    window.location.href = "RegistrarPaciente.jsp"; // carga el módulo de registro
  });
});
