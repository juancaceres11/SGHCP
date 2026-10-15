package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import com.sghcp.modelo.Paciente;
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

/** Acceso a datos de la tabla "paciente" (y de "registro_paciente" al registrar). */
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

    /**
     * Inserta un paciente nuevo y deja constancia de quién lo registró en "registro_paciente".
     * Las dos inserciones van en una transacción: si una falla, no se guarda ninguna.
     * Devuelve el id generado, o -1 si no se pudo guardar.
     */
    public int registrar(Paciente p, String rol, int idUsuario) throws SQLException {
        String sqlPaciente = "INSERT INTO paciente "
                   + "(Nombre, Apellidos, Identificacion, Sexo, Tipo_Sangre, Fecha_Nacimiento, "
                   + "Direccion, Telefono, Id_Clinica, Ocupacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlRegistro = "INSERT INTO registro_paciente "
                   + "(Fecha_Registro, Id_Paciente, Id_Aux_Administrativo, Id_Especialista) "
                   + "VALUES (NOW(), ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion()) {
            conn.setAutoCommit(false);   // inicia la transacción
            try (PreparedStatement ps = conn.prepareStatement(sqlPaciente, Statement.RETURN_GENERATED_KEYS)) {
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

                int idNuevo = -1;
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        idNuevo = claves.getInt(1);
                    }
                }
                if (idNuevo < 0) {
                    conn.rollback();
                    return -1;
                }

                // Se anota quién registró: auxiliar o especialista (el otro campo queda en NULL)
                try (PreparedStatement psRegistro = conn.prepareStatement(sqlRegistro)) {
                    psRegistro.setInt(1, idNuevo);
                    if ("AUXILIAR".equals(rol)) {
                        psRegistro.setInt(2, idUsuario);
                        psRegistro.setNull(3, Types.INTEGER);
                    } else {
                        psRegistro.setNull(2, Types.INTEGER);
                        psRegistro.setInt(3, idUsuario);
                    }
                    psRegistro.executeUpdate();
                }

                conn.commit();   // confirma las dos inserciones
                return idNuevo;

            } catch (SQLException e) {
                conn.rollback();   // si algo falla, se deshace todo
                throw e;
            }
        }
    }

    /** Actualiza los datos editables de un paciente. La identificación no se modifica. */
    public boolean actualizar(int id, Paciente p) throws SQLException {
        String sql = "UPDATE paciente SET Nombre = ?, Apellidos = ?, Sexo = ?, Tipo_Sangre = ?, "
                   + "Fecha_Nacimiento = ?, Direccion = ?, Telefono = ?, Ocupacion = ? "
                   + "WHERE Id_Paciente = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellidos());
            ps.setString(3, p.getSexo());
            ps.setString(4, p.getTipoSangre());
            ps.setDate(5, Date.valueOf(p.getFechaNacimiento()));
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getTelefono());
            ps.setString(8, p.getOcupacion());
            ps.setInt(9, id);
            return ps.executeUpdate() > 0;
        }
    }
}