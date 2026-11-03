package Servicios;

import Modelos.Turno; // Modelo de datos que representa un turno médico y su paciente asociado.

import com.itextpdf.text.Document; // Objeto principal que representa lógicamente el archivo PDF a generar.
import com.itextpdf.text.DocumentException; // Maneja excepciones o errores estructurales generados por la librería iText.
import com.itextpdf.text.Font; // Permite configurar estilos tipográficos, tamaños y familias de fuentes para el texto.
import com.itextpdf.text.Paragraph; // Permite agregar bloques de texto formateados y títulos con alineación personalizada.
import com.itextpdf.text.Phrase; // Contenedor de texto con formato que se utiliza habitualmente dentro de las celdas de las tablas.
import com.itextpdf.text.pdf.PdfPCell; // Representa una celda individual dentro de una tabla en el PDF.
import com.itextpdf.text.pdf.PdfPTable; // Permite estructurar y maquetar tablas tabulares con múltiples columnas y anchos definidos.
import com.itextpdf.text.pdf.PdfWriter; // Asocia el documento lógico con un flujo de salida para escribir el archivo físico en disco.

import java.io.FileOutputStream; // Flujo de salida encargado de escribir los datos binarios del PDF en el disco del equipo.
import java.io.IOException; // Maneja excepciones de entrada/salida (I/O) si ocurre un fallo al crear o escribir el archivo.
import java.sql.Connection; // Representa una sesión activa de conexión con la base de datos relacional MySQL.
import java.sql.PreparedStatement; // Permite ejecutar sentencias SQL precompiladas de forma segura y parametrizada contra inyecciones.
import java.sql.ResultSet; // Almacena y permite recorrer secuencialmente los registros devueltos por una consulta SELECT.
import java.sql.SQLException; // Gestiona los errores y excepciones derivados de la interacción JDBC con el motor de base de datos.
import java.time.LocalDate; // Maneja fechas calendarias (año, mes, día) sin zona horaria, esencial para filtrar los turnos del día.
import java.time.LocalTime; // Maneja horarios exactos (hora, minuto, segundo) para mostrar las franjas de atención de los turnos.
import java.time.format.DateTimeFormatter; // Facilita la conversión y el formateo personalizado de fechas y horas a cadenas de texto (ej. dd-MM-yyyy).
import java.util.ArrayList; // Implementación estándar de listas dinámicas para almacenar temporalmente los turnos consultados.
import java.util.List; // Interfaz genérica para el manejo y paso de colecciones de objetos en forma de lista.

/**
 * Clase ReportePdfService:
 * Servicio encargado de la generación y exportación de reportes en formato PDF
 * para el consultorio médico (como agendas de turnos y historiales).
 */
