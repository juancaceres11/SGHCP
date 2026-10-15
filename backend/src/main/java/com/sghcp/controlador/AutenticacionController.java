package com.sghcp.controlador;

import com.sghcp.dao.AutenticacionDAO;
import com.sghcp.modelo.UsuarioAutenticado;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicios web de autenticación para los tres roles del sistema
 * (especialista, auxiliar administrativo y administrador).
 */
@RestController
@RequestMapping("/api")
public class AutenticacionController {

    private final AutenticacionDAO autenticacionDAO;

    public AutenticacionController(AutenticacionDAO autenticacionDAO) {
        this.autenticacionDAO = autenticacionDAO;
    }

    /** POST /api/login — valida correo y clave, y abre la sesión con el rol del usuario. */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(
            @RequestParam(value = "Correo", required = false) String correo,
            @RequestParam(value = "Clave", required = false) String clave,
            HttpSession sesion) {

        // Validación: ningún dato puede llegar vacío
        if (vacio(correo) || vacio(clave)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Correo y clave son obligatorios"));
        }

        try {
            UsuarioAutenticado usuario = autenticacionDAO.autenticar(correo, clave);

            // Credenciales incorrectas
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(respuesta(false, "Error en la autenticación"));
            }

            // Se limpia cualquier dato de una sesión anterior antes de guardar el nuevo
            sesion.removeAttribute("idEspecialista");

            // Se guarda en la sesión quién es el usuario y qué rol tiene
            sesion.setAttribute("rol", usuario.rol());
            sesion.setAttribute("idUsuario", usuario.id());
            sesion.setAttribute("nombre", usuario.nombre());
            sesion.setAttribute("correo", usuario.correo());

            // Los endpoints de pacientes (versión actual) buscan este atributo
            if ("ESPECIALISTA".equals(usuario.rol())) {
                sesion.setAttribute("idEspecialista", usuario.id());
            }

            Map<String, Object> cuerpo = respuesta(true, "Autenticación satisfactoria");
            cuerpo.put("rol", usuario.rol());
            cuerpo.put("id", usuario.id());
            cuerpo.put("nombre", usuario.nombre());
            cuerpo.put("correo", usuario.correo());
            return ResponseEntity.ok(cuerpo);

        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "Error interno del servidor"));
        }
    }

    /** GET /api/sesion — informa quién tiene la sesión abierta (sirve para el frontend y las pruebas). */
    @GetMapping("/sesion")
    public ResponseEntity<Map<String, Object>> sesionActual(HttpSession sesion) {
        Object rol = sesion.getAttribute("rol");
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta(false, "No hay sesión activa"));
        }
        Map<String, Object> cuerpo = respuesta(true, "Sesión activa");
        cuerpo.put("rol", rol);
        cuerpo.put("id", sesion.getAttribute("idUsuario"));
        cuerpo.put("nombre", sesion.getAttribute("nombre"));
        cuerpo.put("correo", sesion.getAttribute("correo"));
        return ResponseEntity.ok(cuerpo);
    }

    /** POST /api/logout — destruye la sesión en el servidor. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return ResponseEntity.ok(respuesta(true, "Sesión cerrada correctamente"));
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Map<String, Object> respuesta(boolean exito, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("success", exito);
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}