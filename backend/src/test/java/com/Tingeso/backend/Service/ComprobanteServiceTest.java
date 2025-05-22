package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.ComprobanteEntity;
import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ComprobanteRepository;
import com.Tingeso.backend.Repository.ReservaRepository;
import jakarta.mail.Session; // Necesario para mockear MimeMessage
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Para capturar argumentos
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito con JUnit 5
class ComprobanteServiceTest {

    @Mock // Mock para el repositorio de comprobantes
    private ComprobanteRepository comprobanteRepository;

    @Mock // Mock para el repositorio de reservas
    private ReservaRepository reservaRepository;

    @Mock // Mock para el servicio de reservas (para los cálculos de descuento/precio)
    private ReservaService reservaService;

    @Mock // Mock para el servicio de envío de correos
    private JavaMailSender mailSender;

    @Mock // Mock para el objeto MimeMessage (necesario para verificar el envío de correo)
    private MimeMessage mimeMessage;

    @InjectMocks // Instancia real de ComprobanteService con los mocks inyectados
    private ComprobanteService comprobanteService;

    private ReservaEntity reservaEjemplo;
    private ComprobanteEntity comprobanteEjemplo;

    @BeforeEach // Se ejecuta antes de CADA test
    void setUp() {
        // Creamos una reserva de ejemplo base para usar en varios tests
        // Constructor ReservaEntity(cantCumple, cantPersonas, email, fechaHora, nombre, rut, tipoReserva)
        reservaEjemplo = new ReservaEntity(1, 4, "test@example.com", "2025-05-10T15:00", "Test User", "11111111-1", 1);
        reservaEjemplo.setId(1L); // Asignamos un ID simulado

        // Creamos un comprobante de ejemplo asociado, basado en cálculos esperados
        // Constructor ComprobanteEntity(dctocumple, dctoespecial, dctogrupo, idreserva, precio(Inicial), preciofinal(ConIVA), tarifabase, valoriva)
        float precioInicialEsperado = 60000f; // Asumimos este valor devuelto por reservaService.calcularprecioinicial
        float tarifaBaseEsperada = 15000f; // 60000 / 4
        float dctoCumpleEsperado = 7500f; // tarifa * 0.5 = 15000 * 0.5
        float dctoGrupoEsperado = 6000f; // 10% de 60000
        float dctoEspecialEsperado = 1000f; // Valor de ejemplo
        float descuentoAplicado = dctoCumpleEsperado; // El mayor es el de cumpleaños
        float precioConDescuento = precioInicialEsperado - descuentoAplicado; // 60000 - 7500 = 52500
        float ivaEsperado = precioConDescuento * 0.19f; // 52500 * 0.19 = 9975
        float precioFinalEsperado = precioConDescuento + ivaEsperado; // 52500 + 9975 = 62475

        comprobanteEjemplo = new ComprobanteEntity(
                dctoCumpleEsperado,
                dctoEspecialEsperado,
                dctoGrupoEsperado,
                reservaEjemplo.getId(),
                precioInicialEsperado, // precio base inicial
                precioFinalEsperado, // precio final con iva
                tarifaBaseEsperada,
                ivaEsperado
        );
        comprobanteEjemplo.setId(100L); // ID simulado para el comprobante
    }

    // --- Tests para crearcomprobante ---

