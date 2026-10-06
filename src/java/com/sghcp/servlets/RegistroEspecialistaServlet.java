package com.sghcp.servlets;

import com.sghcp.DAO.especialistaDAO;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servicio web encargado del registro de nuevos especialistas en el sistema SGHCP.
 * Recibe los datos del especialista por POST, valida que el correo no esté
 * duplicado, y lo guarda en la base de datos. Responde en formato JSON,
 * pensado para ser consumido tanto por el frontend en React como por
 * herramientas de prueba como Postman.
 */
@WebServlet("/RegistroEspecialistaServlet")
public class RegistroEspecialistaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuración de codificación y tipo de respuesta (JSON)
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Cabeceras CORS, necesarias para que React (localhost:5173) pueda consumir este servicio
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Captura de los parámetros enviados en el formulario/petición
        String nombre = request.getParameter("Nombre");
        String apellidos = request.getParameter("Apellidos");
        String tarjetaProfesional = request.getParameter("Tarjeta_Profesional");
        String correo = request.getParameter("Correo");
        String especialidad = request.getParameter("Especialidad");
        String telefono = request.getParameter("Telefono");

        PrintWriter out = response.getWriter();

        // Validación básica: ningún campo obligatorio puede llegar vacío
        if (nombre == null || nombre.isBlank()
                || apellidos == null || apellidos.isBlank()
                || tarjetaProfesional == null || tarjetaProfesional.isBlank()
                || correo == null || correo.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{ \"success\": false, \"mensaje\": \"Todos los campos obligatorios deben estar completos\" }");
            out.flush();
            return;
        }

        // Delegamos la lógica de negocio (verificación y registro) al DAO
        especialistaDAO dao = new especialistaDAO();
        String resultado = dao.registrarEspecialista(
                nombre, apellidos, tarjetaProfesional, correo, especialidad, telefono
        );

        // Se construye la respuesta según el resultado devuelto por el DAO
        switch (resultado) {
            case "OK":
                out.print("{ \"success\": true, \"mensaje\": \"Registro realizado correctamente\" }");
                break;
            case "DUPLICADO":
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                out.print("{ \"success\": false, \"mensaje\": \"Ya existe un especialista registrado con ese correo\" }");
                break;
            default:
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{ \"success\": false, \"mensaje\": \"Error en la autenticación\" }");
                break;
        }

        out.flush();
    }

    /**
     * Maneja la petición OPTIONS que el navegador envía automáticamente
     * antes de un POST desde otro origen (CORS preflight).
     */
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