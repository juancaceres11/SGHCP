package com.sghcp.controlador;

import com.sghcp.config.Permisos;
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
 * Servicio web de registro de especialistas. Solo el administrador del sistema
 * puede crear cuentas de especialistas.
 */
@RestController
@RequestMapping("/api")
public class EspecialistaController {

    private final EspecialistaDAO especialistaDAO;

    public EspecialistaController(EspecialistaDAO especialistaDAO) {
        this.especialistaDAO = especialistaDAO;
    }

    /** POST /api/especialistas — registra un nuevo especialista (solo ADMIN). */
    @PostMapping("/especialistas")
    public ResponseEntity<Map<String, Object>> registrarEspecialista(
            @RequestParam(value = "Nombre", required = false) String nombre,
            @RequestParam(value = "Apellidos", required = false) String apellidos,
            @RequestParam(value = "Tarjeta_Profesional", required = false) String tarjetaProfesional,
            @RequestParam(value = "Correo", required = false) String correo,
            @RequestParam(value = "Especialidad", required = false) String especialidad,
            @RequestParam(value = "Telefono", required = false) String telefono,
            HttpSession sesion) {

        // Control de acceso: sin sesión -> 401; con sesión pero sin ser ADMIN -> 403
        if (!Permisos.hayLogin(sesion)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta(false, "Debe iniciar sesión"));
        }
        if (!Permisos.tieneRol(sesion, Permisos.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(respuesta(false, "No tiene permisos para esta acción"));
        }

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

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Map<String, Object> respuesta(boolean exito, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("success", exito);
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}