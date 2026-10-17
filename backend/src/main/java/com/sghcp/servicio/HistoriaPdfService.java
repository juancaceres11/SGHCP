package com.sghcp.servicio;

import com.sghcp.modelo.HistoriaClinica;
import com.sghcp.modelo.Paciente;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

/** Genera en PDF el documento de una historia clínica, a partir de los datos ya guardados. */
@Service
public class HistoriaPdfService {

    private static final Color COLOR_PRINCIPAL = new Color(0, 139, 139);   // turquesa del sistema
    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_PRINCIPAL);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font DOCUMENTO = new Font(Font.HELVETICA, 13, Font.BOLD, Color.BLACK);
    private static final Font SECCION = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.DARK_GRAY);
    private static final Font NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font PIE = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Construye el PDF de la historia clínica y lo devuelve como arreglo de bytes.
     *
     * @param h        la historia clínica (con los nombres del paciente y del especialista)
     * @param p        el paciente, para sus datos personales (puede ser null)
     * @param clinica  datos de la clínica para el encabezado (puede venir vacío)
     */
    public byte[] generar(HistoriaClinica h, Paciente p, Map<String, String> clinica) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, salida);
            doc.addTitle("Historia clínica N.º " + h.getId());
            doc.open();

            // 1) Encabezado con los datos de la clínica
            doc.add(centrado(texto(clinica.get("nombre"), "Clínica podológica"), TITULO, 2));
            doc.add(centrado(unir(" - ", clinica.get("direccion"), clinica.get("ciudad"),
                    clinica.get("pais")), SUBTITULO, 0));
            doc.add(centrado(unir(" - ", prefijo("Tel. ", clinica.get("telefono")),
                    prefijo("Horario: ", clinica.get("horario"))), SUBTITULO, 10));
            doc.add(centrado("HISTORIA CLÍNICA N.º " + h.getId(), DOCUMENTO, 4));

            // 2) Datos del paciente
            doc.add(seccion("DATOS DEL PACIENTE"));
            doc.add(tablaDatos(
                    "Nombre:", p == null ? h.getNombrePaciente() : p.getNombre() + " " + p.getApellidos(),
                    "Identificación:", h.getIdentificacionPaciente(),
                    "Fecha de nacimiento:", fecha(p == null ? null : p.getFechaNacimiento()),
                    "Sexo:", sexo(p == null ? null : p.getSexo()),
                    "Tipo de sangre:", p == null ? null : p.getTipoSangre(),
                    "Teléfono:", p == null ? null : p.getTelefono(),
                    "Ocupación:", p == null ? null : p.getOcupacion(),
                    "Dirección:", p == null ? null : p.getDireccion()));

            // 3) Datos de la consulta
            doc.add(seccion("DATOS DE LA CONSULTA"));
            doc.add(tablaDatos(
                    "Fecha de consulta:", fecha(h.getFechaConsulta()),
                    "Estado:", h.getEstado(),
                    "Procedimiento:", h.getTipoProcedimiento(),
                    "Fecha de cierre:", fecha(h.getFechaCierre())));
            doc.add(bloque("Motivo de consulta", h.getMotivoConsulta()));
            doc.add(bloque("Antecedentes médicos", h.getAntecedentesMedicos()));
            doc.add(bloque("Diagnóstico", h.getDiagnostico()));
            doc.add(bloque("Observaciones", h.getObservaciones()));

            // 4) Consentimiento informado
            doc.add(seccion("CONSENTIMIENTO INFORMADO"));
            doc.add(tablaDatos(
                    "Estado:", h.getEstadoConsentimiento(),
                    "Fecha de firma:", fecha(h.getFechaFirma()),
                    "Fecha de revocación:", fecha(h.getFechaRevocacion())));

            // 5) Especialista responsable
            doc.add(seccion("ESPECIALISTA RESPONSABLE"));
            doc.add(tablaDatos("Nombre:", h.getNombreEspecialista()));

            // 6) Líneas de firma
            doc.add(firmas(
                    valor(h.getNombrePaciente()) + "\nIdentificación: " + valor(h.getIdentificacionPaciente()),
                    valor(h.getNombreEspecialista())));

            // 7) Pie con la fecha de generación
            Paragraph pie = new Paragraph("Documento de carácter reservado. Generado el "
                    + LocalDateTime.now().format(FECHA_HORA), PIE);
            pie.setAlignment(Element.ALIGN_CENTER);
            pie.setSpacingBefore(20);
            doc.add(pie);

            doc.close();
            return salida.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la historia clínica", e);
        }
    }

    // ---- Piezas del documento ----

    /** Franja de color con el título de una sección. */
    private PdfPTable seccion(String titulo) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(12);
        PdfPCell c = new PdfPCell(new Phrase(titulo, SECCION));
        c.setBackgroundColor(COLOR_PRINCIPAL);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(5);
        t.addCell(c);
        return t;
    }

    /** Tabla de datos cortos: recibe pares etiqueta/valor y los acomoda de a dos pares por fila. */
    private PdfPTable tablaDatos(String... pares) {
        PdfPTable t = new PdfPTable(new float[] {1.4f, 2f, 1.4f, 2f});
        t.setWidthPercentage(100);
        for (int i = 0; i < pares.length; i += 2) {
            t.addCell(celda(pares[i], ETIQUETA, Rectangle.NO_BORDER));
            t.addCell(celda(valor(pares[i + 1]), NORMAL, Rectangle.NO_BORDER));
        }
        // Si la última fila quedó incompleta, se rellena para que se dibuje
        if ((pares.length / 2) % 2 == 1) {
            t.addCell(celda("", NORMAL, Rectangle.NO_BORDER));
            t.addCell(celda("", NORMAL, Rectangle.NO_BORDER));
        }
        return t;
    }

    /** Recuadro de ancho completo para textos largos (motivo, antecedentes, diagnóstico...). */
    private PdfPTable bloque(String etiqueta, String contenido) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.addCell(celda(etiqueta, ETIQUETA, Rectangle.NO_BORDER));
        PdfPCell c = celda(texto(contenido, "No registrado"), NORMAL, Rectangle.BOX);
        c.setPadding(6);
        t.addCell(c);
        return t;
    }

    /** Dos líneas para la firma manuscrita del paciente y del especialista. */
    private PdfPTable firmas(String paciente, String especialista) {
        PdfPTable t = new PdfPTable(new float[] {1f, 0.2f, 1f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(40);

        PdfPCell linea1 = celda("", NORMAL, Rectangle.BOTTOM);
        linea1.setFixedHeight(35);
        PdfPCell linea2 = celda("", NORMAL, Rectangle.BOTTOM);
        linea2.setFixedHeight(35);
        t.addCell(linea1);
        t.addCell(celda("", NORMAL, Rectangle.NO_BORDER));
        t.addCell(linea2);

        t.addCell(centrada("Firma del paciente\n" + paciente));
        t.addCell(celda("", NORMAL, Rectangle.NO_BORDER));
        t.addCell(centrada("Firma del especialista\n" + especialista));
        return t;
    }

    private PdfPCell celda(String contenido, Font fuente, int borde) {
        PdfPCell c = new PdfPCell(new Phrase(contenido, fuente));
        c.setBorder(borde);
        c.setPadding(4);
        return c;
    }

    private PdfPCell centrada(String contenido) {
        PdfPCell c = celda(contenido, ETIQUETA, Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private Paragraph centrado(String contenido, Font fuente, float espacioDespues) {
        Paragraph p = new Paragraph(contenido.isBlank() ? " " : contenido, fuente);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(espacioDespues);
        return p;
    }

    // ---- Utilidades de formato ----

    /** Un campo corto vacío se muestra con un guion. */
    private static String valor(String t) {
        return (t == null || t.isBlank()) ? "-" : t.trim();
    }

    private static String texto(String t, String porDefecto) {
        return (t == null || t.isBlank()) ? porDefecto : t.trim();
    }

    /** Convierte AAAA-MM-DD en DD/MM/AAAA; si el formato no es el esperado, lo deja como llegó. */
    private static String fecha(String t) {
        if (t == null || t.isBlank()) {
            return "-";
        }
        try {
            return LocalDate.parse(t.substring(0, 10)).format(FECHA);
        } catch (RuntimeException e) {
            return t;
        }
    }

    private static String sexo(String s) {
        if ("M".equals(s)) {
            return "Masculino";
        }
        if ("F".equals(s)) {
            return "Femenino";
        }
        return valor(s);
    }

    /** Une las partes que no estén vacías con un separador. */
    private static String unir(String separador, String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(separador);
                }
                sb.append(parte.trim());
            }
        }
        return sb.toString();
    }

    private static String prefijo(String prefijo, String t) {
        return (t == null || t.isBlank()) ? null : prefijo + t.trim();
    }
}