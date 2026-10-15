package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import com.sghcp.modelo.Paciente;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/** Acceso a datos de la tabla "paciente". */
@Repository
public class PacienteDAO {

    // Por ahora el sistema maneja una sola clínica
    private static final int ID_CLINICA_FIJO = 1;

    private final ConexionBD conexionBD;

    public PacienteDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /**
     * Lista los pacientes para la tabla del Dashboard. Si "busqueda" trae texto, filtra
     * por nombre completo o identificación. Incluye la fecha de la última consulta
     * (la más reciente de historia_clinica), que queda en null si aún no tiene historias.
     */
    public List<Map<String, Object>> listar(String busqueda) throws SQLException {
        String sql = "SELECT p.Id_Paciente, p.Nombre, p.Apellidos, p.Identificacion, p.Telefono, "
                   + "MAX(h.Fecha_Consulta) AS Ultima_Consulta "
                   + "FROM paciente p "
                   + "LEFT JOIN historia_clinica h ON h.Id_Paciente = p.Id_Paciente "
                   + "WHERE CONCAT(p.Nombre, ' ', p.Apellidos) LIKE ? "
                   + "OR CAST(p.Identificacion AS CHAR) LIKE ? "
                   + "GROUP BY p.Id_Paciente, p.Nombre, p.Apellidos, p.Identificacion, p.Telefono "
                   + "ORDER BY p.Id_Paciente";

        List<Map<String, Object>> lista = new ArrayList<>();
        String patron = "%" + busqueda + "%";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("id", rs.getInt("Id_Paciente"));
                    fila.put("nombreCompleto", rs.getString("Nombre") + " " + rs.getString("Apellidos"));
                    fila.put("identificacion", rs.getString("Identificacion"));
                    fila.put("telefono", rs.getString("Telefono"));
                    fila.put("ultimaConsulta", rs.getString("Ultima_Consulta"));
                    lista.add(fila);
                }
            }
        }
        return lista;
    }

    /** Busca un paciente por su id. Devuelve null si no existe. */
    public Paciente buscarPorId(int id) throws SQLException {
        String sql = "SELECT Id_Paciente, Nombre, Apellidos, Identificacion, Sexo, Tipo_Sangre, "
                   + "Fecha_Nacimiento, Direccion, Telefono, Ocupacion "
                   + "FROM paciente WHERE Id_Paciente = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Paciente p = new Paciente();
                    p.setId(rs.getInt("Id_Paciente"));
                    p.setNombre(rs.getString("Nombre"));
                    p.setApellidos(rs.getString("Apellidos"));
                    p.setIdentificacion(rs.getString("Identificacion"));
                    p.setSexo(rs.getString("Sexo"));
                    p.setTipoSangre(rs.getString("Tipo_Sangre"));
                    p.setFechaNacimiento(rs.getString("Fecha_Nacimiento"));
                    p.setDireccion(rs.getString("Direccion"));
                    p.setTelefono(rs.getString("Telefono"));
                    p.setOcupacion(rs.getString("Ocupacion"));
                    return p;
                }
            }
        }
        return null;
    }

    /** Indica si ya hay un paciente registrado con esa identificación. */
    public boolean existeIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT 1 FROM paciente WHERE Identificacion = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserta un paciente nuevo. Devuelve el id generado, o -1 si no se pudo guardar. */
    public int registrar(Paciente p) throws SQLException {
        String sql = "INSERT INTO paciente "
                   + "(Nombre, Apellidos, Identificacion, Sexo, Tipo_Sangre, Fecha_Nacimiento, "
                   + "Direccion, Telefono, Id_Clinica, Ocupacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getIdentificacion());
            ps.setString(4, p.getSexo());
            ps.setString(5, p.getTipoSangre());
            ps.setDate(6, Date.valueOf(p.getFechaNacimiento()));
            ps.setString(7, p.getDireccion());
            ps.setString(8, p.getTelefono());
            ps.setInt(9, ID_CLINICA_FIJO);
            ps.setString(10, p.getOcupacion());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }
        return -1;
    }
}