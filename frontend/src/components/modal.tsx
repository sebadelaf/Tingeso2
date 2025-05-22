import React, { useState, useEffect } from 'react';
import { format as formatDate, parse as parseDate, isValid } from 'date-fns';
// Importamos los componentes de MUI que usaremos para el modal y el formulario
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button, Select, MenuItem, FormControl, InputLabel, FormHelperText, CircularProgress } from '@mui/material';
// Importamos el tipo para los datos del formulario y el formato de fecha API
import { ReservaFormInput } from '../types/reserva';
import { API_DATE_FORMAT } from '../utils/calendarLocalizer';
import Grid from '@mui/material/Grid'; 

// Props son las "propiedades" o datos que este componente recibe desde su padre (BookingTab).
interface ReservationModalProps {
    open: boolean; // ¿Debe estar abierto el modal? (true/false)
    onClose: () => void; // Función que se ejecuta cuando se pide cerrar (ej. botón Cancelar)
    onSubmit: (formData: ReservaFormInput) => Promise<void>; // Función que se ejecuta al enviar el formulario (es async)
    initialDateTime: Date | null; // La fecha/hora seleccionada en el calendario
}
const ReservationModal: React.FC<ReservationModalProps> = ({ open, onClose, onSubmit, initialDateTime }) => {

    // --- 4. Estado Interno del Modal ---
    // Guarda los valores actuales de cada campo del formulario.
    const [formData, setFormData] = useState<ReservaFormInput>({
      fechahora: '', tiporeserva: 1, cantidadpersonas: 1, cantidadcumple: 0, nombreusuario: '', rutusuario: '', email: '',
    });
    // Indica si estamos en proceso de enviar el formulario (para mostrar spinner/deshabilitar botón)
    const [isSubmitting, setIsSubmitting] = useState(false);
    // Guarda los mensajes de error de validación para cada campo.
    const [errors, setErrors] = useState<Partial<Record<keyof ReservaFormInput, string>>>({});
  
    // --- 5. Efecto para Rellenar Fecha/Hora Inicial y Limpiar Errores ---
    useEffect(() => {
      // Si nos pasan una fecha inicial válida Y el modal se está abriendo...
      if (initialDateTime && isValid(initialDateTime) && open) {
        // ...actualizamos el campo 'fechahora' en nuestro estado 'formData'.
        // Lo formateamos al formato que espera el backend (API_DATE_FORMAT).
        setFormData(prev => ({ ...prev, fechahora: formatDate(initialDateTime, API_DATE_FORMAT) }));
      }
      // Siempre que el modal se abra, limpiamos los errores de validación anteriores.
      if (open) { setErrors({}); }
    }, [initialDateTime, open]); // Se ejecuta si cambia initialDateTime o si cambia open

    //cambios en los inputs

    // Se llama CADA VEZ que el usuario escribe o selecciona algo en un campo del formulario.
    const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | (Event & { target: { name: string; value: unknown } }) ) => {
    const target = event.target as HTMLInputElement | HTMLSelectElement; // Obtenemos el input que cambió
    const { name, value } = target; // Sacamos su 'name' (ej. "nombreusuario") y su 'value' (lo que escribió el usuario)
    let processedValue: string | number = value; // Guardamos el valor (puede ser string o number)

    // Si es un campo numérico, lo convertimos a número
    if (name === 'cantidadpersonas' || name === 'cantidadcumple' || name === 'tiporeserva') {
      processedValue = value === '' ? '' : Number(value); // Si está vacío es '', si no, lo convierte
    }

    // Actualizamos el estado 'formData'
    setFormData(prevFormData => ({
        ...prevFormData, // Mantenemos los valores anteriores...
        [name]: processedValue, // ...y actualizamos SÓLO el campo que cambió ('name') con el nuevo valor ('processedValue')
        }));

        // Si había un error para este campo, lo borramos al modificarlo
        if (errors[name as keyof ReservaFormInput]) {
        setErrors(prevErrors => ({ ...prevErrors, [name]: undefined }));
        }
    };

    //errores
    // Chequea si los datos ingresados son válidos ANTES de enviarlos.
  const validateForm = (): boolean => {
        const newErrors: Partial<Record<keyof ReservaFormInput, string>> = {}; // Objeto para guardar errores encontrados

        if (!formData.nombreusuario.trim()) newErrors.nombreusuario = 'Nombre es requerido'; // trim() quita espacios en blanco
        if (!formData.rutusuario.trim()) newErrors.rutusuario = 'RUT es requerido';
        // Aquí podrías añadir validación de formato RUT si quieres
        if (!formData.email.trim()) { newErrors.email = 'Email es requerido'; }
        else if (!/\S+@\S+\.\S+/.test(formData.email)) { newErrors.email = 'Formato de email inválido'; } // Chequeo básico de email

        const cantPersonasNum = Number(formData.cantidadpersonas); // Convertimos a número para validar
        if (isNaN(cantPersonasNum) || cantPersonasNum <= 0) newErrors.cantidadpersonas = 'Cantidad inválida (mínimo 1)';

        const cantCumpleNum = Number(formData.cantidadcumple);
        if (isNaN(cantCumpleNum) || cantCumpleNum < 0) newErrors.cantidadcumple = 'Cantidad inválida (mínimo 0)';
        else if (!newErrors.cantidadpersonas && cantCumpleNum > cantPersonasNum) { // Solo si cantPersonas es válido
            newErrors.cantidadcumple = 'Nº Cumpleaños no puede exceder Nº Personas';
        }

        if (!formData.fechahora) newErrors.fechahora = 'Fecha requerida'; // No debería pasar si viene del calendario

        setErrors(newErrors); // Actualizamos el estado con los errores encontrados
        return Object.keys(newErrors).length === 0; // Es válido si el objeto de errores está vacío
    }

    // Se llama cuando el usuario hace clic en el botón "Confirmar Reserva".
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault(); // Previene el envío normal del formulario HTML
    if (!validateForm()) return; // Si la validación falla, no hacemos nada más

    setIsSubmitting(true); // Indicamos que estamos enviando
    setErrors({}); // Limpiamos errores viejos antes de intentar enviar
    try {
      // Aseguramos que los tipos numéricos sean correctos antes de enviar
      const dataToSend: ReservaFormInput = {
          ...formData,
          tiporeserva: Number(formData.tiporeserva) as 1 | 2 | 3,
          cantidadpersonas: Number(formData.cantidadpersonas),
          cantidadcumple: Number(formData.cantidadcumple),
      };
      await onSubmit(dataToSend); // Llamamos a la función onSubmit que nos pasó BookingTab
                                  // Esperamos a que termine (puede tomar tiempo por la llamada a la API)
    } catch (error) {
      // Si onSubmit lanzó un error (porque createReserva falló),
      // el error ya se muestra en el Snackbar de BookingTab.
      console.error("Error en handleSubmit del modal:", error);
    } finally {
      // Se ejecuta siempre, haya habido éxito o error.
      setIsSubmitting(false); // Dejamos de estar en estado "enviando"
    }
  };
  return (
    // Componente Dialog de MUI, controla la visibilidad con 'open' y el cierre con 'onClose'
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle textAlign={"center"}>Crear Nueva Reserva</DialogTitle>
      <form onSubmit={handleSubmit}> {/* Envolvemos en <form> para usar onSubmit */}
        <DialogContent> {/* Cuerpo del modal */}
          <Grid container spacing={2}> {/* Grid para layout */}
            {/* Campo Fecha/Hora (solo lectura) */}
            <Grid size={5}>
                 <TextField label="Fecha y Hora Seleccionada"
                    value={formData.fechahora ? formatDate(parseDate(formData.fechahora, API_DATE_FORMAT, new Date()), 'dd/MM/yyyy HH:mm') : ''}
                    InputProps={{ readOnly: true }} fullWidth margin="dense" error={!!errors.fechahora} helperText={errors.fechahora} />
            </Grid>
            {/* Campos Nombre y RUT (mitad y mitad en pantallas medianas/grandes) */}
            <Grid size={5}>
              <TextField label="Nombre Usuario" name="nombreusuario" value={formData.nombreusuario} onChange={handleChange} fullWidth required margin="dense" error={!!errors.nombreusuario} helperText={errors.nombreusuario} disabled={isSubmitting} />
            </Grid>
            <Grid size={5}>
              <TextField label="RUT Usuario" name="rutusuario" value={formData.rutusuario} onChange={handleChange} fullWidth required margin="dense" error={!!errors.rutusuario} helperText={errors.rutusuario} disabled={isSubmitting} />
            </Grid>
            {/* Campo Email (ancho completo) */}
            <Grid size={5}>
              <TextField label="Email" name="email" type="email" value={formData.email} onChange={handleChange} fullWidth required margin="dense" error={!!errors.email} helperText={errors.email} disabled={isSubmitting} />
            </Grid>
            {/* Fila para Tipo Reserva, Cantidad Personas, Cantidad Cumpleaños (un tercio cada uno) */}
            <Grid size={5}>
               <FormControl fullWidth margin="dense" required error={!!errors.tiporeserva}>
                    <InputLabel id="tipo-reserva-label">Tipo Reserva</InputLabel>
                    <Select labelId="tipo-reserva-label" label="Tipo Reserva" name="tiporeserva" value={formData.tiporeserva} onChange={handleChange as any} disabled={isSubmitting}>
                        <MenuItem value={1}>10 vueltas o 10 minutos</MenuItem>
                        <MenuItem value={2}>15 vueltas o 15 minutos</MenuItem>
                        <MenuItem value={3}>20 vueltas o 20 minutos</MenuItem>
                    </Select>
                    {errors.tiporeserva && <FormHelperText>{errors.tiporeserva}</FormHelperText>}
                </FormControl>
            </Grid>
            <Grid size={5}>
              <TextField 
              label="Cantidad Personas" name="cantidadpersonas" type="number" value={formData.cantidadpersonas} onChange={handleChange} 
              fullWidth required margin="dense"   InputProps={{
                inputProps: {
                    min: 1,
                    max: 15 // <-- AÑADIDO: Límite máximo
                }
            }} error={!!errors.cantidadpersonas} helperText={errors.cantidadpersonas ?? "desde 1 hasta 15 personas"} disabled={isSubmitting} />
            </Grid>
             <Grid size={5}>
              <TextField label="Nº Cumpleaños" name="cantidadcumple" type="number" value={formData.cantidadcumple} onChange={handleChange} fullWidth required margin="dense" InputProps={{ inputProps: { min: 0 } }} error={!!errors.cantidadcumple} helperText={errors.cantidadcumple ?? "Personas del grupo que estan de cumpleaños"} disabled={isSubmitting} />
             </Grid>
             {/* Mostramos un spinner si se está enviando */}
              {isSubmitting && (
                <Grid>
                    <CircularProgress size={24} />
                </Grid>
              )}
          </Grid>
        </DialogContent>
        <DialogActions> {/* Pie del modal para los botones */}
          {/* Botón Cancelar: llama a onClose, deshabilitado si se está enviando */}
          <Button onClick={onClose} color="secondary" disabled={isSubmitting}> Cancelar </Button>
          {/* Botón Submit: tipo 'submit' para activar el onSubmit del form, deshabilitado si se está enviando */}
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting}>
            {isSubmitting ? 'Reservando...' : 'Confirmar Reserva'} {/* Texto dinámico */}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};  
export default ReservationModal;