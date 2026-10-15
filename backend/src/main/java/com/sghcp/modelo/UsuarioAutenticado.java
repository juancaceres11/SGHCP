package com.sghcp.modelo;

/** Datos mínimos de quien inició sesión, sin importar su rol. */
public record UsuarioAutenticado(String rol, int id, String nombre, String correo) {}