/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sghcp.DAO;

import com.sghcp.config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PacienteDAO {

    public boolean registrarPaciente(String Nombre, String Apellidos, String Identificacion,
                                     String Sexo, String Tipo_Sangre, String Fecha_Nacimiento,
                                     String Direccion, String Telefono, String Ocupacion, int Id_clinica) {
        try (Connection con = ConexionBD.getConexion()) {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO paciente (Nombre, Apellidos, Identificacion, Sexo, Tipo_Sangre, Fecha_Nacimiento, Direccion, Telefono, Ocupacion, Id_clinica) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, Nombre);
            ps.setString(2, Apellidos);
            ps.setString(3, Identificacion);
            ps.setString(4, Sexo);
            ps.setString(5, Tipo_Sangre);
            ps.setString(6, Fecha_Nacimiento); // formato YYYY-MM-DD
            ps.setString(7, Direccion);
            ps.setString(8, Telefono);
            ps.setString(9, Ocupacion);
            ps.setInt(10, Id_clinica); // este valor por ahora le daremos un valor unico, ya que nos sentramos en la creación del modulo de registro, aun no trabajamos con la tabla de clinicas (ej. fijo, sesión, selección)

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
    System.err.println("Error al registrar paciente: " + e.getMessage());
    return false;
}
    }
}
