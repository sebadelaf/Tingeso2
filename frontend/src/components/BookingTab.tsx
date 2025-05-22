// Importamos React y hooks básicos: useState (para estado), useEffect (para efectos secundarios),
// useCallback, useMemo (para optimizaciones)
import React, { useState, useEffect, useCallback, useMemo } from 'react';
// Importamos componentes de react-big-calendar y sus tipos
import { Calendar, Views, SlotInfo } from 'react-big-calendar';
// Importamos funciones de date-fns que usaremos para manipular fechas
import { parse as parseDate, format as formatDate, addMinutes, getDay as getDayOfWeek, setHours, setMinutes,  isBefore } from 'date-fns';
import { useTheme } from '@mui/material/styles';
import { Typography } from '@mui/material'
// Importamos componentes de Material UI para la interfaz (layout, progreso, alertas)
import { Box, CircularProgress, Alert, Snackbar } from '@mui/material';
// Importamos nuestra configuración del localizador y formato de fecha API
import { localizer, API_DATE_FORMAT } from '../utils/calendarLocalizer';
// Importamos nuestras funciones para hablar con el backend
import { fetchReservas, createReserva } from '../services/reservaService';
// Importamos nuestras interfaces TypeScript
import { ReservaEntity, CalendarEvent, ReservaFormInput } from '../types/reserva';
// Importamos el componente del Modal (que crearemos en el siguiente paso)
import ReservationModal from './modal';
import ReservationDetailModal from './ReservationDetailModal'; // Importamos el nuevo modal
// Importamos los estilos CSS base de react-big-calendar
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { createComprobante } from '../services/comprobanteService';

// --- 2. Constantes y Funciones Helper ---
const holidays = ["01-01", "05-01", "09-18", "09-19", "12-25"];

const getReservaDuration = (tipo: 1 | 2 | 3): number => {
  switch (tipo) { case 1: return 30; case 2: return 35; case 3: return 40; default: return 30; }
};

const getOperatingHours = (date: Date): { start: number, end: number } => {
  const day = getDayOfWeek(date); const monthDay = formatDate(date, 'MM-dd'); const isHoliday = holidays.includes(monthDay);
  if (isHoliday || day === 0 || day === 6) { return { start: 10, end: 22 }; } else { return { start: 14, end: 22 }; }
};

const isSlotAvailable = (slotStart: Date, existingEvents: CalendarEvent[]): boolean => {
  const operatingHours = getOperatingHours(slotStart); const slotHour = slotStart.getHours();
  // Verifica si está fuera del horario operativo
  if (slotHour < operatingHours.start || slotHour >= operatingHours.end) return false;
   // Verifica si la fecha/hora ya pasó
  if (isBefore(slotStart, new Date())) return false;
   // Verifica superposición con eventos existentes
  const slotEnd = addMinutes(slotStart, 30); // Asume 30min para chequeo visual
  for (const event of existingEvents) { if (isBefore(slotStart, event.end) && isBefore(event.start, slotEnd)) return false; }
  return true;
};


