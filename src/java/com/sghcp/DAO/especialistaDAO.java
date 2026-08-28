/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sghcp.DAO;
import com.sghcp.config.ConexionBD;
import java.sql.*;

public class especialistaDAO {

    // Método para validar login
    public boolean validarLogin(String correo, String tarjetaProfesional) {
        try (Connection conn = ConexionBD.getConexion()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM especialista WHERE Correo=? AND Tarjeta_Profesional=?"
            );
            ps.setString(1, correo);
            ps.setString(2, tarjetaProfesional);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true si existe el registro
        } catch (SQLException e) {
    System.err.println("Error al validar login: " + e.getMessage());
    return false;
             }
    }
}

