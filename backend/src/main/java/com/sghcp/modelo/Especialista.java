package com.sghcp.modelo;

/** Clase modelo: representa a un especialista (columnas de la tabla "especialista"). */
public class Especialista {
    private int idEspecialista;
    private String nombre;
    private String apellidos;
    private String tarjetaProfesional;
    private String correo;
    private String especialidad;
    private String telefono;

    public int getIdEspecialista() { return idEspecialista; }
    public void setIdEspecialista(int idEspecialista) { this.idEspecialista = idEspecialista; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTarjetaProfesional() { return tarjetaProfesional; }
    public void setTarjetaProfesional(String tarjetaProfesional) { this.tarjetaProfesional = tarjetaProfesional; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}