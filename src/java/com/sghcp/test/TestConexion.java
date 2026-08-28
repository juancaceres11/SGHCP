/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sghcp.test;

import com.sghcp.config.ConexionBD;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = ConexionBD.getConexion()) {
            System.out.println("Conexión exitosa a la BD!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
