package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import com.sghcp.modelo.HistoriaClinica;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/** Acceso a datos de "historia_clinica" y de los servicios de la clínica ("servicio_clinica"). */
@Repository
public class HistoriaClinicaDAO {

    // Por ahora el sistema maneja una sola clínica
    private static final int ID_CLINICA_FIJO = 1;

    private final ConexionBD conexionBD;

    public HistoriaClinicaDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** Inserta una historia clínica nueva. Devuelve el id generado, o -1 si no se pudo guardar. */
    public int crear(HistoriaClinica h) throws SQLException {
        String sql = "INSERT INTO historia_clinica "
                   + "(Motivo_Consulta, Antecedentes_Medicos, Diagnostico, Fecha_Consulta, Estado, "
                   + "Observaciones, Fecha_Firma, Tipo_Procedimiento, Estado_Consentimiento, "
                   + "Id_Paciente, Id_Especialista) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getMotivoConsulta());
            ps.setString(2, h.getAntecedentesMedicos());
            ps.setString(3, h.getDiagnostico());
            ps.setDate(4, Date.valueOf(h.getFechaConsulta()));
            ps.setString(5, h.getEstado());
            ps.setString(6, h.getObservaciones());
            if (h.getFechaFirma() != null) {
                ps.setDate(7, Date.valueOf(h.getFechaFirma()));
            } else {
                ps.setNull(7, Types.DATE);   // sin consentimiento firmado no hay fecha de firma
            }
            ps.setString(8, h.getTipoProcedimiento());
            ps.setString(9, h.getEstadoConsentimiento());
            ps.setInt(10, h.getIdPaciente());
            ps.setInt(11, h.getIdEspecialista());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Historial de un paciente: sus historias, de la más reciente a la más antigua. */
    public List<Map<String, Object>> listarPorPaciente(int idPaciente) throws SQLException {
        String sql = "SELECT Id_HistoriaClinica, Fecha_Consulta, Tipo_Procedimiento, Diagnostico, "
                   + "Estado, Estado_Consentimiento "
                   + "FROM historia_clinica WHERE Id_Paciente = ? "
                   + "ORDER BY Fecha_Consulta DESC, Id_HistoriaClinica DESC";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("id", rs.getInt("Id_HistoriaClinica"));
                    fila.put("fechaConsulta", rs.getString("Fecha_Consulta"));
                    fila.put("tipoProcedimiento", rs.getString("Tipo_Procedimiento"));
                    fila.put("diagnostico", rs.getString("Diagnostico"));
                    fila.put("estado", rs.getString("Estado"));
                    fila.put("estadoConsentimiento", rs.getString("Estado_Consentimiento"));
                    lista.add(fila);
                }
            }
        }
        return lista;
    }

    /** Detalle completo de una historia, con los nombres del paciente y del especialista. */
    public HistoriaClinica buscarPorId(int id) throws SQLException {
        String sql = "SELECT h.Id_HistoriaClinica, h.Motivo_Consulta, h.Antecedentes_Medicos, "
                   + "h.Diagnostico, h.Fecha_Consulta, h.Estado, h.Observaciones, h.Fecha_Firma, "
                   + "h.Tipo_Procedimiento, h.Estado_Consentimiento, h.Id_Paciente, h.Id_Especialista, "
                   + "CONCAT(p.Nombre, ' ', p.Apellidos) AS Nombre_Paciente, "
                   + "p.Identificacion AS Identificacion_Paciente, "
                   + "CONCAT(e.Nombre, ' ', e.Apellidos) AS Nombre_Especialista "
                   + "FROM historia_clinica h "
                   + "JOIN paciente p ON p.Id_Paciente = h.Id_Paciente "
                   + "JOIN especialista e ON e.Id_Especialista = h.Id_Especialista "
                   + "WHERE h.Id_HistoriaClinica = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HistoriaClinica h = new HistoriaClinica();
                    h.setId(rs.getInt("Id_HistoriaClinica"));
                    h.setMotivoConsulta(rs.getString("Motivo_Consulta"));
                    h.setAntecedentesMedicos(rs.getString("Antecedentes_Medicos"));
                    h.setDiagnostico(rs.getString("Diagnostico"));
                    h.setFechaConsulta(rs.getString("Fecha_Consulta"));
                    h.setEstado(rs.getString("Estado"));
                    h.setObservaciones(rs.getString("Observaciones"));
                    h.setFechaFirma(rs.getString("Fecha_Firma"));
                    h.setTipoProcedimiento(rs.getString("Tipo_Procedimiento"));
                    h.setEstadoConsentimiento(rs.getString("Estado_Consentimiento"));
                    h.setIdPaciente(rs.getInt("Id_Paciente"));
                    h.setIdEspecialista(rs.getInt("Id_Especialista"));
                    h.setNombrePaciente(rs.getString("Nombre_Paciente"));
                    h.setIdentificacionPaciente(rs.getString("Identificacion_Paciente"));
                    h.setNombreEspecialista(rs.getString("Nombre_Especialista"));
                    return h;
                }
            }
        }
        return null;
    }

    /** Servicios de la clínica: alimentan la lista desplegable de "Tipo de procedimiento". */
    public List<Map<String, Object>> listarServicios() throws SQLException {
        String sql = "SELECT Id_Servicio, Nombre_Servicio, Descripcion "
                   + "FROM servicio_clinica WHERE Id_clinica = ? ORDER BY Nombre_Servicio";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ID_CLINICA_FIJO);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("id", rs.getInt("Id_Servicio"));
                    fila.put("nombre", rs.getString("Nombre_Servicio"));
                    fila.put("descripcion", rs.getString("Descripcion"));
                    lista.add(fila);
                }
            }
        }
        return lista;
    }
}