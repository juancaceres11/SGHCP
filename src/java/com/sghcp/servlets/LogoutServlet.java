/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.sghcp.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate(); // destruye la sesión en el servidor
        }

        // Redirige al login de React 
        response.sendRedirect("http://localhost:5173/");
    }
}// nuevo Servlet agregado para que maneje el logout del sistema,
//con el fin de mejorar la seguridad y dar cumlimiento a los requisitos no funcionales del SGHCP.
// Una vez se cierre sesión, se debe volver a validar la sesión ya que se destruye las credenciales guardadas y redirecciona nuevamnete al local host del login.