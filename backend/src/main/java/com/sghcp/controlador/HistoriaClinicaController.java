package com.sghcp.controlador;

import com.sghcp.config.Permisos;
import com.sghcp.dao.HistoriaClinicaDAO;
import com.sghcp.dao.PacienteDAO;
import com.sghcp.modelo.HistoriaClinica;
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
 * Servicios web de historias clínicas. Solo el especialista puede crearlas, y solo el
 * especialista que la creó puede cerrarla o cambiar su consentimiento. El especialista y el
 * auxiliar pueden consultarlas. El administrador del sistema no tiene acceso a información clínica.
 * No existe ningún servicio para eliminar historias: se conservan siempre.
 */
@RestController
@RequestMapping("/api")
public class HistoriaClinicaController {

    private static final List<String> ESTADOS = List.of("Abierta", "Cerrada", "Pendiente");
    private static final List<String> CONSENTIMIENTOS = List.of("Firmado", "No Firmado", "Revocado");
    private static final String SOLO_AUTOR =
            "Solo el especialista que creó la historia clínica puede modificarla";

    private final HistoriaClinicaDAO historiaDAO;
    private final PacienteDAO pacienteDAO;

    public HistoriaClinicaController(HistoriaClinicaDAO historiaDAO, PacienteDAO pacienteDAO) {
        this.historiaDAO = historiaDAO;
        this.pacienteDAO = pacienteDAO;
    }

    /** POST /api/pacientes/{idPaciente}/historias — crea una historia clínica (solo ESPECIALISTA). */
    @PostMapping("/pacientes/{idPaciente}/historias")
    public ResponseEntity<?> crear(
            @PathVariable int idPaciente,
            @RequestParam(value = "Motivo_Consulta", required = false) String motivo,
            @RequestParam(value = "Antecedentes_Medicos", required = false) String antecedentes,
            @RequestParam(value = "Diagnostico", required = false) String diagnostico,
            @RequestParam(value = "Fecha_Consulta", required = false) String fechaConsulta,
            @RequestParam(value = "Estado", required = false) String estado,
            @RequestParam(value = "Observaciones", required = false) String observaciones,
            @RequestParam(value = "Fecha_Firma", required = false) String fechaFirma,
            @RequestParam(value = "Tipo_Procedimiento", required = false) String tipoProcedimiento,
            @RequestParam(value = "Estado_Consentimiento", required = false) String consentimiento,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA);
        if (acceso != null) {
            return acceso;
        }

        // Validación 1: campos obligatorios
        if (vacio(motivo) || vacio(tipoProcedimiento) || vacio(consentimiento)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Motivo de consulta, tipo de procedimiento y estado del consentimiento son obligatorios");
        }
        if (tipoProcedimiento.trim().length() > 50) {
            return error(HttpStatus.BAD_REQUEST, "El tipo de procedimiento admite máximo 50 caracteres");
        }

        // Validación 2: valores permitidos (si no se envía el estado, queda Abierta)
        String estadoFinal = vacio(estado) ? "Abierta" : estado;
        if (!ESTADOS.contains(estadoFinal)) {
            return error(HttpStatus.BAD_REQUEST, "El estado debe ser Abierta, Cerrada o Pendiente");
        }
        if (!CONSENTIMIENTOS.contains(consentimiento)) {
            return error(HttpStatus.BAD_REQUEST,
                    "El estado del consentimiento debe ser Firmado, No Firmado o Revocado");
        }
        // Una historia nueva no puede nacer con el consentimiento revocado
        if ("Revocado".equals(consentimiento)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Una historia nueva no puede crearse con el consentimiento revocado");
        }

        // Validación 3: fechas (si no se envía la fecha de consulta, es la de hoy)
        String fechaConsultaFinal = vacio(fechaConsulta) ? LocalDate.now().toString() : fechaConsulta;
        String errorFecha = errorDeFecha(fechaConsultaFinal, "La fecha de consulta");
        if (errorFecha != null) {
            return error(HttpStatus.BAD_REQUEST, errorFecha);
        }

