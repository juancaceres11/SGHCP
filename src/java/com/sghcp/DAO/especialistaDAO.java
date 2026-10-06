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
    } /**
 * Registra un nuevo especialista en la base de datos.
 * Antes de insertar, valida que el correo no esté ya registrado,
 * para evitar duplicados de cuentas de acceso.
 *
 * @param nombre              Nombre del especialista
 * @param apellidos            Apellidos del especialista
 * @param tarjetaProfesional   Tarjeta profesional (funciona como contraseña del login)
 * @param correo               Correo (funciona como usuario del login)
 * @param especialidad         Especialidad médica/podológica del especialista
 * @param telefono             Teléfono de contacto
 * @return "OK" si el registro fue exitoso,
 *         "DUPLICADO" si el correo ya existe,
 *         "ERROR" si ocurrió un fallo en la base de datos
 */
public String registrarEspecialista(String nombre, String apellidos, String tarjetaProfesional,
                                     String correo, String especialidad, String telefono) {

    // Id_Clinica queda fijo en 1, ya que por ahora el sistema maneja una sola clínica
    final int ID_CLINICA_FIJO = 1;

    // Paso 1: verificar que el correo no esté ya registrado
    String sqlVerificar = "SELECT Id_especialista FROM especialista WHERE Correo = ?";
    try (Connection conn = ConexionBD.getConexion();
         PreparedStatement psVerificar = conn.prepareStatement(sqlVerificar)) {

        psVerificar.setString(1, correo);
        ResultSet rs = psVerificar.executeQuery();

        if (rs.next()) {
            // Ya existe un especialista con ese correo
            return "DUPLICADO";
        }

    } catch (SQLException e) {
        System.err.println("Error al verificar correo existente: " + e.getMessage());
        return "ERROR";
    }

    // Paso 2: insertar el nuevo especialista
    String sqlInsertar = "INSERT INTO especialista "
            + "(Nombre, Apellidos, Tarjeta_Profesional, Correo, Especialidad, Telefono, Id_Clinica) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = ConexionBD.getConexion();
         PreparedStatement psInsertar = conn.prepareStatement(sqlInsertar)) {

        psInsertar.setString(1, nombre);
        psInsertar.setString(2, apellidos);
        psInsertar.setString(3, tarjetaProfesional);
        psInsertar.setString(4, correo);
        psInsertar.setString(5, especialidad);
        psInsertar.setString(6, telefono);
        psInsertar.setInt(7, ID_CLINICA_FIJO);

        int filasAfectadas = psInsertar.executeUpdate();

        // Si se insertó al menos una fila, el registro fue exitoso
        return (filasAfectadas > 0) ? "OK" : "ERROR";

    } catch (SQLException e) {
        System.err.println("Error al registrar especialista: " + e.getMessage());
        return "ERROR";
    }
}
}