package com.sghcp.controlador;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servicios web de pacientes: listar y buscar (Dashboard), consultar uno (Perfil)
 * y registrar. Todos exigen que el especialista haya iniciado sesión.
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

        if (!hayLogin(sesion)) {
            return noAutorizado();
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

        if (!hayLogin(sesion)) {
            return noAutorizado();
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

    /** POST /api/pacientes — registra un paciente nuevo. */
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

        if (!hayLogin(sesion)) {
            return noAutorizado();
        }

        // Validación 1: campos obligatorios
        if (vacio(nombre) || vacio(apellidos) || vacio(fechaNacimiento)
                || vacio(tipoSangre) || vacio(identificacion) || vacio(sexo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Todos los campos obligatorios deben estar completos"));
        }
        // Validación 2: valores permitidos
        if (!SEXOS.contains(sexo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "El sexo debe ser M o F"));
        }
        if (!TIPOS_SANGRE.contains(tipoSangre)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "Tipo de sangre no válido"));
        }
        // Validación 3: la fecha debe tener formato AAAA-MM-DD
        try {
            LocalDate.parse(fechaNacimiento);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(respuesta(false, "La fecha de nacimiento debe tener formato AAAA-MM-DD"));
        }

        try {
            // Validación 4: la identificación no puede estar repetida
            if (pacienteDAO.existeIdentificacion(identificacion)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(respuesta(false, "Ya existe un paciente con esa identificación"));
            }

            Paciente nuevo = new Paciente();
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos);
            nuevo.setFechaNacimiento(fechaNacimiento);
            nuevo.setTipoSangre(tipoSangre);
            nuevo.setIdentificacion(identificacion);
            nuevo.setTelefono(telefono);
            nuevo.setSexo(sexo);
            nuevo.setOcupacion(ocupacion);
            nuevo.setDireccion(direccion);

            int idNuevo = pacienteDAO.registrar(nuevo);
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

    // ---- Métodos de apoyo ----

    /** Hay login si el controlador de acceso dejó el id del especialista en la sesión. */
    private boolean hayLogin(HttpSession sesion) {
        return sesion.getAttribute("idEspecialista") != null;
    }

    private ResponseEntity<Map<String, Object>> noAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(respuesta(false, "Debe iniciar sesión"));
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