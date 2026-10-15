package com.sghcp.controlador;

import com.sghcp.dao.EspecialistaDAO;
import com.sghcp.modelo.Especialista;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicios web de inicio de sesión, cierre de sesión y registro de especialistas.
 * Es la puerta de entrada de la API: recibe las peticiones (por ejemplo desde Postman
 * o React), delega en el DAO y responde siempre en JSON.
 */
@RestController
@RequestMapping("/api")
public class EspecialistaController {

    private final EspecialistaDAO especialistaDAO;

    public EspecialistaController(EspecialistaDAO especialistaDAO) {
        this.especialistaDAO = especialistaDAO;
    }

    /** POST /api/login — valida correo y tarjeta profesional y abre la sesión. */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(
            @RequestParam(value = "Correo", required = false) String correo,
            @RequestParam(value = "Tarjeta_Profesional", required = false) String tarjetaProfesional,
            HttpSession sesion) {

        // Validación: ningún dato puede llegar vacío
        if (estaVacio(correo) || estaVacio(tarjetaProfesional)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Correo y tarjeta profesional son obligatorios"));
        }

        try {
            Especialista especialista = especialistaDAO.validarLogin(correo, tarjetaProfesional);

            // Credenciales incorrectas
            if (especialista == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(respuesta(false, "Error en la autenticación"));
            }

            // Credenciales correctas: se guarda en la sesión quién inició sesión
            sesion.setAttribute("idEspecialista", especialista.getIdEspecialista());
            sesion.setAttribute("correo", especialista.getCorreo());

            Map<String, Object> cuerpo = respuesta(true, "Autenticación satisfactoria");
            cuerpo.put("idEspecialista", especialista.getIdEspecialista());
            cuerpo.put("nombre", especialista.getNombre());
            cuerpo.put("correo", especialista.getCorreo());
            return ResponseEntity.ok(cuerpo);

        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "Error interno del servidor"));
        }
    }

    /** POST /api/logout — destruye la sesión en el servidor. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return ResponseEntity.ok(respuesta(true, "Sesión cerrada correctamente"));
    }

    /** POST /api/especialistas — registra un nuevo especialista. */
    @PostMapping("/especialistas")
    public ResponseEntity<Map<String, Object>> registrarEspecialista(
            @RequestParam(value = "Nombre", required = false) String nombre,
            @RequestParam(value = "Apellidos", required = false) String apellidos,
            @RequestParam(value = "Tarjeta_Profesional", required = false) String tarjetaProfesional,
            @RequestParam(value = "Correo", required = false) String correo,
            @RequestParam(value = "Especialidad", required = false) String especialidad,
            @RequestParam(value = "Telefono", required = false) String telefono) {

        // Validación: los campos obligatorios no pueden llegar vacíos
        if (estaVacio(nombre) || estaVacio(apellidos)
                || estaVacio(tarjetaProfesional) || estaVacio(correo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Todos los campos obligatorios deben estar completos"));
        }

        try {
            // Validación: el correo no puede estar repetido
            if (especialistaDAO.existeCorreo(correo)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(respuesta(false, "Ya existe un especialista registrado con ese correo"));
            }

            Especialista nuevo = new Especialista();
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos);
            nuevo.setTarjetaProfesional(tarjetaProfesional);
            nuevo.setCorreo(correo);
            nuevo.setEspecialidad(especialidad);
            nuevo.setTelefono(telefono);

            if (especialistaDAO.registrar(nuevo)) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(respuesta(true, "Registro realizado correctamente"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "No se pudo completar el registro"));

        } catch (SQLException e) {
            System.err.println("Error en registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "Error interno del servidor"));
        }
    }

    // ---- Métodos de apoyo ----

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Construye el JSON base de respuesta: success + mensaje. */
    private Map<String, Object> respuesta(boolean exito, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("success", exito);
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}