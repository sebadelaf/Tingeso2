package com.Tingeso.backend.Controller; // Asegúrate que el package sea correcto

import com.Tingeso.backend.DTO.ReporteDTO;
import com.Tingeso.backend.Entity.ComprobanteEntity;
import com.Tingeso.backend.Service.ComprobanteService;
import org.junit.jupiter.api.BeforeEach; // Añadido por si se necesita setup
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Imports necesarios para List y Arrays
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*; // Para any(), eq()
import static org.mockito.Mockito.*; // Para when(), verify()

@ExtendWith(MockitoExtension.class) // Habilita Mockito
class ComprobanteControllerTest {

    @Mock // Crea un mock del servicio
    private ComprobanteService comprobanteService;

    @InjectMocks // Crea una instancia del controller e inyecta el mock de arriba
    private ComprobanteController comprobanteController;

    // Variables para datos de prueba (opcional, se pueden definir en cada test)
    private ComprobanteEntity comprobanteEjemplo;
    private ReporteDTO reporteDTOEjemplo;

    @BeforeEach
    void setUp() {
        // Puedes inicializar objetos aquí si se usan en múltiples tests
        comprobanteEjemplo = new ComprobanteEntity(0,0,0, 1L, 50000, 59500, 25000, 9500);
        comprobanteEjemplo.setId(10L);

        reporteDTOEjemplo = new ReporteDTO("2025-01", "2025-03");
    }

    @Test
    void testCrearComprobante() {
        // Arrange
        long idReserva = 1L;
        // Configuramos el mock: cuando se llame a comprobanteService.crearcomprobante(1L)...
        when(comprobanteService.crearcomprobante(idReserva)).thenReturn(comprobanteEjemplo); // ...devolverá nuestro comprobante de ejemplo

        // Act
        // Llamamos DIRECTAMENTE al método del controller
        ComprobanteEntity resultado = comprobanteController.crearcomprobante(idReserva);

        // Assert
        assertNotNull(resultado); // Verificamos que no sea nulo
        assertEquals(comprobanteEjemplo.getId(), resultado.getId()); // Comparamos el resultado con lo esperado
        assertEquals(comprobanteEjemplo.getIdreserva(), resultado.getIdreserva());
        assertEquals(comprobanteEjemplo.getPreciofinal(), resultado.getPreciofinal());
        // ... puedes añadir más asserts si quieres ser más exhaustivo ...

        // Verificamos que el método del servicio fue llamado exactamente 1 vez con el ID correcto
        verify(comprobanteService, times(1)).crearcomprobante(idReserva);
    }

    @Test
    void testReporteTipoReserva() {
        // Arrange
        // Datos de ejemplo que simulan la respuesta del servicio
        List<Object> reporteSimulado = new ArrayList<>();
        reporteSimulado.add(List.of(100L, 200L, 50L));
        reporteSimulado.add(List.of(150L, 250L, 75L));

        // Configuramos el mock: cuando se llame al servicio con estos meses...
        when(comprobanteService.reporteportiporeserva(reporteDTOEjemplo.getMesinicio(), reporteDTOEjemplo.getMesfin()))
                .thenReturn(reporteSimulado); // ...devolverá nuestra lista simulada

        // Act
        // Llamamos DIRECTAMENTE al método del controller, pasándole el DTO
        List<Object> resultado = comprobanteController.reportetiporeserva(reporteDTOEjemplo);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size()); // Verificamos el tamaño de la lista externa
        assertTrue(resultado.get(0) instanceof List); // Verificamos que el elemento sea una lista
        assertEquals(3, ((List<?>)resultado.get(0)).size()); // Verificamos tamaño de lista interna
        assertEquals(100L, ((List<?>)resultado.get(0)).get(0)); // Verificamos un valor específico

        // Verificamos que el método del servicio fue llamado 1 vez con los argumentos correctos
        verify(comprobanteService, times(1)).reporteportiporeserva(
                eq(reporteDTOEjemplo.getMesinicio()),
                eq(reporteDTOEjemplo.getMesfin())
        );
    }

    @Test
    void testReportePorGrupos() {
        // Arrange
        ReporteDTO dto = new ReporteDTO("2025-02", "2025-02");
        List<Object> reporteSimulado = new ArrayList<>();
        reporteSimulado.add(List.of(1000L, 5000L, 8000L, 12000L)); // Datos simulados

        // Configuramos el mock
        when(comprobanteService.reporteporgrupo(dto.getMesinicio(), dto.getMesfin()))
                .thenReturn(reporteSimulado);

        // Act
        List<Object> resultado = comprobanteController.reporteporgrupos(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size()); // Tamaño esperado
        assertTrue(resultado.get(0) instanceof List);
        assertEquals(4, ((List<?>)resultado.get(0)).size()); // 4 grupos
        assertEquals(5000L, ((List<?>)resultado.get(0)).get(1)); // Valor específico

        // Verificamos llamada al servicio
        verify(comprobanteService, times(1)).reporteporgrupo(eq(dto.getMesinicio()), eq(dto.getMesfin()));
    }
}
