package com.sghcp.modelo;

/** Clase modelo: representa una historia clínica (tabla "historia_clinica"). */
public class HistoriaClinica {
    private int id;
    private String motivoConsulta;
    private String antecedentesMedicos;
    private String diagnostico;
    private String fechaConsulta;          // formato AAAA-MM-DD
    private String estado;                 // Abierta, Cerrada o Pendiente
    private String observaciones;
    private String fechaFirma;             // solo si el consentimiento está firmado
    private String tipoProcedimiento;
    private String estadoConsentimiento;   // Firmado, No Firmado o Revocado
    private int idPaciente;
    private int idEspecialista;

    // Estos tres solo se llenan al consultar el detalle (vienen de otras tablas)
    private String nombrePaciente;
    private String identificacionPaciente;
    private String nombreEspecialista;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    public String getAntecedentesMedicos() { return antecedentesMedicos; }
    public void setAntecedentesMedicos(String antecedentesMedicos) { this.antecedentesMedicos = antecedentesMedicos; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(String fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(String fechaFirma) { this.fechaFirma = fechaFirma; }
    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }
    public String getEstadoConsentimiento() { return estadoConsentimiento; }
    public void setEstadoConsentimiento(String estadoConsentimiento) { this.estadoConsentimiento = estadoConsentimiento; }
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    public int getIdEspecialista() { return idEspecialista; }
    public void setIdEspecialista(int idEspecialista) { this.idEspecialista = idEspecialista; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public String getIdentificacionPaciente() { return identificacionPaciente; }
    public void setIdentificacionPaciente(String identificacionPaciente) { this.identificacionPaciente = identificacionPaciente; }
    public String getNombreEspecialista() { return nombreEspecialista; }
    public void setNombreEspecialista(String nombreEspecialista) { this.nombreEspecialista = nombreEspecialista; }
}