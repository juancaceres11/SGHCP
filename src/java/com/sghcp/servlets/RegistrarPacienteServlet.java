/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.sghcp.servlets;

import com.sghcp.DAO.PacienteDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;


@WebServlet("/RegistrarPacienteServlet")
public class RegistrarPacienteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String Nombre = request.getParameter("Nombre");
        String Apellidos = request.getParameter("Apellidos");
        String Identificacion = request.getParameter("Identificacion");
        String Sexo = request.getParameter("Sexo");
        String Tipo_Sangre = request.getParameter("Tipo_Sangre");
        String Fecha_Nacimiento = request.getParameter("Fecha_Nacimiento");
        String Direccion = request.getParameter("Direccion");
        String Telefono = request.getParameter("Telefono");
        String Ocupacion = request.getParameter ("Ocupacion");
        int Id_clinica = 1; // por ahora sería 1 mientras vamos agregando las otras tablas de la base de datos y completamos el SGHCP.
    
     
  
        PacienteDAO dao = new PacienteDAO();
        boolean exito = dao.registrarPaciente(Nombre, Apellidos, Identificacion,
                Sexo, Tipo_Sangre, Fecha_Nacimiento,
                Direccion, Telefono, Ocupacion, Id_clinica);

        if (exito) {
            response.sendRedirect("RegistrarPaciente.jsp?msg=Paciente registrado correctamente");
        } else {
            response.sendRedirect("RegistrarPaciente.jsp?msg=Error: Identificacion  ya registrada");
        }
    }
}
            