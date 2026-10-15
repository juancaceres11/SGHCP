package com.sghcp.config;

import jakarta.servlet.http.HttpSession;

/** Utilidades para saber quién tiene la sesión abierta y con qué rol. */
public final class Permisos {

    public static final String ESPECIALISTA = "ESPECIALISTA";
    public static final String AUXILIAR = "AUXILIAR";
    public static final String ADMIN = "ADMIN";

    private Permisos() {
    }

    /** Hay login si el controlador de autenticación dejó el rol en la sesión. */
    public static boolean hayLogin(HttpSession sesion) {
        return sesion.getAttribute("rol") != null;
    }

    public static String rol(HttpSession sesion) {
        return (String) sesion.getAttribute("rol");
    }

    public static int idUsuario(HttpSession sesion) {
        return (Integer) sesion.getAttribute("idUsuario");
    }

    /** Indica si el rol de la sesión está entre los roles permitidos. */
    public static boolean tieneRol(HttpSession sesion, String... permitidos) {
        String rolActual = rol(sesion);
        for (String permitido : permitidos) {
            if (permitido.equals(rolActual)) {
                return true;
            }
        }
        return false;
    }
}