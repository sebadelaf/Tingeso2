// Esta interface describe CÓMO ES una reserva que viene de tu backend.
// Los nombres de las propiedades (id, fechahora, etc.) deben coincidir EXACTAMENTE
// con los nombres de los campos en tu clase Java 'ReservaEntity'.
export interface ReservaEntity {
    id: number;           // El número único de la reserva
    fechahora: string;    // La fecha y hora como texto (en formato API_DATE_FORMAT)
    tiporeserva: 1 | 2 | 3; // Solo puede ser 1, 2 o 3
    cantidadpersonas: number;
    cantidadcumple: number;
    nombreusuario: string;
    rutusuario: string;
    email: string;
  }

// Esta interface describe los datos que necesitamos para CREAR una reserva.
// Es igual a ReservaEntity, pero SIN el 'id' (porque el ID lo genera el backend).
// 'Omit' es una utilidad de TypeScript que crea un nuevo tipo quitando ciertas propiedades.
export type ReservaFormInput = Omit<ReservaEntity, 'id'>;

// Esta interface describe cómo necesita 'react-big-calendar' los datos para MOSTRARLOS.
// Necesita un título, una fecha de inicio (start) y una fecha de fin (end).
export interface CalendarEvent {
    title: string;          // El texto que se verá en el calendario (ej. "Reserva (5p)")
    start: Date;            // La fecha/hora de inicio COMO OBJETO Date de JavaScript
    end: Date;              // La fecha/hora de fin COMO OBJETO Date de JavaScript
    resource?: ReservaEntity; // Opcional: Guardamos aquí la reserva original por si necesitamos más datos al hacerle clic
}