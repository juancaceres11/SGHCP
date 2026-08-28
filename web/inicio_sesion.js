document.addEventListener("DOMContentLoaded", () => {
  const forgotLink = document.querySelector(".forgot");

  // Acción al hacer clic en "¿Olvidaste tu contraseña?"
  forgotLink.addEventListener("click", (event) => {
    event.preventDefault();
    alert("Por favor contacta al administrador para recuperar tu contraseña.");
  });
});
