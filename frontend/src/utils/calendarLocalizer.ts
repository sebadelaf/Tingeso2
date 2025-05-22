// para que sepa como manejar las fechas
// src/utils/calendarLocalizer.ts
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { dateFnsLocalizer } from 'react-big-calendar';
import { es } from 'date-fns/locale/es'; // <-- LÍNEA CORRECTA; // Importamos el idioma español de date-fns

const locales = {
  'es': es, // Definimos 'es' (español) como un locale disponible
};

// Creamos y exportamos el 'localizer' que usará react-big-calendar
export const localizer = dateFnsLocalizer({
  format, // Le decimos que use la función 'format' de date-fns para mostrar fechas
  parse,  // Le decimos que use la función 'parse' de date-fns para entender fechas
  startOfWeek: (date:Date) => startOfWeek(date, { locale: es }), // Le decimos que la semana empieza en Lunes para español
  getDay, // Le decimos que use la función 'getDay' de date-fns para saber el día de la semana
  locales, // Le pasamos los locales que definimos (solo 'es')
});

// Exportamos también el formato de fecha EXACTO que usa nuestro backend
// Esto es MUY importante para evitar errores al enviar/recibir fechas.
// Tiene que coincidir con el formato en tu código Java: "yyyy-MM-dd'T'HH:mm"
export const API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";