        // Validación 4: un consentimiento firmado exige fecha de firma; si no está firmado, no se guarda
        String fechaFirmaFinal = null;
        if ("Firmado".equals(consentimiento)) {
            if (vacio(fechaFirma)) {
                return error(HttpStatus.BAD_REQUEST,
                        "La fecha de firma es obligatoria cuando el consentimiento está firmado");
            }
            errorFecha = errorDeFecha(fechaFirma, "La fecha de firma");
            if (errorFecha != null) {
                return error(HttpStatus.BAD_REQUEST, errorFecha);
            }
            fechaFirmaFinal = fechaFirma;
        }

        // Validación 5: para cerrar una historia debe existir el diagnóstico
        if ("Cerrada".equals(estadoFinal) && vacio(diagnostico)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Para cerrar la historia clínica el diagnóstico es obligatorio");
        }

        try {
            // Validación 6: el paciente debe existir
            if (pacienteDAO.buscarPorId(idPaciente) == null) {
                return error(HttpStatus.NOT_FOUND, "Paciente no encontrado");
            }

            HistoriaClinica nueva = new HistoriaClinica();
            nueva.setMotivoConsulta(motivo.trim());
            nueva.setAntecedentesMedicos(textoOVacio(antecedentes));
            nueva.setDiagnostico(textoOVacio(diagnostico));
            nueva.setFechaConsulta(fechaConsultaFinal);
            nueva.setEstado(estadoFinal);
            nueva.setObservaciones(textoOVacio(observaciones));
            nueva.setFechaFirma(fechaFirmaFinal);
            nueva.setTipoProcedimiento(tipoProcedimiento.trim());
            nueva.setEstadoConsentimiento(consentimiento);
            nueva.setIdPaciente(idPaciente);
            nueva.setIdEspecialista(Permisos.idUsuario(sesion));   // quien inició sesión
            // Si la historia nace cerrada, la fecha de cierre es hoy
            nueva.setFechaCierre("Cerrada".equals(estadoFinal) ? LocalDate.now().toString() : null);

            int idNueva = historiaDAO.crear(nueva);
            if (idNueva > 0) {
                Map<String, Object> cuerpo = respuesta(true, "Historia clínica creada correctamente");
                cuerpo.put("id", idNueva);
                return ResponseEntity.status(HttpStatus.CREATED).body(cuerpo);
            }
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la historia clínica");

        } catch (SQLException e) {
            return errorInterno("crear historia clínica", e);
        }
    }

    /**
     * PUT /api/historias/{id}/estado — cambia el estado de la historia (solo su especialista autor).
     * Transiciones permitidas: Pendiente a Abierta, y Abierta a Cerrada. Cerrada es un estado final.
     * Al cerrar se puede enviar el diagnóstico final y las observaciones; el diagnóstico es obligatorio.
     */
    @PutMapping("/historias/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable int id,
            @RequestParam(value = "Estado", required = false) String nuevoEstado,
            @RequestParam(value = "Diagnostico", required = false) String diagnostico,
            @RequestParam(value = "Observaciones", required = false) String observaciones,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA);
        if (acceso != null) {
            return acceso;
        }
        if (vacio(nuevoEstado) || !ESTADOS.contains(nuevoEstado)) {
            return error(HttpStatus.BAD_REQUEST, "El estado debe ser Abierta, Cerrada o Pendiente");
        }

        try {
            HistoriaClinica historia = historiaDAO.buscarPorId(id);
            if (historia == null) {
                return error(HttpStatus.NOT_FOUND, "Historia clínica no encontrada");
            }
            if (!esAutor(historia, sesion)) {
                return error(HttpStatus.FORBIDDEN, SOLO_AUTOR);
            }

            String actual = historia.getEstado();
            if ("Cerrada".equals(actual)) {
                return error(HttpStatus.CONFLICT,
                        "La historia clínica está cerrada y no puede cambiar de estado");
            }

            boolean abrir = "Pendiente".equals(actual) && "Abierta".equals(nuevoEstado);
            boolean cerrar = "Abierta".equals(actual) && "Cerrada".equals(nuevoEstado);
            if (!abrir && !cerrar) {
                return error(HttpStatus.CONFLICT,
                        "Transición de estado no permitida: de " + actual + " a " + nuevoEstado);
            }

            if (abrir) {
                if (!historiaDAO.abrir(id)) {
                    return error(HttpStatus.CONFLICT, "La historia cambió de estado; consúltela de nuevo");
                }
                return ResponseEntity.ok(respuesta(true, "La historia clínica ahora está Abierta"));
            }

            // Cierre: el diagnóstico final es el enviado o, si no se envía, el que ya tenía
            String diagnosticoFinal = vacio(diagnostico) ? historia.getDiagnostico() : diagnostico.trim();
            if (vacio(diagnosticoFinal)) {
                return error(HttpStatus.BAD_REQUEST,
                        "Para cerrar la historia clínica el diagnóstico es obligatorio");
            }
            String observacionesFinal = vacio(observaciones)
                    ? textoOVacio(historia.getObservaciones()) : observaciones.trim();
            String hoy = LocalDate.now().toString();

            if (!historiaDAO.cerrar(id, diagnosticoFinal, observacionesFinal, hoy)) {
                return error(HttpStatus.CONFLICT, "La historia cambió de estado; consúltela de nuevo");
            }
            Map<String, Object> cuerpo = respuesta(true, "Historia clínica cerrada correctamente");
            cuerpo.put("fechaCierre", hoy);
            return ResponseEntity.ok(cuerpo);

        } catch (SQLException e) {
            return errorInterno("cambiar el estado de la historia clínica", e);
        }
    }

    /**
     * PUT /api/historias/{id}/consentimiento — cambia el estado del consentimiento (solo su especialista autor).
     * Transiciones permitidas: No Firmado a Firmado (si la historia no está cerrada, con fecha de firma)
     * y Firmado a Revocado (en cualquier momento, incluso con la historia cerrada). Revocado es final.
     */
    @PutMapping("/historias/{id}/consentimiento")
    public ResponseEntity<?> cambiarConsentimiento(
            @PathVariable int id,
            @RequestParam(value = "Estado_Consentimiento", required = false) String nuevo,
            @RequestParam(value = "Fecha_Firma", required = false) String fechaFirma,
            HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA);
        if (acceso != null) {
            return acceso;
        }
        if (vacio(nuevo) || !CONSENTIMIENTOS.contains(nuevo)) {
            return error(HttpStatus.BAD_REQUEST,
                    "El estado del consentimiento debe ser Firmado, No Firmado o Revocado");
        }

        try {
            HistoriaClinica historia = historiaDAO.buscarPorId(id);
            if (historia == null) {
                return error(HttpStatus.NOT_FOUND, "Historia clínica no encontrada");
            }
            if (!esAutor(historia, sesion)) {
                return error(HttpStatus.FORBIDDEN, SOLO_AUTOR);
            }

            String actual = historia.getEstadoConsentimiento();
            if ("Revocado".equals(actual)) {
                return error(HttpStatus.CONFLICT,
                        "El consentimiento ya fue revocado; para autorizar de nuevo cree una nueva historia clínica");
            }

            // Caso 1: firmar un consentimiento pendiente
            if ("No Firmado".equals(actual) && "Firmado".equals(nuevo)) {
                if ("Cerrada".equals(historia.getEstado())) {
                    return error(HttpStatus.CONFLICT,
                            "No se puede firmar el consentimiento de una historia cerrada");
                }
                if (vacio(fechaFirma)) {
                    return error(HttpStatus.BAD_REQUEST,
                            "La fecha de firma es obligatoria cuando el consentimiento está firmado");
                }
                String errorFecha = errorDeFecha(fechaFirma, "La fecha de firma");
                if (errorFecha != null) {
                    return error(HttpStatus.BAD_REQUEST, errorFecha);
                }
                if (!historiaDAO.firmar(id, fechaFirma)) {
                    return error(HttpStatus.CONFLICT, "La historia cambió; consúltela de nuevo");
                }
                return ResponseEntity.ok(respuesta(true, "Consentimiento firmado correctamente"));
            }

            // Caso 2: revocar un consentimiento firmado (se conserva la fecha de firma original)
            if ("Firmado".equals(actual) && "Revocado".equals(nuevo)) {
                String hoy = LocalDate.now().toString();
                if (!historiaDAO.revocar(id, hoy)) {
                    return error(HttpStatus.CONFLICT, "La historia cambió; consúltela de nuevo");
                }
                Map<String, Object> cuerpo = respuesta(true, "Consentimiento revocado correctamente");
                cuerpo.put("fechaRevocacion", hoy);
                return ResponseEntity.ok(cuerpo);
            }

            return error(HttpStatus.CONFLICT,
                    "Transición de consentimiento no permitida: de " + actual + " a " + nuevo);

        } catch (SQLException e) {
            return errorInterno("cambiar el consentimiento", e);
        }
    }

    /** GET /api/pacientes/{idPaciente}/historias — historial clínico del paciente. */
    @GetMapping("/pacientes/{idPaciente}/historias")
    public ResponseEntity<?> historial(@PathVariable int idPaciente, HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA, Permisos.AUXILIAR);
        if (acceso != null) {
            return acceso;
        }
        try {
            if (pacienteDAO.buscarPorId(idPaciente) == null) {
                return error(HttpStatus.NOT_FOUND, "Paciente no encontrado");
            }
            return ResponseEntity.ok(historiaDAO.listarPorPaciente(idPaciente));
        } catch (SQLException e) {
            return errorInterno("consultar el historial", e);
        }
    }

    /** GET /api/historias/{id} — detalle completo de una historia (ventana emergente). */
    @GetMapping("/historias/{id}")
    public ResponseEntity<?> detalle(@PathVariable int id, HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA, Permisos.AUXILIAR);
        if (acceso != null) {
            return acceso;
        }
        try {
            HistoriaClinica historia = historiaDAO.buscarPorId(id);
            if (historia == null) {
                return error(HttpStatus.NOT_FOUND, "Historia clínica no encontrada");
            }
            return ResponseEntity.ok(historia);
        } catch (SQLException e) {
            return errorInterno("consultar la historia clínica", e);
        }
    }

    /** GET /api/servicios — servicios de la clínica, para la lista de tipos de procedimiento. */
    @GetMapping("/servicios")
    public ResponseEntity<?> servicios(HttpSession sesion) {

        ResponseEntity<?> acceso = verificarAcceso(sesion, Permisos.ESPECIALISTA, Permisos.AUXILIAR);
        if (acceso != null) {
            return acceso;
        }
        try {
            return ResponseEntity.ok(historiaDAO.listarServicios());
        } catch (SQLException e) {
            return errorInterno("listar servicios", e);
        }
    }

    // ---- Métodos de apoyo ----

    /** Devuelve null si el usuario puede continuar; 401 si no hay sesión; 403 si su rol no está permitido. */
    private ResponseEntity<Map<String, Object>> verificarAcceso(HttpSession sesion, String... rolesPermitidos) {
        if (!Permisos.hayLogin(sesion)) {
            return error(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
        }
        if (!Permisos.tieneRol(sesion, rolesPermitidos)) {
            return error(HttpStatus.FORBIDDEN, "No tiene permisos para esta acción");
        }
        return null;
    }

    /** Indica si quien tiene la sesión abierta es el especialista que creó la historia. */
    private boolean esAutor(HistoriaClinica historia, HttpSession sesion) {
        return historia.getIdEspecialista() == Permisos.idUsuario(sesion);
    }

    /** Valida formato AAAA-MM-DD y que la fecha no sea futura. Devuelve el mensaje de error, o null. */
    private String errorDeFecha(String texto, String nombreCampo) {
        try {
            LocalDate fecha = LocalDate.parse(texto);
            if (fecha.isAfter(LocalDate.now())) {
                return nombreCampo + " no puede ser una fecha futura";
            }
        } catch (DateTimeParseException e) {
            return nombreCampo + " debe tener formato AAAA-MM-DD";
        }
        return null;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(respuesta(false, mensaje));
    }

    private ResponseEntity<Map<String, Object>> errorInterno(String accion, SQLException e) {
        System.err.println("Error al " + accion + ": " + e.getMessage());
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Los campos de texto opcionales se guardan como cadena vacía, nunca como NULL. */
    private String textoOVacio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private Map<String, Object> respuesta(boolean exito, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("success", exito);
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}