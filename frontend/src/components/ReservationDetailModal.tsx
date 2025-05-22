// src/components/ReservationDetailModal.tsx
import React from 'react';
import {
    Dialog, DialogTitle, DialogContent, DialogActions, Button,
     Divider, List, ListItem, ListItemText
} from '@mui/material';
import { format as formatDate, parse as parseDate } from 'date-fns';
import { es } from 'date-fns/locale/es';
// Asegúrate que la ruta y nombre sean correctos
import { ReservaEntity } from '../types/reserva';
import { API_DATE_FORMAT } from '../utils/calendarLocalizer';

// Props que espera este componente
interface ReservationDetailModalProps {
  open: boolean;                      // Para saber si debe mostrarse
  onClose: () => void;                // Función para cerrarlo
  reserva: ReservaEntity | null;     // La reserva seleccionada (o null si no hay)
}

// Helper para obtener descripción del tipo de reserva
const getTipoReservaDesc = (tipo?: 1 | 2 | 3): string => {
    switch (tipo) {
        case 1: return "Normal (10v / 10min)";
        case 2: return "Extendida (15v / 15min)";
        case 3: return "Premium (20v / 20min)";
        default: return "Desconocido";
    }
}

const ReservationDetailModal: React.FC<ReservationDetailModalProps> = ({ open, onClose, reserva }) => {

  // Si no hay reserva, no renderizamos nada (o un estado vacío)
  if (!reserva) {
    return null;
  }

  // Formatear la fecha y hora para mostrarla legiblemente
  let fechaHoraFormateada = "Fecha inválida";
  try {
      const fecha = parseDate(reserva.fechahora, API_DATE_FORMAT, new Date());
      // Formato ejemplo: Sábado, 12 de abril de 2025, 15:30 hrs
      fechaHoraFormateada = formatDate(fecha, "EEEE, d 'de' MMMM 'de' yyyy, HH:mm 'hrs'", { locale: es });
  } catch (e) {
      console.error("Error formateando fecha para detalle:", e);
      fechaHoraFormateada = reserva.fechahora; // Mostrar el string original si falla el parseo
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ textAlign: 'center', pb: 1 }}>
         Detalle de la Reserva #{reserva.id}
      </DialogTitle>
      <Divider />
      <DialogContent sx={{ pt: 2 }}>
        {/* Usamos List para una presentación ordenada */}
        <List dense disablePadding>
            <ListItem disableGutters>
                <ListItemText primary="Fecha y Hora:" secondary={fechaHoraFormateada} />
            </ListItem>
            <ListItem disableGutters>
                <ListItemText primary="Tipo de Reserva:" secondary={getTipoReservaDesc(reserva.tiporeserva)} />
            </ListItem>
             <ListItem disableGutters>
                <ListItemText primary="Cantidad de Personas:" secondary={reserva.cantidadpersonas} />
            </ListItem>
            {/* Solo mostramos cumpleaños si es mayor a 0 */}
            {reserva.cantidadcumple > 0 && (
                 <ListItem disableGutters>
                    <ListItemText primary="Personas de Cumpleaños:" secondary={reserva.cantidadcumple} />
                </ListItem>
            )}
            <Divider sx={{ my: 1 }} /> {/* Separador */}
            <ListItem disableGutters>
                <ListItemText primary="Nombre Cliente:" secondary={reserva.nombreusuario} />
            </ListItem>
             <ListItem disableGutters>
                <ListItemText primary="RUT Cliente:" secondary={reserva.rutusuario} />
            </ListItem>
             <ListItem disableGutters>
                <ListItemText primary="Email Cliente:" secondary={reserva.email} />
            </ListItem>
        </List>

      </DialogContent>
      <Divider />
      <DialogActions sx={{ justifyContent: 'center', p: 1.5 }}>
        <Button onClick={onClose} variant="outlined" color="secondary">
          Cerrar
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ReservationDetailModal;