/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

// codigo modificado para trabajar el modulo de login con react para responder JSON.
package com.sghcp.servlets;

import com.sghcp.DAO.especialistaDAO;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // CORS - necesario mientras React corre en su propio servidor de desarrollo
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // puerto por defecto de Vite
        response.setHeader("Access-Control-Allow-Credentials", "true");

        String correo = request.getParameter("Correo");
        String tarjetaProfesional = request.getParameter("Tarjeta_Profesional");

        especialistaDAO dao = new especialistaDAO();
        boolean valido = dao.validarLogin(correo, tarjetaProfesional);

        PrintWriter out = response.getWriter();

        if (valido) {
            HttpSession sesion = request.getSession();
            sesion.setAttribute("correo", correo);

            out.print("{ \"success\": true, \"correo\": \"" + correo + "\" }");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{ \"success\": false, \"mensaje\": \"usuario o contraseña incorrectos\" }");
        }
        out.flush();
    }

    // Necesario para que el navegador no bloquee la petición (preflight CORS)
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}