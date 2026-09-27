import { useState } from "react";
import "./Login.css";

function Login() {
  const [correo, setCorreo] = useState("");
  const [tarjetaProfesional, setTarjetaProfesional] = useState("");
  const [error, setError] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(false);

    const formData = new URLSearchParams();
    formData.append("Correo", correo);
    formData.append("Tarjeta_Profesional", tarjetaProfesional);

    try {
      const res = await fetch("/LoginServlet", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: formData,
      });

      const data = await res.json();

      if (data.success) {
        window.location.href = "http://localhost:8080/sghcp_inicio_sesion/dashboard.jsp";
      } else {
        setError(true);
      }
    } catch (err) {
      setError(true);
    }
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    alert("Por favor contacta al administrador para recuperar tu contraseña.");
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>SGHCP - PODOLOGICAS</h1>

        <form onSubmit={handleSubmit}>
          <input
            type="email"
            id="correo"
            name="Correo"
            placeholder="Ingrese su correo"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            required
          />
          <input
            type="password"
            id="tarjeta"
            name="Tarjeta_Profesional"
            placeholder="Ingrese su contraseña"
            value={tarjetaProfesional}
            onChange={(e) => setTarjetaProfesional(e.target.value)}
            required
          />

          <a href="#" className="forgot" onClick={handleForgotPassword}>
            ¿Olvidaste tu contraseña?
          </a>
          <button type="submit">Ingresar</button>
        </form>

        {error && (
          <p style={{ color: "red", textAlign: "center" }}>
            Credenciales inválidas. Intente nuevamente.
          </p>
        )}
      </div>
    </div>
  );
}

export default Login;