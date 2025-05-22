package com.Tingeso.backend.Controller; // Asegúrate que el package sea correcto

import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ReservaRepository; // Necesario para el mock
import com.Tingeso.backend.Service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*; // Para any(), eq()
import static org.mockito.Mockito.*; // Para when(), verify()

@ExtendWith(MockitoExtension.class) // Habilita Mockito
class ReservaControllerTest {

    @Mock // Mock del Servicio
    private ReservaService reservaService;

    @Mock // Mock del Repositorio (porque el controller lo usa para /todas)
    private ReservaRepository reservaRepository;

    @InjectMocks // Instancia real del Controller con mocks inyectados
    private ReservaController reservaController;

    private ReservaEntity reservaEntrada;
    private ReservaEntity reservaSalida;
    private List<ReservaEntity> listaReservasEjemplo;

    @BeforeEach
    void setUp() {
        // Datos de entrada simulando lo que llegaría en el @RequestBody
        // Usando el constructor de ReservaEntity(cantCumple, cantPersonas, email, fechaHora, nombre, rut, tipoReserva)
        reservaEntrada = new ReservaEntity(
                1, // cantidadcumple
                2, // cantidadpersonas
                "test@mail.com", // email
                "2025-10-10T15:00", // fechahora
                "Test User", // nombreusuario
                "123-4", // rutusuario
                1 // tiporeserva
        );
        // No establecemos ID para la entrada, ya que viene del request body

        // Datos de salida simulando lo que devolvería el servicio
        reservaSalida = new ReservaEntity(
                reservaEntrada.getCantidadcumple(),
                reservaEntrada.getCantidadpersonas(),
                reservaEntrada.getEmail(),
                reservaEntrada.getFechahora(),
                reservaEntrada.getNombreusuario(),
                reservaEntrada.getRutusuario(),
                reservaEntrada.getTiporeserva()
        );
        reservaSalida.setId(5L); // ID asignado por el servicio/BD

        // Lista de ejemplo para otros métodos
        listaReservasEjemplo = List.of(
                new ReservaEntity(0, 1, "a@a.com", "f1", "N1", "123-4", 1),
                new ReservaEntity(0, 2, "b@b.com", "f2", "N2", "123-4", 2)
        );
        // Asignar IDs si findById los necesitara en otros tests
        listaReservasEjemplo.get(0).setId(1L);
        listaReservasEjemplo.get(1).setId(2L);
    }

    @Test
    void testCrearReserva_LlamaAlServicioCorrectamente() {
        // Arrange
        // Configuramos el mock del servicio para que devuelva reservaSalida cuando se le llame
        // con los parámetros extraídos de reservaEntrada.
        when(reservaService.crearReserva(
                eq(reservaEntrada.getFechahora()),
                eq(reservaEntrada.getTiporeserva()),
                eq(reservaEntrada.getCantidadpersonas()),
                eq(reservaEntrada.getCantidadcumple()),
                eq(reservaEntrada.getNombreusuario()),
                eq(reservaEntrada.getRutusuario()),
                eq(reservaEntrada.getEmail())
        )).thenReturn(reservaSalida);

        // Act
        // Llamamos DIRECTAMENTE al método del controller, pasándole la entidad de entrada
        ReservaEntity resultado = reservaController.crearReserva(reservaEntrada);

        // Assert
        assertNotNull(resultado);
        assertEquals(reservaSalida.getId(), resultado.getId()); // Verificamos que devuelve la reserva con ID
        assertEquals(reservaSalida.getNombreusuario(), resultado.getNombreusuario());
        assertEquals(reservaSalida.getEmail(), resultado.getEmail());

        // Verificamos que el método del servicio fue llamado exactamente 1 vez con los parámetros correctos
        verify(reservaService, times(1)).crearReserva(
                eq(reservaEntrada.getFechahora()),
                eq(reservaEntrada.getTiporeserva()),
                eq(reservaEntrada.getCantidadpersonas()),
                eq(reservaEntrada.getCantidadcumple()),
                eq(reservaEntrada.getNombreusuario()),
                eq(reservaEntrada.getRutusuario()),
                eq(reservaEntrada.getEmail())
        );
        // Verificamos que el repositorio no fue llamado directamente por este método del controller
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void testObtenerReservas_LlamaAlServicioCorrectamente() {
        // Arrange
        String rutUsuario = "123-4";
        // Configuramos el mock para que devuelva nuestra lista de ejemplo
        when(reservaService.obtenerReservasUsuario(rutUsuario)).thenReturn(listaReservasEjemplo);

        // Act
        // Llamamos directamente al método del controller
        List<ReservaEntity> resultado = reservaController.obtenerReservas(rutUsuario);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size()); // Verificamos el tamaño de la lista devuelta
        assertEquals("N1", resultado.get(0).getNombreusuario());
        assertEquals("N2", resultado.get(1).getNombreusuario());

        // Verificamos que el método del servicio fue llamado 1 vez con el RUT correcto
        verify(reservaService, times(1)).obtenerReservasUsuario(eq(rutUsuario));
        // Verificamos que el repositorio no fue llamado directamente por este método del controller
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void testCalcularPrecioInicial_LlamaAlServicioCorrectamente() {
        // Arrange
        Long idReserva = 50L;
        float precioCalculadoMock = 75000.0f;
        // Configuramos el mock
        when(reservaService.calcularprecioinicial(idReserva)).thenReturn(precioCalculadoMock);

        // Act
        // Llamamos directamente al método del controller
        float resultado = reservaController.calcularPrecioInicial(idReserva);

        // Assert
        assertEquals(precioCalculadoMock, resultado, 0.01); // Verificamos el valor devuelto

        // Verificamos que el método del servicio fue llamado 1 vez con el ID correcto
        verify(reservaService, times(1)).calcularprecioinicial(eq(idReserva));
        // Verificamos que el repositorio no fue llamado directamente por este método del controller
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void testObtenerTodasReservas_LlamaAlRepositorioCorrectamente() {
        // Arrange
        // Este método del controller llama directamente al REPOSITORIO
        when(reservaRepository.findAll()).thenReturn(listaReservasEjemplo);

        // Act
        List<ReservaEntity> resultado = reservaController.obtenerreservas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("N1", resultado.get(0).getNombreusuario());

        // Verificamos que el método del REPOSITORIO fue llamado 1 vez
        verify(reservaRepository, times(1)).findAll();
        // Verificamos que NINGÚN método del SERVICIO fue llamado para esta acción
        verifyNoInteractions(reservaService);
    }
}