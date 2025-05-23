package com.tingeso.reservascomprobantesservice.Service;

import com.tingeso.reservascomprobantesservice.Entity.ComprobanteEntity;
import com.tingeso.reservascomprobantesservice.Entity.ReservaEntity;
import com.tingeso.reservascomprobantesservice.Repository.ComprobanteRepository;
import com.tingeso.reservascomprobantesservice.Repository.ReservaRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import org.springframework.web.client.RestTemplate;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ComprobanteService {
    // Logger for error handling
    private static final Logger logger = LoggerFactory.getLogger(ComprobanteService.class);

    @Autowired
    public ComprobanteRepository comprobanteRepository;
    @Autowired
    public ReservaRepository reservaRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public ComprobanteEntity crearcomprobante(long idreserva) {
        Optional<ReservaEntity> reservaop = reservaRepository.findById(idreserva);
        if (reservaop.isEmpty()) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        ReservaEntity reserva = reservaop.get();

        // Validaciones básicas de la reserva
        if (reserva.getCantidadpersonas() <= 0) {
            throw new IllegalArgumentException("La cantidad de personas debe ser mayor que 0");
        }

        // --- LLAMADAS A OTROS MICROSERVICIOS ---

        // 1. Obtener Precio Inicial del Tarifas Service (M1)
        // Endpoint: GET http://localhost:8081/tarifas/precio-inicial?tipoReserva={tipoReserva}&cantidadPersonas={cantidadPersonas}
        String tarifasServiceUrl = "http://localhost:8081/tarifas/precio-inicial?tipoReserva=" + reserva.getTiporeserva() + "&cantidadPersonas=" + reserva.getCantidadpersonas();
        Float precioInicialBruto = restTemplate.getForObject(tarifasServiceUrl, Float.class);

        if (precioInicialBruto == null || precioInicialBruto <= 0) {
            throw new IllegalStateException("No se pudo obtener un precio inicial válido del servicio de tarifas.");
        }

        // 2. Aplicar Descuento/Recargo por Días Especiales del Tarifas Especiales Service (M4)
        // Endpoint: GET http://localhost:8084/tarifas-especiales/aplicar-descuento-dia-especial?fechahora={fechahora}&precioInicialBase={precioInicialBase}
        String tarifasEspecialesServiceUrl = "http://localhost:8084/tarifas-especiales/aplicar-descuento-dia-especial?fechahora=" + reserva.getFechahora() + "&precioInicialBase=" + precioInicialBruto;
        Float precioConDescuentoDiaEspecial = restTemplate.getForObject(tarifasEspecialesServiceUrl, Float.class);

        if (precioConDescuentoDiaEspecial == null) {
            precioConDescuentoDiaEspecial = precioInicialBruto; // Si hay error, no aplicar descuento especial
        }

        // Usamos este precio como base para los descuentos finales
        float precioParaDescuentos = precioConDescuentoDiaEspecial;

        // 3. Calcular Descuento por Grupo del Descuentos Grupo Service (M2)
        // Endpoint: GET http://localhost:8082/descuentos-grupo/calcular?cantidadPersonas={cantidadPersonas}&precioInicial={precioInicial}
        String descuentosGrupoServiceUrl = "http://localhost:8082/descuentos-grupo/calcular?cantidadPersonas=" + reserva.getCantidadpersonas() + "&precioInicial=" + precioParaDescuentos;
        Float dctogrupo = restTemplate.getForObject(descuentosGrupoServiceUrl, Float.class);
        if (dctogrupo == null) dctogrupo = 0f;

        // 4. Calcular Descuento Especial (Frecuente) del Descuentos Frecuentes Service (M3)
        // Endpoint: GET http://localhost:8083/descuentos-frecuentes/calcular?rutCliente={rutCliente}&precioInicial={precioInicial}
        String descuentosFrecuentesServiceUrl = "http://localhost:8083/descuentos-frecuentes/calcular?rutCliente=" + reserva.getRutusuario() + "&precioInicial=" + precioParaDescuentos;
        Float dctoespecial = restTemplate.getForObject(descuentosFrecuentesServiceUrl, Float.class);
        if (dctoespecial == null) dctoespecial = 0f;

        // 5. Calcular Descuento por Cumpleaños del Tarifas Especiales Service (M4)
        // Endpoint: GET http://localhost:8084/tarifas-especiales/calcular-descuento-cumpleanos?cantidadPersonas={cantidadPersonas}&precioInicialOriginal={precioInicialOriginal}&cantidadCumple={cantidadCumple}
        String cumpleanosServiceUrl = "http://localhost:8084/tarifas-especiales/calcular-descuento-cumpleanos?cantidadPersonas=" + reserva.getCantidadpersonas() + "&precioInicialOriginal=" + precioInicialBruto + "&cantidadCumple=" + reserva.getCantidadcumple();
        Float dctocumple = restTemplate.getForObject(cumpleanosServiceUrl, Float.class);
        if (dctocumple == null) dctocumple = 0f;

        // --- FIN LLAMADAS A OTROS MICROSERVICIOS ---

        // Lógica de aplicación del mayor descuento (la que ya tenías)
        List<Float> descuentos = List.of(dctoespecial, dctogrupo, dctocumple);
        float descuentoAplicado = descuentos.stream().max(Float::compare).orElse(0f); // Mayor descuento
        float preciofinalConDescuentoMax = precioParaDescuentos - descuentoAplicado; // Precio con el mejor descuento

        float iva = preciofinalConDescuentoMax * 0.19f;
        float precioTotalConIva = preciofinalConDescuentoMax + iva;

        // Calcula la tarifa base por persona, usando el precioInicialBruto antes de descuentos por día especial
        float tarifabase = precioInicialBruto / reserva.getCantidadpersonas();

        // Crear y guardar el comprobante
        ComprobanteEntity comprobante = new ComprobanteEntity(
                dctocumple,
                dctoespecial,
                dctogrupo,
                idreserva,
                precioInicialBruto, // precio (campo en ComprobanteEntity que se usa para precioInicial)
                precioTotalConIva, // preciofinal
                tarifabase,
                iva
        );
        comprobanteRepository.save(comprobante);

        // Generar y enviar PDF
        byte[] pdf = generarPDFComprobante(comprobante, reserva);
        enviarComprobantePorCorreo(reserva.getEmail(), pdf);
        return comprobante;
    }

    @Transactional
    public List<Object> reporteportiporeserva(String mesinicio, String mesfin) {
        // Validar que los meses no sean nulos
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }

        // Parsear los meses de inicio y fin (formato yyyy-MM)
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser yyyy-MM");
        }

        // Verificar que el mes de inicio sea anterior o igual al mes de fin
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Formato para parsear fechahora de las reservas (yyyy-MM-dd'T'HH:mm)
        DateTimeFormatter formatoReserva = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Obtener todas las reservas
        List<ReservaEntity> reservas = reservaRepository.findAll();

        // Filtrar reservas dentro del rango de meses
        List<ReservaEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> {
                    LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                    YearMonth mesReserva = YearMonth.from(fechaReserva);
                    return !mesReserva.isBefore(inicio) && !mesReserva.isAfter(fin);
                })
                .collect(Collectors.toList());

        // Calcular el número de meses en el rango (inclusive)
        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;

        // Crear la lista de resultados
        List<Object> reporte = new ArrayList<>(mesesRango);

        // Iterar sobre cada mes en el rango
        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            // Definir el inicio y fin del mes actual como LocalDateTime
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            // Filtrar reservas del mes actual
            List<ReservaEntity> reservasMes = reservasFiltradas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            // Calcular totales por tipo de reserva usando el precio final pagado
            long totalTipo1 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 1)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalTipo2 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 2)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalTipo3 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 3)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();

            // Crear sublista con los totales
            List<Long> totalesMes = List.of(totalTipo1, totalTipo2, totalTipo3);
            reporte.add(totalesMes);

            // Avanzar al siguiente mes
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }

    @Transactional
    public List<Object> reporteporgrupo(String mesinicio,String mesfin){
        // Validar que los meses no sean nulos
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }

        // Parsear los meses de inicio y fin (formato yyyy-MM)
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser yyyy-MM");
        }

        // Verificar que el mes de inicio sea anterior o igual al mes de fin
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Formato para parsear fechahora de las reservas (yyyy-MM-dd'T'HH:mm)
        DateTimeFormatter formatoReserva = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Obtener todas las reservas
        List<ReservaEntity> reservas = reservaRepository.findAll();

        // Filtrar reservas dentro del rango de meses
        List<ReservaEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> {
                    LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                    YearMonth mesReserva = YearMonth.from(fechaReserva);
                    return !mesReserva.isBefore(inicio) && !mesReserva.isAfter(fin);
                })
                .collect(Collectors.toList());

        // Calcular el número de meses en el rango (inclusive)
        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;

        // Crear la lista de resultados
        List<Object> reporte = new ArrayList<>(mesesRango);

        // Iterar sobre cada mes en el rango
        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            // Definir el inicio y fin del mes actual como LocalDateTime
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            // Filtrar reservas del mes actual
            List<ReservaEntity> reservasMes = reservasFiltradas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            // Calcular totales por cantidad de personas: 1-2, 3-5, 6-10, 11-15
            long totalGrupo1_2 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 1 && r.getCantidadpersonas() <= 2)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo3_5 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 3 && r.getCantidadpersonas() <= 5)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo6_10 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 6 && r.getCantidadpersonas() <= 10)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo11_15 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 11 && r.getCantidadpersonas() <= 15)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();

            // Crear sublista con los totales
            List<Long> totalesMes = List.of(totalGrupo1_2, totalGrupo3_5, totalGrupo6_10, totalGrupo11_15);
            reporte.add(totalesMes);

            // Avanzar al siguiente mes
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }

    //Metodo para enviar el comprobante por correo
    public void enviarComprobantePorCorreo(String destinatario, byte[] pdfBytes) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            helper.setTo(destinatario);
            helper.setSubject("🏎️ Comprobante de tu reserva en KartingGO");
            helper.setText("Hola! Adjunto encontrarás el comprobante de tu reserva. ¡Gracias por reservar!");

            helper.addAttachment("Comprobante.pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage(), e);
        }
    }

    public byte[] generarPDFComprobante(ComprobanteEntity comprobante, ReservaEntity reserva) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4); // Use standard page size
            document.setMargins(36, 36, 36, 36); // Add some margins (top, right, bottom, left)

            // Use Locale for formatting numbers consistently (e.g., Chile uses '.' for thousands)
            // Adjust if your target audience uses different formatting
            Locale chileLocale = new Locale("es", "CL");

            // ----- Header -----
            Paragraph header = new Paragraph()
                    .add(new Text("🏁 Comprobante de Reserva - KartingRM 🏁").setBold().setFontSize(16))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20); // Add space below header
            document.add(header);

            // ----- Customer and Reservation Info Table -----
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})) // 2 columns, define relative widths
                    .setWidth(UnitValue.createPercentValue(100)) // Use full width
                    .setMarginBottom(15);

            // Helper method to add info rows cleanly
            addInfoRow(infoTable, "Nombre Cliente:", reserva.getNombreusuario());
            addInfoRow(infoTable, "RUT Cliente:", reserva.getRutusuario());
            addInfoRow(infoTable, "Email Cliente:", reserva.getEmail());

            // Format date and time nicely
            String fechaHoraFormateada = "No disponible";
            try {
                LocalDateTime fechaHoraReserva = LocalDateTime.parse(reserva.getFechahora(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                fechaHoraFormateada = fechaHoraReserva.format(DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy, HH:mm 'hrs'", chileLocale));
            } catch (Exception e) {
                System.out.println("Error al formatear fecha y hora: " + e.getMessage());
            }
            addInfoRow(infoTable, "Fecha y Hora:", fechaHoraFormateada);

            String tipoReservaDesc;
            switch (reserva.getTiporeserva()) {
                case 1: tipoReservaDesc = "Normal (10 vueltas / 10 min)"; break;
                case 2: tipoReservaDesc = "Extendida (15 vueltas / 15 min)"; break;
                case 3: tipoReservaDesc = "Premium (20 vueltas / 20 min)"; break;
                default: tipoReservaDesc = "Desconocido (" + reserva.getTiporeserva() + ")";
            }
            addInfoRow(infoTable, "Tipo Reserva:", tipoReservaDesc);
            addInfoRow(infoTable, "Cantidad Personas:", String.valueOf(reserva.getCantidadpersonas()));
            if(reserva.getCantidadcumple() > 0) {
                addInfoRow(infoTable, "Personas Cumpleaños:", String.valueOf(reserva.getCantidadcumple()));
            }


            document.add(infoTable);
            Paragraph ifodscto= new Paragraph().add(new Text("El descuento aplicado es el mejor para usted!").setBold().setFontSize(12))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20); // Add space below header
            document.add(ifodscto);


            // ----- Pricing Details Table -----
            document.add(new Paragraph("Detalle de Precios:").setBold().setMarginBottom(5));

            Table pricingTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})) // Concept, Value
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            // Header Row
            pricingTable.addHeaderCell(createHeaderCell("Concepto"));
            pricingTable.addHeaderCell(createHeaderCell("Valor (CLP)"));

            // Pricing Rows using helper method
            addPricingRow(pricingTable, "Precio Base Inicial (Total)", comprobante.getPrecio(), chileLocale);
            if (reserva.getCantidadpersonas() > 0) {
                addPricingRow(pricingTable, String.format(chileLocale,"Tarifa Base por Persona (%d)", reserva.getCantidadpersonas()), comprobante.getTarifabase(), chileLocale);
            }

            // Show discounts only if they were applied (value > 0)
            float totalDescuentos = 0;
            if (comprobante.getDctocumple() > 0) {
                addPricingRow(pricingTable, "Descuento Cumpleaños", -comprobante.getDctocumple(), chileLocale);
                totalDescuentos += comprobante.getDctocumple();
            }
            if (comprobante.getDctogrupo() > 0) {
                addPricingRow(pricingTable, "Descuento Grupo", -comprobante.getDctogrupo(), chileLocale);
                totalDescuentos += comprobante.getDctogrupo();
            }
            if (comprobante.getDctoespecial() > 0) {
                addPricingRow(pricingTable, "Descuento Especial", -comprobante.getDctoespecial(), chileLocale);
                totalDescuentos += comprobante.getDctoespecial();
            }

            // Add a subtotal line for clarity
            float subtotal = comprobante.getPreciofinal()-comprobante.getValoriva(); // Precio includes IVA as per your entity setup
            addPricingRow(pricingTable, "Subtotal (Neto)", subtotal, chileLocale);
            addPricingRow(pricingTable, "IVA (19%)", comprobante.getValoriva(), chileLocale); // Use 'iva' field
            // --- Total Row (Footer of the table) ---
            Cell totalLabelCell = new Cell(1, 1)
                    .add(new Paragraph("MONTO TOTAL A PAGAR").setBold())
                    .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1)) // Line above total
                    .setBorderBottom(Border.NO_BORDER)
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingTop(5); // Add some space before the total
            pricingTable.addFooterCell(totalLabelCell);

            Cell totalValueCell = new Cell(1, 1)
                    .add(new Paragraph(String.format(chileLocale, "$%,.0f", (comprobante.getPreciofinal()))).setBold()) // Use 'precio' field for total
                    .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1))
                    .setBorderBottom(Border.NO_BORDER)
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingTop(5);
            pricingTable.addFooterCell(totalValueCell);


            document.add(pricingTable);

            // ----- Footer Message -----
            Paragraph footerMessage = new Paragraph("¡Gracias por preferir KartingRM! Te esperamos.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setFontSize(10)
                    .setMarginTop(20);
            document.add(footerMessage);


            // ----- Finish -----
            document.close(); // This is important!
            logger.info("PDF generado exitosamente para reserva ID: {}", comprobante.getIdreserva());
            return outputStream.toByteArray();

        } catch (Exception e) {
            // Log the error with more details
            logger.error("Error al generar PDF para reserva ID {}: {}",
                    (comprobante != null ? comprobante.getIdreserva() : "N/A"),
                    e.getMessage(), e);
            // Re-throw as a runtime exception so the caller knows something went wrong
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    // ----- Helper Methods for Table Creation -----

    /** Adds a row to the info table with a label and value. */
    private void addInfoRow(Table table, String label, String value) {
        // Label Cell (Bold)
        Cell labelCell = new Cell().add(new Paragraph(new Text(label).setBold()))
                .setBorder(Border.NO_BORDER) // No borders for clean look
                .setTextAlignment(TextAlignment.LEFT)
                .setPaddingRight(10); // Space between label and value
        table.addCell(labelCell);

        // Value Cell
        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : "-")) // Handle null values
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(valueCell);
    }

    /** Adds a row to the pricing table with description and formatted value. */
    private void addPricingRow(Table table, String description, float value, Locale locale) {
        // Description Cell
        Cell descCell = new Cell().add(new Paragraph(description))
                .setBorder(Border.NO_BORDER) // Keep borders clean inside table body
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(descCell);

        // Value Cell (Formatted Currency, Right Aligned)
        String formattedValue = String.format(locale, "$%,.0f", value); // Format with thousand separators, no decimals
        Cell valueCell = new Cell().add(new Paragraph(formattedValue))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(valueCell);
    }

    /** Creates a styled header cell for tables. */
    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY) // Light gray background
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1)); // Bottom border for header
    }

}