// --- 3. Definición del Componente Funcional ---
const BookingTab: React.FC = () => {
  const theme = useTheme();
  const [detailModalOpen, setDetailModalOpen] = useState<boolean>(false);
  const [selectedEventForDetail, setSelectedEventForDetail] = useState<ReservaEntity | null>(null);
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState<boolean>(false);
  const [selectedSlot, setSelectedSlot] = useState<Date | null>(null);
  const [snackbar, setSnackbar] = useState<{ open: boolean, message: string, severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });
  // N U E V O: Estado para la fecha actual que muestra el calendario
  const [currentDate, setCurrentDate] = useState<Date>(new Date());

  // --- 5. Valores Memorizados ---
  // Ya no necesitamos defaultDate, usamos currentDate
  const minTime = useMemo(() => setHours(setMinutes(new Date(), 0), 9), []);  // 09:00 AM
  const maxTime = useMemo(() => setHours(setMinutes(new Date(), 0), 23), []); // 23:00 PM

  // --- 6. Efecto para Cargar Datos ---
  useEffect(() => {
    console.log("Efecto: Cargando reservas...");
    setLoading(true);
    fetchReservas()
      .then(data => {
        console.log("Reservas recibidas:", data);
        const calendarEvents = data.map((reserva): CalendarEvent | null => {
          try {
            const startDate = parseDate(reserva.fechahora, API_DATE_FORMAT, new Date());
            const duration = getReservaDuration(reserva.tiporeserva);
            const endDate = addMinutes(startDate, duration);
            if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
              console.error("Fecha inválida detectada para reserva:", reserva.id, reserva.fechahora);
              return null;
            }
            return { title: `Reservado`, start: startDate, end: endDate, resource: reserva };
          } catch (parseError) {
            console.error("Error parseando fecha:", reserva.fechahora, parseError);
            return null;
          }
        }).filter((event): event is CalendarEvent => event !== null);

        console.log("Eventos para calendario:", calendarEvents);
        setEvents(calendarEvents);
        setError(null);
      })
      .catch((err) => {
        console.error("Error en fetchReservas:", err);
        setError('No se pudieron cargar las reservas. Verifica que el backend esté corriendo.');
      })
      .finally(() => {
        setLoading(false);
        console.log("Carga finalizada.");
      });
  }, []); // Se ejecuta solo al montar

  // --- 7. Manejadores de Eventos ---
  const handleSelectSlot = useCallback((slotInfo: SlotInfo) => {
    console.log("Slot seleccionado:", slotInfo.start);
    const slotStart = slotInfo.start;
    if (!isSlotAvailable(slotStart, events)) {
      setSnackbar({ open: true, message: 'Este horario no está disponible.', severity: 'error' });
      return;
    }
    setSelectedSlot(slotStart);
    setModalOpen(true);
  }, [events]);

  const handleCloseModal = useCallback(() => {
    setModalOpen(false);
    setSelectedSlot(null);
  }, []);

  const handleFormSubmit = useCallback(async (formData: ReservaFormInput) => {
    console.log("HandleFormSubmit: Enviando formulario...", formData);
    let newReserva: ReservaEntity | null = null; // Variable para guardar la reserva creada
    let newEvent: CalendarEvent | null = null; // Variable para guardar el evento del calendario

    try {
      // --- 1. Crear la Reserva ---
      newReserva = await createReserva(formData);
      console.log("HandleFormSubmit: Reserva creada:", newReserva);

      if (!newReserva || !newReserva.id || !newReserva.fechahora) {
          // Si la respuesta del backend no es lo esperado, lanzar un error
           throw new Error("La respuesta de creación de reserva no contiene datos válidos (ID o fechahora).");
      }

      // --- 2. Parsear Fecha y Crear Evento del Calendario (HACERLO TEMPRANO) ---
      console.log(`HandleFormSubmit: Parseando fecha: ${newReserva.fechahora}`);
      const startDate = parseDate(newReserva.fechahora, API_DATE_FORMAT, new Date());
      const duration = getReservaDuration(newReserva.tiporeserva);
      const endDate = addMinutes(startDate, duration);

      // Validar fechas ANTES de continuar
      if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
          console.error(`HandleFormSubmit: Error - Fecha inválida parseada (${startDate} / ${endDate}) para fechahora: ${newReserva.fechahora}`);
          throw new Error(`Error interno al procesar la fecha de la reserva (${newReserva.fechahora}).`);
      }
      console.log("HandleFormSubmit: Fechas parseadas:", { startDate, endDate });

      // Crear el objeto del evento para el calendario
      newEvent = {
          title: `Reservado`,
          start: startDate,
          end: endDate,
          resource: newReserva
      };
      console.log("HandleFormSubmit: Evento de calendario creado:", newEvent);

      // --- 3. Intentar crear el Comprobante ---
      let comprobanteIntentado = false; // Para saber si se intentó
      let comprobanteExitoso = false; // Para saber si no hubo error
      try {
          console.log(`HandleFormSubmit: Intentando crear comprobante para reserva ID: ${newReserva.id}`);
          await createComprobante(newReserva.id);
          console.log(`HandleFormSubmit: Llamada a crear comprobante para reserva ${newReserva.id} realizada.`);
          comprobanteIntentado = true;
          comprobanteExitoso = true; // Asumimos éxito si no hay error
      } catch (comprobanteError: any) {
          comprobanteIntentado = true;
          comprobanteExitoso = false; // Hubo error
          console.error("HandleFormSubmit: Error secundario al intentar crear el comprobante:", comprobanteError);
          setSnackbar({ open: true, message: `Reserva creada, pero hubo un problema al generar el comprobante: ${comprobanteError.message}`, severity: 'error' });
      }


      // --- 4. Actualizar Estado y Cerrar Modal (SOLO SI TODO LO ANTERIOR FUE BIEN) ---
      // Verificamos que realmente tenemos un evento válido para añadir
      if (newEvent) {
         console.log("HandleFormSubmit: Actualizando estado de eventos...");
         setEvents(prevEvents => [...prevEvents, newEvent!]); // Usamos '!' porque ya comprobamos que no es null
         console.log("HandleFormSubmit: Cerrando modal...");
         handleCloseModal();

         // Mostrar mensaje de éxito solo si el comprobante no dio error (o no se intentó por ID inválido)
         if (comprobanteExitoso || !comprobanteIntentado) { // Si fue exitoso O si ni siquiera se intentó (porque el ID era malo antes)
            setSnackbar({ open: true, message: '¡Reserva creada con éxito! Revisa tu email para el comprobante.', severity: 'success' });
         }
      } else {
         // Esto no debería pasar si la validación de fecha funcionó, pero es una salvaguarda
         console.error("HandleFormSubmit: Error - No se pudo crear el evento del calendario.");
         setSnackbar({ open: true, message: 'Reserva creada, pero hubo un error al actualizar el calendario.', severity: 'error' });
         // Decidimos cerrar el modal igualmente, aunque el evento no se muestre
         handleCloseModal();
      }


    } catch (submitError: any) { // Error al crear la RESERVA principal o al parsear la fecha
      console.error("HandleFormSubmit: Error principal en el proceso:", submitError);
      // Mostramos el error específico que ocurrió
      setSnackbar({ open: true, message: `Error: ${submitError.message || 'No se pudo completar la operación.'}`, severity: 'error' });
      // NO cerramos el modal si falla la reserva principal o el parseo inicial
    }
  }, [handleCloseModal]); // Mantenemos la dependencia


    const handleNavigate = useCallback((newDate: Date) => {
        console.log("Navegando a:", newDate);
        setCurrentDate(newDate); // Actualiza el estado con la nueva fecha
        // Aquí iría la lógica futura para recargar eventos si fuera necesario
    }, []); // No tiene dependencias, se crea una vez


    // Dentro de BookingTab, junto a otros handlers

  // --- N U E V O S: Manejadores para el Modal de Detalles ---
  // Se llama cuando se hace clic en un evento existente en el calendario
  const handleSelectEvent = useCallback((calendarEvent: CalendarEvent) => {
    console.log("Evento seleccionado:", calendarEvent);
    // Extraemos la reserva original que guardamos en 'resource'
    const reservaSeleccionada = calendarEvent.resource as ReservaEntity | undefined;
    if (reservaSeleccionada) {
        setSelectedEventForDetail(reservaSeleccionada); // Guardamos la reserva para mostrar
        setDetailModalOpen(true); // Abrimos el modal de detalles
    } else {
        console.warn("El evento seleccionado no tiene datos de reserva asociados.");
        // Opcional: Mostrar un snackbar si falta 'resource'
        // setSnackbar({ open: true, message: 'No se pudieron cargar los detalles para este evento.', severity: 'error' });
    }
    }, []); // No depende de estados externos que cambien frecuentemente

    // Se llama para cerrar el modal de detalles
    const handleCloseDetailModal = useCallback(() => {
        setDetailModalOpen(false);
        setSelectedEventForDetail(null); // Limpiamos la reserva seleccionada
    }, []);
    // ---------------------------------------------------------


  const slotPropGetter = useCallback((date: Date) => {
    // La función usa 'theme' y 'events' que están fuera de ella
    if (!isSlotAvailable(date, events)) {
      return {
        style: {
          backgroundColor: theme.palette.action.disabledBackground, // Usa theme
          cursor: 'not-allowed',
          borderTop: `1px solid ${theme.palette.divider}` // Usa theme
        }
      };
    }
    return { style: { borderTop: `1px solid ${theme.palette.divider}` } }; // Usa theme
  }, [events]); // <-- Quitamos 'theme', dejamos solo [events]
  
  const eventPropGetter = useCallback((_event: CalendarEvent, _start: Date, _end: Date, _isSelected: boolean) => ({
    // La función usa 'theme' que está fuera de ella
    style: {
        backgroundColor:theme.palette.primary.main, // Usa theme
        borderRadius: '3px',
        opacity: 0.9,
        color: theme.palette.primary.contrastText, // Usa theme
        border: 'none',
        padding: '2px 4px',
        fontSize: '0.8em',
        cursor: 'pointer'
    }
  }), []);

  // --- 9. Renderizado del Componente ---
  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress /></Box>;
  }
  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  

  return (
    <Box sx={{ height: '80vh', p: 2 }}>
      <Typography
        variant="h6" // Tamaño del título (puedes usar 'subtitle1' si prefieres algo más pequeño)
        component="h2" // Etiqueta semántica HTML
        align="center" // Centrar el texto
        gutterBottom // Añade un margen inferior
        sx={{ mb: 2 }} // Margen inferior explícito si gutterBottom no es suficiente
      >
        Selecciona un horario disponible para iniciar tu reserva
      </Typography>
      <Calendar
        localizer={localizer}
        events={events}
        startAccessor="start"
        endAccessor="end"
        defaultView={Views.WEEK}
        views={[Views.WEEK]}
        selectable={true}
        onSelectSlot={handleSelectSlot}
        onSelectEvent={handleSelectEvent} 
        style={{ height: '100%' }}
        culture='es'
        messages={{
            next: "Sig.", previous: "Ant.", today: "Hoy", month: "Mes", week: "Semana",
            day: "Día", agenda: "Agenda", date: "Fecha", time: "Hora", event: "Evento",
            noEventsInRange: "No hay reservas en este rango.", showMore: total => `+ Ver más (${total})`
        }}
        step={30}
        timeslots={2}
        min={minTime}
        max={maxTime}
        slotPropGetter={slotPropGetter}
        eventPropGetter={eventPropGetter}
        // --- Cambios para Navegación ---
        date={currentDate}         // Usa el estado para la fecha actual
        onNavigate={handleNavigate}  // Llama a handleNavigate al navegar
        // defaultDate ya no se usa
        // -----------------------------
      />

      {selectedSlot && modalOpen && (
          <ReservationModal
            open={modalOpen}
            onClose={handleCloseModal}
            onSubmit={handleFormSubmit}
            initialDateTime={selectedSlot}
          />
      )}

      {selectedEventForDetail && (
         <ReservationDetailModal
            open={detailModalOpen}
            onClose={handleCloseDetailModal}
            reserva={selectedEventForDetail}
         />
      )}

       <Snackbar
            open={snackbar.open}
            autoHideDuration={6000}
            onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
            <Alert onClose={() => setSnackbar(prev => ({ ...prev, open: false }))} severity={snackbar.severity} sx={{ width: '100%' }}>
                {snackbar.message}
            </Alert>
        </Snackbar>
    </Box>
  );
};

export default BookingTab;