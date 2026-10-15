package com.sghcp.dao;

import com.sghcp.config.ConexionBD;
import com.sghcp.modelo.UsuarioAutenticado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.stereotype.Repository;

/** Valida credenciales buscando al usuario en las tres tablas de acceso. */
@Repository
public class AutenticacionDAO {

    private final ConexionBD conexionBD;

    public AutenticacionDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** Devuelve el usuario con su rol, o null si las credenciales no coinciden en ninguna tabla. */
    public UsuarioAutenticado autenticar(String correo, String clave) throws SQLException {

        // 1) Especialista: correo + tarjeta profesional
        UsuarioAutenticado usuario = buscar("ESPECIALISTA",
                "SELECT Id_especialista, Nombre, Correo FROM especialista "
              + "WHERE Correo = ? AND Tarjeta_Profesional = ?", correo, clave);
        if (usuario != null) {
            return usuario;
        }

        // 2) Auxiliar administrativo: correo + identificación
        usuario = buscar("AUXILIAR",
                "SELECT Id_Aux_Administrativo, Nombre, Correo FROM auxiliar_admin "
              + "WHERE Correo = ? AND Identificacion = ?", correo, clave);
        if (usuario != null) {
            return usuario;
        }

        // 3) Administrador del sistema: correo + contraseña
        return buscar("ADMIN",
                "SELECT Id_Administrador, Nombre, Correo FROM admin_sistema "
              + "WHERE Correo = ? AND Contrasena = ?", correo, clave);
    }

    /** Ejecuta una consulta de acceso; las tres devuelven (id, nombre, correo) en ese orden. */
    private UsuarioAutenticado buscar(String rol, String sql, String correo, String clave)
            throws SQLException {
        try (Connection conn = conexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuarioAutenticado(rol, rs.getInt(1), rs.getString(2), rs.getString(3));
                }
            }
        }
        return null;
    }
}