package com.sghcp.controlador;

import com.sghcp.config.Permisos;
import com.sghcp.dao.PacienteDAO;
import com.sghcp.modelo.Paciente;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicios web de pacientes: listar y buscar (Dashboard), consultar uno (Perfil),
 * registrar y actualizar. Solo pueden usarlos el especialista y el auxiliar
 * administrativo; el administrador del sistema no accede a datos de pacientes.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private static final List<String> SEXOS = List.of("M", "F");
    private static final List<String> TIPOS_SANGRE =
            List.of("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");

    private final PacienteDAO pacienteDAO;

    public PacienteController(PacienteDAO pacienteDAO) {
        this.pacienteDAO = pacienteDAO;
    }

    /** GET /api/pacientes?q=texto — lista de pacientes; "q" es opcional y filtra la búsqueda. */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "q", defaultValue = "") String q,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion);
        if (acceso != null) {
            return acceso;
        }
        try {
            return ResponseEntity.ok(pacienteDAO.listar(q.trim()));
        } catch (SQLException e) {
            return errorInterno("listar pacientes", e);
        }
    }

    /** GET /api/pacientes/{id} — datos completos de un paciente (Perfil). */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable int id, HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion);
        if (acceso != null) {
            return acceso;
        }
        try {
            Paciente paciente = pacienteDAO.buscarPorId(id);
            if (paciente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(respuesta(false, "Paciente no encontrado"));
            }
            return ResponseEntity.ok(paciente);
        } catch (SQLException e) {
            return errorInterno("consultar paciente", e);
        }
    }

    /** POST /api/pacientes — registra un paciente nuevo y anota quién lo registró. */
    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestParam(value = "Nombre", required = false) String nombre,
            @RequestParam(value = "Apellidos", required = false) String apellidos,
            @RequestParam(value = "Fecha_Nacimiento", required = false) String fechaNacimiento,
            @RequestParam(value = "Tipo_Sangre", required = false) String tipoSangre,
            @RequestParam(value = "Identificacion", required = false) String identificacion,
            @RequestParam(value = "Telefono", required = false) String telefono,
            @RequestParam(value = "Sexo", required = false) String sexo,
            @RequestParam(value = "Ocupacion", required = false) String ocupacion,
            @RequestParam(value = "Direccion", required = false) String direccion,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion);
        if (acceso != null) {
            return acceso;
        }

        // Validación: la identificación es obligatoria al registrar
        if (vacio(identificacion)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Todos los campos obligatorios deben estar completos"));
        }
        // Validación: obligatorios, valores permitidos y formato de fecha
        String error = validarDatos(nombre, apellidos, fechaNacimiento, tipoSangre, sexo);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta(false, error));
        }

        try {
            // Validación: la identificación no puede estar repetida
            if (pacienteDAO.existeIdentificacion(identificacion)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(respuesta(false, "Ya existe un paciente con esa identificación"));
            }

            Paciente nuevo = armarPaciente(nombre, apellidos, fechaNacimiento, tipoSangre,
                    telefono, sexo, ocupacion, direccion);
            nuevo.setIdentificacion(identificacion);

            // Se envía el rol y el id de quien registra, para llenar "registro_paciente"
            int idNuevo = pacienteDAO.registrar(nuevo, Permisos.rol(sesion), Permisos.idUsuario(sesion));
            if (idNuevo > 0) {
                Map<String, Object> cuerpo = respuesta(true, "Paciente registrado correctamente");
                cuerpo.put("id", idNuevo);
                return ResponseEntity.status(HttpStatus.CREATED).body(cuerpo);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "No se pudo completar el registro"));

        } catch (SQLException e) {
            return errorInterno("registrar paciente", e);
        }
    }

    /** PUT /api/pacientes/{id} — actualiza los datos del paciente (la identificación no cambia). */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable int id,
            @RequestParam(value = "Nombre", required = false) String nombre,
            @RequestParam(value = "Apellidos", required = false) String apellidos,
            @RequestParam(value = "Fecha_Nacimiento", required = false) String fechaNacimiento,
            @RequestParam(value = "Tipo_Sangre", required = false) String tipoSangre,
            @RequestParam(value = "Telefono", required = false) String telefono,
            @RequestParam(value = "Sexo", required = false) String sexo,
            @RequestParam(value = "Ocupacion", required = false) String ocupacion,
            @RequestParam(value = "Direccion", required = false) String direccion,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion);
        if (acceso != null) {
            return acceso;
        }

        String error = validarDatos(nombre, apellidos, fechaNacimiento, tipoSangre, sexo);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta(false, error));
        }

        try {
            if (pacienteDAO.buscarPorId(id) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(respuesta(false, "Paciente no encontrado"));
            }

            Paciente datos = armarPaciente(nombre, apellidos, fechaNacimiento, tipoSangre,
                    telefono, sexo, ocupacion, direccion);

            if (pacienteDAO.actualizar(id, datos)) {
                return ResponseEntity.ok(respuesta(true, "Datos del paciente actualizados correctamente"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(respuesta(false, "No se pudo actualizar el paciente"));

        } catch (SQLException e) {
            return errorInterno("actualizar paciente", e);
        }
    }

    // ---- Métodos de apoyo ----

    /**
     * Control de acceso: devuelve null si el usuario puede continuar,
     * 401 si no hay sesión y 403 si su rol no tiene permiso sobre pacientes.
     */
    private ResponseEntity<Map<String, Object>> verificarAcceso(HttpSession sesion) {
        if (!Permisos.hayLogin(sesion)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta(false, "Debe iniciar sesión"));
        }
        if (!Permisos.tieneRol(sesion, Permisos.ESPECIALISTA, Permisos.AUXILIAR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(respuesta(false, "No tiene permisos para esta acción"));
        }
        return null;
    }

    /** Valida los datos comunes del paciente. Devuelve el mensaje de error, o null si todo está bien. */
    private String validarDatos(String nombre, String apellidos, String fechaNacimiento,
                                String tipoSangre, String sexo) {
        if (vacio(nombre) || vacio(apellidos) || vacio(fechaNacimiento)
                || vacio(tipoSangre) || vacio(sexo)) {
            return "Todos los campos obligatorios deben estar completos";
        }
        if (!SEXOS.contains(sexo)) {
            return "El sexo debe ser M o F";
        }
        if (!TIPOS_SANGRE.contains(tipoSangre)) {
            return "Tipo de sangre no válido";
        }
        try {
            LocalDate.parse(fechaNacimiento);
        } catch (DateTimeParseException e) {
            return "La fecha de nacimiento debe tener formato AAAA-MM-DD";
        }
        return null;
    }

    private Paciente armarPaciente(String nombre, String apellidos, String fechaNacimiento,
                                   String tipoSangre, String telefono, String sexo,
                                   String ocupacion, String direccion) {
        Paciente p = new Paciente();
        p.setNombre(nombre);
        p.setApellidos(apellidos);
        p.setFechaNacimiento(fechaNacimiento);
        p.setTipoSangre(tipoSangre);
        p.setTelefono(telefono);
        p.setSexo(sexo);
        p.setOcupacion(ocupacion);
        p.setDireccion(direccion);
        return p;
    }

    private ResponseEntity<Map<String, Object>> errorInterno(String accion, SQLException e) {
        System.err.println("Error al " + accion + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta(false, "Error interno del servidor"));
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