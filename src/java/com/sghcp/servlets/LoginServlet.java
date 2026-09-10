/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.sghcp.servlets;

import com.sghcp.config.ConexionBD;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Recibir los datos del formulario
        String correo = request.getParameter("Correo");
        String tarjetaProfesional = request.getParameter("Tarjeta_Profesional");

        try (Connection conn = ConexionBD.getConexion()) {
            // Consulta a la tabla especialista
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM especialista WHERE Correo=? AND Tarjeta_Profesional=?"
            );
            ps.setString(1, correo);
            ps.setString(2, tarjetaProfesional);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Si existe el especialista - crear sesión y redirigir
                HttpSession sesion = request.getSession();
                sesion.setAttribute("correo", correo);
                response.sendRedirect("dashboard.jsp");
            } else {
                // Si no existe - regresar al login con error
                response.sendRedirect("login.jsp?error=1");
            }

        } catch (Exception e) {
            throw new ServletException("Error en el proceso de login", e);
        }
    }
}
