/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sghcp.DAO;

import com.sghcp.config.ConexionBD;
import java.sql.*;

public class especialistaDAO {
    public boolean validarLogin(String correo, String tarjetaProfesional) {
        System.out.println("DAO - correo recibido: " + correo);
        System.out.println("DAO - tarjeta recibida: " + tarjetaProfesional);
        try (Connection conn = ConexionBD.getConexion()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM especialista WHERE Correo=? AND Tarjeta_Profesional=?"
            );
            ps.setString(1, correo);
            ps.setString(2, tarjetaProfesional);
            ResultSet rs = ps.executeQuery();
            boolean existe = rs.next();
            System.out.println("DAO - resultado consulta: " + existe);
            return existe;
        } catch (SQLException e) {
            System.err.println("Error al validar login: " + e.getMessage());
            return false;
        }
    }
}