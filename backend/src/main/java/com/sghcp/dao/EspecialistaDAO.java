package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import com.sghcp.modelo.Especialista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.stereotype.Repository;

/** Acceso a datos de la tabla "especialista". */
@Repository
public class EspecialistaDAO {

    // Por ahora el sistema maneja una sola clínica
    private static final int ID_CLINICA_FIJO = 1;

    private final ConexionBD conexionBD;

    public EspecialistaDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** Busca al especialista por correo y tarjeta profesional. Devuelve null si no existe. */
    public Especialista validarLogin(String correo, String tarjetaProfesional) throws SQLException {
        String sql = "SELECT Id_especialista, Nombre, Apellidos, Correo FROM especialista "
                   + "WHERE Correo = ? AND Tarjeta_Profesional = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, tarjetaProfesional);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Especialista e = new Especialista();
                    e.setIdEspecialista(rs.getInt("Id_especialista"));
                    e.setNombre(rs.getString("Nombre"));
                    e.setApellidos(rs.getString("Apellidos"));
                    e.setCorreo(rs.getString("Correo"));
                    return e;
                }
            }
        }
        return null;
    }

    /** Indica si ya hay un especialista registrado con ese correo. */
    public boolean existeCorreo(String correo) throws SQLException {
        String sql = "SELECT 1 FROM especialista WHERE Correo = ?";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserta un nuevo especialista. Devuelve true si se guardó. */
    public boolean registrar(Especialista e) throws SQLException {
        String sql = "INSERT INTO especialista "
                   + "(Nombre, Apellidos, Tarjeta_Profesional, Correo, Especialidad, Telefono, Id_Clinica) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellidos());
            ps.setString(3, e.getTarjetaProfesional());
            ps.setString(4, e.getCorreo());
            ps.setString(5, e.getEspecialidad());
            ps.setString(6, e.getTelefono());
            ps.setInt(7, ID_CLINICA_FIJO);
            return ps.executeUpdate() > 0;
        }
    }
}