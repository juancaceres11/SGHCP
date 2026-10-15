package com.sghcp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Entrega conexiones a MySQL. Los datos se leen de application.properties (no van en el código). */
@Component
public class ConexionBD {

    @Value("${sghcp.db.url}")
    private String url;

    @Value("${sghcp.db.user}")
    private String user;

    @Value("${sghcp.db.password}")
    private String password;

    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}