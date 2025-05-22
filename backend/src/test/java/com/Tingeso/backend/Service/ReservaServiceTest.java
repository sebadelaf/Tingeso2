package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*; // Importa when, verify, etc.

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    // Formato estándar para las fechas/horas en los tests
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @BeforeEach
    void setUp() {

    }

    @Test
    void crearReserva() {
        // Arrange: Preparamos los datos y mocks
        String fechaHora = "2025-07-15T15:00"; // Martes a las 3 PM (válido)
        int tipoReserva = 1; // Normal (30 min)
        int cantPersonas = 4;
        int cantCumple = 1;
        String nombre = "Usuario Test";
        String rut = "12345678-9";
        String email = "test@test.com";

        // Simulamos que no hay reservas existentes que se superpongan
        when(reservaRepository.findAll()).thenReturn(new ArrayList<>()); // Devuelve lista vacía

        // Simulamos que el método save funciona y devuelve la reserva guardada (podemos añadir un ID simulado)
        // Usamos any(ReservaEntity.class) porque no sabemos exactamente la instancia que se creará dentro
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reservaGuardada = invocation.getArgument(0);
            // reservaGuardada.setId(1L); // Opcional: Simular que se le asigna un ID
            return reservaGuardada;
        });

        // Act: Ejecutamos el método a probar
        ReservaEntity resultado = reservaService.crearReserva(fechaHora, tipoReserva, cantPersonas, cantCumple, nombre, rut, email);

        // Assert: Verificamos los resultados
        assertNotNull(resultado); // No debería ser nulo
        assertEquals(fechaHora, resultado.getFechahora());
        assertEquals(tipoReserva, resultado.getTiporeserva());
        assertEquals(cantPersonas, resultado.getCantidadpersonas());
        assertEquals(cantCumple, resultado.getCantidadcumple());
        assertEquals(nombre, resultado.getNombreusuario());
        assertEquals(rut, resultado.getRutusuario());
        assertEquals(email, resultado.getEmail());

        // Verificamos que el método save del repositorio fue llamado exactamente 1 vez
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
        // Verificamos que se llamó a findAll para chequear superposiciones
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void testCrearReserva_Falla_Superposicion() {
        // Arrange: Preparamos datos y una reserva existente que cause conflicto
        String fechaHoraNueva = "2025-07-15T16:00"; // Martes 4 PM (Normal, 30 min -> hasta 16:30)
        int tipoReservaNueva = 1;
        int cantPersonasNueva = 2;

        // Creamos una reserva existente simulada
        ReservaEntity existente = new ReservaEntity(0, 5, "otro@test.com", "2025-07-15T16:15", "Otro User", "98765432-1", 2); // Extendida (35 min -> hasta 16:50)
        List<ReservaEntity> listaExistentes = new ArrayList<>();
        listaExistentes.add(existente);

        // Simulamos que findAll devuelve la reserva existente
        when(reservaRepository.findAll()).thenReturn(listaExistentes);

        // Act & Assert: Esperamos que se lance una IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHoraNueva, tipoReservaNueva, cantPersonasNueva, 0, "User Sup", "11111111-1", "sup@test.com");
        });

        // Verificamos el mensaje de la excepción
        assertTrue(exception.getMessage().contains("La reserva se superpone con una existente"));

        // Verificamos que save NUNCA fue llamado
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        // Verificamos que findAll SÍ fue llamado
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void testCrearReserva_Falla_FueraDeHorario_Semana() {
        // Arrange
        String fechaHora = "2025-07-15T13:00"; // Martes a la 1 PM (antes de las 14:00)

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, 1, 2, 0, "User Temprano", "22222222-2", "temp@test.com");
        });
        assertTrue(exception.getMessage().contains("La hora de inicio está fuera del horario de atención"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        // findAll no se llama si falla antes la validación de horario
        verify(reservaRepository, never()).findAll();
    }

    @Test
    void testCrearReserva_Falla_ExcedeCierre() {
        // Arrange
        String fechaHora = "2025-07-15T21:45"; // Martes 21:45
        int tipoReserva = 3; // Premium (40 min) -> Terminaría 22:25

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, tipoReserva, 2, 0, "User Tarde", "33333333-3", "tarde@test.com");
        });
        assertTrue(exception.getMessage().contains("La reserva excede el horario de cierre"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        verify(reservaRepository, never()).findAll(); // No llega a chequear superposición
    }


    @Test
    void testObtenerReservasUsuario() {
        // Arrange
        String rut = "12345678-9";
        ReservaEntity res1 = new ReservaEntity(0, 2, "a@a.com", "2025-01-10T10:00", "User A", rut, 1);
        ReservaEntity res2 = new ReservaEntity(1, 4, "b@b.com", "2025-02-15T15:00", "User B", rut, 2);
        List<ReservaEntity> mockLista = List.of(res1, res2);
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(mockLista);

        // Act
        List<ReservaEntity> resultado = reservaService.obtenerReservasUsuario(rut);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(mockLista, resultado);
        verify(reservaRepository, times(1)).findAllByRutusuario(rut);
    }

    @Test
    void testCalcularPrecioInicial_Tipo1_Semana() {
        // Arrange
        long idReserva = 1L;
        // Martes, tipo 1, 3 personas
        ReservaEntity reserva = new ReservaEntity(0, 3, "c@c.com", "2025-07-15T18:00", "User C", "111", 1);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioEsperado = 15000 * 3; // Sin descuentos de día

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01); // Usar delta para floats
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    @Test
    void testCalcularPrecioInicial_Tipo2_Feriado() {
        // Arrange
        long idReserva = 2L;
        // 1 Mayo (Feriado), tipo 2, 2 personas
        ReservaEntity reserva = new ReservaEntity(0, 2, "d@d.com", "2025-05-01T11:00", "User D", "222", 2);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioBase = 20000 * 2;
        float precioEsperado = precioBase - (0.25f * precioBase); // 25% descuento feriado

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01);
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    @Test
    void testCalcularPrecioInicial_NotFound() {
        // Arrange
        long idReserva = 99L;
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.empty()); // Simula no encontrar la reserva

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.calcularprecioinicial(idReserva);
        });
        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(reservaRepository, times(1)).findById(idReserva);
    }


    @Test
    void testCalcularDescuentoGrupo() {
        // Arrange
        float precioBase = 100000f;

        // Act & Assert
        assertEquals(0f, reservaService.calcularDescuentoGrupo(1, precioBase), 0.01);
        assertEquals(0f, reservaService.calcularDescuentoGrupo(2, precioBase), 0.01);
        assertEquals(10000f, reservaService.calcularDescuentoGrupo(3, precioBase), 0.01); // 10%
        assertEquals(10000f, reservaService.calcularDescuentoGrupo(5, precioBase), 0.01); // 10%
        assertEquals(20000f, reservaService.calcularDescuentoGrupo(6, precioBase), 0.01); // 20%
        assertEquals(20000f, reservaService.calcularDescuentoGrupo(10, precioBase), 0.01); // 20%
        assertEquals(30000f, reservaService.calcularDescuentoGrupo(11, precioBase), 0.01); // 30%
        assertEquals(30000f, reservaService.calcularDescuentoGrupo(15, precioBase), 0.01); // 30%
        assertEquals(0f, reservaService.calcularDescuentoGrupo(16, precioBase), 0.01); // 0%
    }



    @Test
    void testDescuentoPorCumpleano() {
        // Arrange
        float precioInicial = 60000f;
        int cantPersonas = 4; // Grupo 3-5
        float tarifa = precioInicial / cantPersonas;

        // Act & Assert
        assertEquals(0, reservaService.descuentoporcumpleano(2, 30000, 1), 0.01);
    }

    @Test
    void testCrearReserva_Exito_FinDeSemana() {
        // Arrange
        String fechaHora = "2025-07-19T10:30"; // Sábado 10:30 AM (válido en finde)
        when(reservaRepository.findAll()).thenReturn(new ArrayList<>());
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ReservaEntity resultado = reservaService.crearReserva(fechaHora, 1, 2, 0, "User Finde", "444", "finde@test.com");

        // Assert
        assertNotNull(resultado);
        assertEquals(fechaHora, resultado.getFechahora());
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    void testCrearReserva_Falla_FueraDeHorario_FinDeSemana() {
        // Arrange
        String fechaHora = "2025-07-19T09:30"; // Sábado 9:30 AM (antes de las 10:00)

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, 1, 2, 0, "User Madruga", "555", "madruga@test.com");
        });
        assertTrue(exception.getMessage().contains("La hora de inicio está fuera del horario de atención"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    void testCrearReserva_Falla_FormatoFechaInvalido() {
        // Arrange
        String fechaHoraInvalida = "15-07-2025T15:00"; // Formato DD-MM-YYYY en lugar de YYYY-MM-DD

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHoraInvalida, 1, 2, 0, "User Malformato", "666", "bad@test.com");
        });
        assertTrue(exception.getMessage().contains("Formato de fecha y hora inválido"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    void testCrearReserva_Falla_TipoReservaInvalido() {
        // Arrange
        String fechaHora = "2025-07-15T15:00";
        int tipoReservaInvalido = 4; // Mayor que 3

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, tipoReservaInvalido, 2, 0, "User TipoX", "777", "tipox@test.com");
        });
        // La validación inicial debería atrapar esto
        assertTrue(exception.getMessage().contains("Tipo de reserva no puede ser negativo o mayor que 3"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    void testCrearReserva_Falla_CantidadPersonasInvalida() {
        // Arrange
        String fechaHora = "2025-07-15T15:00";
        int cantidadPersonasInvalida = 0; // Debe ser > 0

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, 1, cantidadPersonasInvalida, 0, "User Cero", "888", "cero@test.com");
        });
        assertTrue(exception.getMessage().contains("cantidad de personas debe ser mayor a 0"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    // --- Tests Adicionales para calcularprecioinicial ---

    @Test
    void testCalcularPrecioInicial_Tipo3_FinDeSemana() {
        // Arrange
        long idReserva = 3L;
        // Sábado, tipo 3, 5 personas
        ReservaEntity reserva = new ReservaEntity(0, 5, "e@e.com", "2025-07-19T14:00", "User E", "333", 3);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioBase = 25000 * 5; // 125000
        float precioEsperado = precioBase - (0.15f * precioBase); // 15% descuento fin de semana

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01);
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    @Test
    void testCalcularPrecioInicial_Tipo1_FeriadoYFinDeSemana() {
        // Arrange
        long idReserva = 4L;
        // Viernes 1 Nov (Asumiendo que fuera feriado y finde - irreal, pero prueba la lógica)
        // O usamos uno real: Sabado 19 de Sep (si Sep fuera Sabado) -> usemos Sabado 01-01-2028
        // Sábado 1 Enero 2028 (Feriado y Fin de Semana), tipo 1, 2 personas
        ReservaEntity reserva = new ReservaEntity(0, 2, "f@f.com", "2028-01-01T12:00", "User F", "444", 1);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioBase = 15000 * 2; // 30000
        // Aplica ambos descuentos secuencialmente según el código actual
        float precioConDctoFeriado = precioBase - (0.25f * precioBase); // 30000 * 0.75 = 22500
        float precioEsperado = precioConDctoFeriado - (0.15f * precioConDctoFeriado); // 22500 * 0.85 = 19125

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01);
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    // --- Tests para calcularDescuentoEspecial ---

    private ReservaEntity crearReservaMesActual(String rut, int dia, int hora) {
        LocalDateTime ahora = LocalDateTime.now();
        // Asegurarse de que la fecha sea válida para el mes actual
        LocalDateTime fechaReservaRaw = ahora.withDayOfMonth(1).withHour(hora).withMinute(0).withSecond(0).withNano(0).plusDays(dia - 1);
        // Ajustar si el día calculado excede los días del mes
        if (fechaReservaRaw.getMonthValue() != ahora.getMonthValue()) {
            fechaReservaRaw = ahora.withDayOfMonth(1).withHour(hora).withMinute(0).withSecond(0).withNano(0).plusMonths(1).minusDays(1); // Último día del mes
        }
        String fechaHora = fechaReservaRaw.format(formatter);
        return new ReservaEntity(0, 1, "test@test.com", fechaHora, "User", rut, 1);
    }

    @Test
    void testCalcularDescuentoEspecial_SinReservasPrevias() {
        // Arrange
        String rut = "101";
        float precioInicial = 10000f;
        // Simulamos que no hay reservas para este RUT
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(new ArrayList<>());

        // Act
        float descuento = reservaService.calcularDescuentoEspecial(rut, precioInicial);

        // Assert
        assertEquals(0f, descuento, 0.01); // Sin descuento (<= 1 reserva en mes)
        verify(reservaRepository, times(1)).findAllByRutusuario(rut);
    }

    @Test
    void testCalcularDescuentoEspecial_UnaReservaPrevia() {
        // Arrange
        String rut = "102";
        float precioInicial = 10000f;
        List<ReservaEntity> reservas = List.of(
                crearReservaMesActual(rut, 5, 15) // Una reserva este mes
        );
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(reservas);

        // Act
        float descuento = reservaService.calcularDescuentoEspecial(rut, precioInicial);

        // Assert
        assertEquals(0f, descuento, 0.01); // Sin descuento (<= 1 reserva en mes)
    }

    @Test
    void testCalcularDescuentoEspecial_TresReservasPrevias() {
        // Arrange
        String rut = "103";
        float precioInicial = 10000f;
        List<ReservaEntity> reservas = List.of(
                crearReservaMesActual(rut, 2, 16),
                crearReservaMesActual(rut, 8, 17),
                crearReservaMesActual(rut, 15, 18) // 3 reservas este mes
        );
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(reservas);

        // Act
        float descuento = reservaService.calcularDescuentoEspecial(rut, precioInicial);

        // Assert
        assertEquals(precioInicial * 0.1f, descuento, 0.01); // 10% descuento (2-4 reservas)
    }

    @Test
    void testCalcularDescuentoEspecial_CincoReservasPrevias() {
        // Arrange
        String rut = "104";
        float precioInicial = 10000f;
        List<ReservaEntity> reservas = List.of(
                crearReservaMesActual(rut, 1, 10), crearReservaMesActual(rut, 2, 11),
                crearReservaMesActual(rut, 3, 12), crearReservaMesActual(rut, 4, 13),
                crearReservaMesActual(rut, 5, 14) // 5 reservas este mes
        );
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(reservas);

        // Act
        float descuento = reservaService.calcularDescuentoEspecial(rut, precioInicial);

        // Assert
        assertEquals(precioInicial * 0.2f, descuento, 0.01); // 20% descuento (5-6 reservas)
    }

    @Test
    void testCalcularDescuentoEspecial_SieteReservasPrevias() {
        // Arrange
        String rut = "105";
        float precioInicial = 10000f;
        List<ReservaEntity> reservas = List.of(
                crearReservaMesActual(rut, 1, 10), crearReservaMesActual(rut, 2, 11),
                crearReservaMesActual(rut, 3, 12), crearReservaMesActual(rut, 4, 13),
                crearReservaMesActual(rut, 5, 14), crearReservaMesActual(rut, 6, 15),
                crearReservaMesActual(rut, 7, 16) // 7 reservas este mes
        );
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(reservas);

        // Act
        float descuento = reservaService.calcularDescuentoEspecial(rut, precioInicial);

        // Assert
        assertEquals(precioInicial * 0.3f, descuento, 0.01); // 30% descuento ( > 6 reservas)
    }


    // --- Tests Adicionales/Completos para descuentoporcumpleano ---

    @Test
    void testDescuentoPorCumpleano_CasosCompletos() {
        // Arrange
        float precioIniBajo = 30000f; int persBajo = 2; float tarifaBajo = precioIniBajo / persBajo; // 15000
        float precioIniMedio = 60000f; int persMedio = 4; float tarifaMedio = precioIniMedio / persMedio; // 15000
        float precioIniAlto = 120000f; int persAlto = 8; float tarifaAlto = precioIniAlto / persAlto; // 15000

        // Act & Assert
        // Grupo < 3 personas
        assertEquals(0f, reservaService.descuentoporcumpleano(persBajo, precioIniBajo, 0), 0.01, "Grupo < 3, 0 cumple");
        assertEquals(0f, reservaService.descuentoporcumpleano(persBajo, precioIniBajo, 1), 0.01, "Grupo < 3, 1 cumple");

        // Grupo 3-5 personas
        assertEquals(0f, reservaService.descuentoporcumpleano(persMedio, precioIniMedio, 0), 0.01, "Grupo 3-5, 0 cumple");
        assertEquals(tarifaMedio * 0.5f, reservaService.descuentoporcumpleano(persMedio, precioIniMedio, 1), 0.01, "Grupo 3-5, 1 cumple");
        assertEquals(tarifaMedio * 0.5f, reservaService.descuentoporcumpleano(persMedio, precioIniMedio, 3), 0.01, "Grupo 3-5, >1 cumple (solo 1 dcto)");

        // Grupo >= 6 personas
        assertEquals(0f, reservaService.descuentoporcumpleano(persAlto, precioIniAlto, 0), 0.01, "Grupo >=6, 0 cumple");
        // ¡OJO! La lógica actual multiplica por 2 FIJO, no por cantidadcumple > 1
        assertEquals(tarifaAlto * 0.5f * 2, reservaService.descuentoporcumpleano(persAlto, precioIniAlto, 1), 0.01, "Grupo >=6, 1 cumple (ERROR LOGICA ACTUAL?)");
        assertEquals(tarifaAlto * 0.5f * 2, reservaService.descuentoporcumpleano(persAlto, precioIniAlto, 2), 0.01, "Grupo >=6, 2 cumple (logica actual * 2)");
        assertEquals(tarifaAlto * 0.5f * 2, reservaService.descuentoporcumpleano(persAlto, precioIniAlto, 5), 0.01, "Grupo >=6, >2 cumple (logica actual * 2)");

        // Caso cantidadcumple negativo (aunque validado antes, probamos el método aislado)
        //assertEquals(0f, reservaService.descuentoporcumpleano(persAlto, precioIniAlto, -1), 0.01); // El if(cantidadcumple>0) lo maneja

    }
}