package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

/** Acceso a los datos de la tabla "clinica". */
@Repository
public class ClinicaDAO {

    // Por ahora el sistema maneja una sola clínica
    private static final int ID_CLINICA_FIJO = 1;

    private final ConexionBD conexionBD;

    public ClinicaDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** Devuelve los datos de la clínica; el mapa queda vacío si no existe la fila. */
    public Map<String, String> obtener() throws SQLException {
        String sql = "SELECT Nombre, Pais, Ciudad, Direccion, Telefono, Horario_Atencion "
                   + "FROM clinica WHERE Id_Clinica = ?";
        Map<String, String> clinica = new LinkedHashMap<>();
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ID_CLINICA_FIJO);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    clinica.put("nombre", rs.getString("Nombre"));
                    clinica.put("pais", rs.getString("Pais"));
                    clinica.put("ciudad", rs.getString("Ciudad"));
                    clinica.put("direccion", rs.getString("Direccion"));
                    clinica.put("telefono", rs.getString("Telefono"));
                    clinica.put("horario", rs.getString("Horario_Atencion"));
                }
            }
        }
        return clinica;
    }
}