    @Test
    void testCrearComprobante_Exito() {
        // Arrange
        long idReserva = reservaEjemplo.getId();
        float precioInicialMock = 60000f;
        float dctoGrupoMock = 6000f;
        float dctoEspecialMock = 1000f;
        float dctoCumpleMock = 7500f;

        // Configurar mocks
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaEjemplo));
        when(reservaService.calcularprecioinicial(idReserva)).thenReturn(precioInicialMock);
        when(reservaService.calcularDescuentoGrupo(reservaEjemplo.getCantidadpersonas(), precioInicialMock)).thenReturn(dctoGrupoMock);
        when(reservaService.calcularDescuentoEspecial(reservaEjemplo.getRutusuario(), precioInicialMock)).thenReturn(dctoEspecialMock);
        when(reservaService.descuentoporcumpleano(reservaEjemplo.getCantidadpersonas(), precioInicialMock, reservaEjemplo.getCantidadcumple())).thenReturn(dctoCumpleMock);

        // Simular que save devuelve la entidad guardada
        when(comprobanteRepository.save(any(ComprobanteEntity.class))).thenAnswer(invocation -> {
            ComprobanteEntity saved = invocation.getArgument(0);
            saved.setId(comprobanteEjemplo.getId()); // Asignar ID simulado
            return saved;
        });

        // Simular la creación del MimeMessage para el envío de correo
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // doNothing().when(mailSender).send(any(MimeMessage.class)); // Alternativa si send es void

        // Act
        ComprobanteEntity resultado = comprobanteService.crearcomprobante(idReserva);

        // Assert
        assertNotNull(resultado);
        assertEquals(idReserva, resultado.getIdreserva());
        assertEquals(precioInicialMock, resultado.getPrecio(), 0.01);
        assertEquals(dctoGrupoMock, resultado.getDctogrupo(), 0.01);
        assertEquals(dctoEspecialMock, resultado.getDctoespecial(), 0.01);
        assertEquals(dctoCumpleMock, resultado.getDctocumple(), 0.01);
        assertEquals(comprobanteEjemplo.getTarifabase(), resultado.getTarifabase(), 0.01);
        assertEquals(comprobanteEjemplo.getValoriva(), resultado.getValoriva(), 0.01);
        assertEquals(comprobanteEjemplo.getPreciofinal(), resultado.getPreciofinal(), 0.01);

        // Verificar que save fue llamado una vez
        ArgumentCaptor<ComprobanteEntity> comprobanteCaptor = ArgumentCaptor.forClass(ComprobanteEntity.class);
        verify(comprobanteRepository, times(1)).save(comprobanteCaptor.capture());
        // Podríamos hacer más asserts sobre comprobanteCaptor.getValue()

        // Verificar que el correo fue enviado una vez
        // Usamos ArgumentCaptor si queremos verificar detalles del mensaje,
        // o simplemente verify si solo queremos saber si se llamó.
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testCrearComprobante_Falla_ReservaNoEncontrada() {
        // Arrange
        long idReservaInexistente = 99L;
        when(reservaRepository.findById(idReservaInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comprobanteService.crearcomprobante(idReservaInexistente);
        });

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(comprobanteRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testCrearComprobante_Falla_PrecioInicialCero() {
        // Arrange
        long idReserva = reservaEjemplo.getId();
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaEjemplo));
        when(reservaService.calcularprecioinicial(idReserva)).thenReturn(0f); // Simulamos precio 0

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comprobanteService.crearcomprobante(idReserva);
        });

        assertTrue(exception.getMessage().contains("El precio inicial debe ser mayor que 0"));
        verify(comprobanteRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testCrearComprobante_Falla_CantidadPersonasCero() {
        // Arrange
        long idReserva = reservaEjemplo.getId();
        reservaEjemplo.setCantidadpersonas(0); // Modificamos la reserva de ejemplo
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaEjemplo));
        // Asumimos que calcularprecioinicial funciona incluso con 0 personas (aunque debería validar antes)
        when(reservaService.calcularprecioinicial(idReserva)).thenReturn(5000f);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comprobanteService.crearcomprobante(idReserva);
        });

        assertTrue(exception.getMessage().contains("La cantidad de personas debe ser mayor que 0"));
        verify(comprobanteRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // --- Tests para reporteportiporeserva ---

    @Test
    void testReportePorTipoReserva_DatosValidos() {
        // Arrange
        String mesInicio = "2025-04";
        String mesFin = "2025-05";

        // Crear reservas de ejemplo
        ReservaEntity r1 = new ReservaEntity(0, 2, "r1@e.com", "2025-04-10T10:00", "U1", "1", 1); r1.setId(1L);
        ReservaEntity r2 = new ReservaEntity(0, 3, "r2@e.com", "2025-04-15T14:00", "U2", "2", 2); r2.setId(2L);
        ReservaEntity r3 = new ReservaEntity(0, 1, "r3@e.com", "2025-05-01T11:00", "U3", "3", 1); r3.setId(3L);
        ReservaEntity r4 = new ReservaEntity(0, 5, "r4@e.com", "2025-05-20T16:00", "U4", "4", 3); r4.setId(4L);
        ReservaEntity r5 = new ReservaEntity(0, 2, "r5@e.com", "2025-06-01T10:00", "U5", "5", 1); r5.setId(5L); // Fuera de rango

        List<ReservaEntity> todasLasReservas = List.of(r1, r2, r3, r4, r5);

        // Crear comprobantes simulados
        ComprobanteEntity c1 = new ComprobanteEntity(); c1.setIdreserva(1L); c1.setPreciofinal(25000f);
        ComprobanteEntity c2 = new ComprobanteEntity(); c2.setIdreserva(2L); c2.setPreciofinal(50000f);
        ComprobanteEntity c3 = new ComprobanteEntity(); c3.setIdreserva(3L); c3.setPreciofinal(15000f);
        ComprobanteEntity c4 = new ComprobanteEntity(); c4.setIdreserva(4L); c4.setPreciofinal(100000f);

        // Configurar mocks
        when(reservaRepository.findAll()).thenReturn(todasLasReservas);
        when(comprobanteRepository.findByIdreserva(1L)).thenReturn(Optional.of(c1));
        when(comprobanteRepository.findByIdreserva(2L)).thenReturn(Optional.of(c2));
        when(comprobanteRepository.findByIdreserva(3L)).thenReturn(Optional.of(c3));
        when(comprobanteRepository.findByIdreserva(4L)).thenReturn(Optional.of(c4));
        // No necesitamos mock para r5.getId() porque se filtra antes por fecha

        // Act
        List<Object> reporte = comprobanteService.reporteportiporeserva(mesInicio, mesFin);

        // Assert
        assertNotNull(reporte);
        assertEquals(2, reporte.size()); // Abril, Mayo

        // Mes de Abril (index 0)
        assertTrue(reporte.get(0) instanceof List);
        @SuppressWarnings("unchecked") // Suprimimos warning de casteo genérico
        List<Long> totalesAbril = (List<Long>) reporte.get(0);
        assertEquals(3, totalesAbril.size()); // Tipos 1, 2, 3
        assertEquals(25000L, totalesAbril.get(0)); // r1 (Tipo 1)
        assertEquals(50000L, totalesAbril.get(1)); // r2 (Tipo 2)
        assertEquals(0L, totalesAbril.get(2));     // Tipo 3

        // Mes de Mayo (index 1)
        assertTrue(reporte.get(1) instanceof List);
        @SuppressWarnings("unchecked")
        List<Long> totalesMayo = (List<Long>) reporte.get(1);
        assertEquals(3, totalesMayo.size()); // Tipos 1, 2, 3
        assertEquals(15000L, totalesMayo.get(0)); // r3 (Tipo 1)
        assertEquals(0L, totalesMayo.get(1));     // Tipo 2
        assertEquals(100000L, totalesMayo.get(2));// r4 (Tipo 3)

        verify(reservaRepository, times(1)).findAll();
        // Verificamos que se buscó comprobante para las 4 reservas dentro del rango
        verify(comprobanteRepository, times(1)).findByIdreserva(1L);
        verify(comprobanteRepository, times(1)).findByIdreserva(2L);
        verify(comprobanteRepository, times(1)).findByIdreserva(3L);
        verify(comprobanteRepository, times(1)).findByIdreserva(4L);
        verify(comprobanteRepository, never()).findByIdreserva(5L); // Nunca se buscó la r5
    }

    @Test
    void testReportePorTipoReserva_FormatoFechaInvalido() {
        // Arrange
        String mesInicio = "04-2025"; // Formato incorrecto
        String mesFin = "2025-05";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comprobanteService.reporteportiporeserva(mesInicio, mesFin);
        });
        assertTrue(exception.getMessage().contains("Formato de fecha inválido"));
    }

    @Test
    void testReportePorTipoReserva_RangoInvalido() {
        // Arrange
        String mesInicio = "2025-06";
        String mesFin = "2025-05"; // Fin antes que inicio

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            comprobanteService.reporteportiporeserva(mesInicio, mesFin);
        });
        assertTrue(exception.getMessage().contains("El mes de inicio debe ser anterior o igual al mes de fin"));
    }


    // --- Tests para reporteporgrupo ---

    @Test
    void testReportePorGrupo_DatosValidos() {
        // Arrange
        String mesInicio = "2025-04";
        String mesFin = "2025-04";

        // Reservas con diferentes tamaños de grupo en Abril
        ReservaEntity r1 = new ReservaEntity(0, 2, "r1@e.com", "2025-04-10T10:00", "U1", "1", 1); r1.setId(1L); // Grupo 1-2
        ReservaEntity r2 = new ReservaEntity(0, 5, "r2@e.com", "2025-04-15T14:00", "U2", "2", 2); r2.setId(2L); // Grupo 3-5
        ReservaEntity r3 = new ReservaEntity(0, 8, "r3@e.com", "2025-04-20T11:00", "U3", "3", 1); r3.setId(3L); // Grupo 6-10
        ReservaEntity r4 = new ReservaEntity(0, 12, "r4@e.com", "2025-04-25T16:00", "U4", "4", 3); r4.setId(4L);// Grupo 11-15
        ReservaEntity r5 = new ReservaEntity(0, 4, "r5@e.com", "2025-04-28T18:00", "U5", "5", 1); r5.setId(5L); // Grupo 3-5
        ReservaEntity r6 = new ReservaEntity(0, 1, "r6@e.com", "2025-05-01T10:00", "U6", "6", 1); r6.setId(6L); // Fuera de rango

        List<ReservaEntity> todasLasReservas = List.of(r1, r2, r3, r4, r5, r6);

        // Comprobantes simulados
        ComprobanteEntity c1 = new ComprobanteEntity(); c1.setIdreserva(1L); c1.setPreciofinal(20000f);
        ComprobanteEntity c2 = new ComprobanteEntity(); c2.setIdreserva(2L); c2.setPreciofinal(80000f);
        ComprobanteEntity c3 = new ComprobanteEntity(); c3.setIdreserva(3L); c3.setPreciofinal(100000f);
        ComprobanteEntity c4 = new ComprobanteEntity(); c4.setIdreserva(4L); c4.setPreciofinal(200000f);
        ComprobanteEntity c5 = new ComprobanteEntity(); c5.setIdreserva(5L); c5.setPreciofinal(50000f);

        // Configurar mocks
        when(reservaRepository.findAll()).thenReturn(todasLasReservas);
        when(comprobanteRepository.findByIdreserva(1L)).thenReturn(Optional.of(c1));
        when(comprobanteRepository.findByIdreserva(2L)).thenReturn(Optional.of(c2));
        when(comprobanteRepository.findByIdreserva(3L)).thenReturn(Optional.of(c3));
        when(comprobanteRepository.findByIdreserva(4L)).thenReturn(Optional.of(c4));
        when(comprobanteRepository.findByIdreserva(5L)).thenReturn(Optional.of(c5));

        // Act
        List<Object> reporte = comprobanteService.reporteporgrupo(mesInicio, mesFin);

        // Assert
        assertNotNull(reporte);
        assertEquals(1, reporte.size()); // Solo Abril

        assertTrue(reporte.get(0) instanceof List);
        @SuppressWarnings("unchecked")
        List<Long> totalesAbril = (List<Long>) reporte.get(0);
        assertEquals(4, totalesAbril.size()); // 4 grupos
        assertEquals(20000L, totalesAbril.get(0));  // Total Grupo 1-2 (r1)
        assertEquals(130000L, totalesAbril.get(1)); // Total Grupo 3-5 (r2 + r5)
        assertEquals(100000L, totalesAbril.get(2)); // Total Grupo 6-10 (r3)
        assertEquals(200000L, totalesAbril.get(3)); // Total Grupo 11-15 (r4)

        verify(reservaRepository, times(1)).findAll();
        // Verificamos que se buscaron los 5 comprobantes relevantes de Abril
        verify(comprobanteRepository, times(5)).findByIdreserva(anyLong());
        verify(comprobanteRepository, never()).findByIdreserva(6L); // r6 no se busca
    }


}