public class ReportePdfService {
    /**
     * Genera un documento PDF estructurado con la agenda de turnos para una fecha específica.
     * 
     * @param fecha Fecha de la agenda consultada.
     * @param turnos Lista de turnos registrados para ese día.
     */
    public void generarPdfAgenda(LocalDate fecha, List<Turno> turnos) {
        String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String nombreArchivo = "Agenda_" + fechaFormateada + ".pdf";
        
        Document documento = new Document();
        
        try{
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();
            
            // Definición de fuentes y estilos tipográficos.
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            Font cabeceraTablaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font contenidoTablaFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            
            // Encabezado del reporte.
            Paragraph titulo = new Paragraph("CONSULTORIO MÉDICO - AGENDA DE TURNOS", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);
            
            Paragraph subtitulo = new Paragraph("Fecha Consultada: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n\n", subtituloFont);
            subtitulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(subtitulo);
            
            // Creación de tabla con 5 columnas (Horario, Paciente, DNI, Motivo, Estado).
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{18f, 25f, 15f, 27f, 15f});
            
            // Cabeceras de la tabla.
            String[] cabeceras = {"Horario", "Paciente", "DNI", "Motivo", "Estado"};
            for(String cabecera : cabeceras){
                PdfPCell celda = new PdfPCell(new Phrase(cabecera, cabeceraTablaFont));
                celda.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }
            
            // Inserción de los datos de cada turno en las filas de la tabla.
            for(Turno tu : turnos){
                tabla.addCell(new Phrase(tu.getHorarioInicio() + " - " + tu.getHorarioFin(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getPaciente().getNombreCompleto(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getPaciente().getDni(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getMotivo(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getEstado(), contenidoTablaFont));
            }
            
            documento.add(tabla);
            System.out.println("\n[Éxito] Archivo PDF generado y guardado como: " + nombreArchivo + "\n");
            
        }catch(DocumentException | IOException e){
            System.out.println("Error al generar el archivo PDF: " + e.getMessage());
        }finally{
            if(documento.isOpen()){
                documento.close();
            }
        }
    }
    
    /**
     * Consulta automáticamente los turnos del día actual (fecha del sistema) en la base de datos
     * y genera un reporte en formato PDF listo para imprimir o visualizar en el consultorio.
     */
    public void generarPdfTurnosDelDia() {
        LocalDate fechaHoy = LocalDate.now();
        String fechaFormateada = fechaHoy.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String nombreArchivo = "Turnos_Del_Dia_" + fechaFormateada + ".pdf";
        
        // Consulta SQL para obtener los turnos de hoy (excluyendo cancelados y ordenados por horario)
        String sql = "SELECT t.id, t.fecha, t.horario_inicio, t.horario_fin, t.motivo, t.estado, " +
                     "p.id AS paciente_id, p.nombre_completo, p.dni, p.numero_de_telefono, p.obra_social " +
                     "FROM Turno t " +
                     "JOIN Paciente p ON t.paciente_id = p.id " +
                     "WHERE t.fecha = ? AND t.estado <> 'Cancelado' " +
                     "ORDER BY t.horario_inicio ASC";
        
        List<Turno> turnosHoy = new ArrayList<>();
        Conexion conexionDB = new Conexion();
        
        try(Connection con = conexionDB.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)){
            
            pstmt.setObject(1, fechaHoy);
            
            try(ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    Turno turn = new Turno();
                    turn.setId(rs.getInt("id"));
                    turn.setFecha(rs.getObject("fecha", LocalDate.class));
                    turn.setHorarioInicio(rs.getObject("horario_inicio", LocalTime.class));
                    turn.setHorarioFin(rs.getObject("horario_fin", LocalTime.class));
                    turn.setMotivo(rs.getString("motivo"));
                    turn.setEstado(rs.getString("estado"));
                    
                    turn.getPaciente().setId(rs.getInt("paciente_id"));
                    turn.getPaciente().setNombreCompleto(rs.getString("nombre_completo"));
                    turn.getPaciente().setDni(rs.getString("dni"));
                    turn.getPaciente().setNumeroDeTelefono(rs.getString("numero_de_telefono"));
                    turn.getPaciente().setObraSocial(rs.getString("obra_social"));
                    
                    turnosHoy.add(turn);
                }
            }
        }catch(SQLException e){
            System.out.println("Error al consultar los turnos del día en la base de datos: " + e.getMessage());
            return;
        }
        
        if (turnosHoy.isEmpty()) {
            System.out.println("\n[AVISO] No hay turnos registrados para el día de hoy (" + fechaHoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "). No se generó el PDF.\n");
            return;
        }
        
        // Estructuración y generación del archivo PDF mediante iText
        Document documento = new Document();
        
        try{
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();
            
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            Font cabeceraTablaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font contenidoTablaFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            
            Paragraph titulo = new Paragraph("CONSULTORIO MÉDICO - TURNOS DEL DÍA", tituloFont);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);
            
            Paragraph subtitulo = new Paragraph("Fecha de Emisión: " + fechaHoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                               " | Total Turnos: " + turnosHoy.size() + "\n\n", subtituloFont);
            subtitulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(subtitulo);
            
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{18f, 25f, 15f, 27f, 15f});
            
            String[] cabeceras = {"Horario", "Paciente", "DNI", "Motivo", "Estado"};
            for(String cabecera : cabeceras){
                PdfPCell celda = new PdfPCell(new Phrase(cabecera, cabeceraTablaFont));
                celda.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }
            
            for(Turno tu : turnosHoy){
                tabla.addCell(new Phrase(tu.getHorarioInicio() + " - " + tu.getHorarioFin(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getPaciente().getNombreCompleto(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getPaciente().getDni(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getMotivo(), contenidoTablaFont));
                tabla.addCell(new Phrase(tu.getEstado(), contenidoTablaFont));
            }
            
            documento.add(tabla);
            System.out.println("\n[Éxito] Reporte PDF de los turnos del día generado como: " + nombreArchivo + "\n");
            
        }catch(DocumentException | IOException e){
            System.out.println("Error al generar el archivo PDF de turnos del día: " + e.getMessage());
        }finally{
            if(documento.isOpen()){
                documento.close();
            }
        }
    }
}