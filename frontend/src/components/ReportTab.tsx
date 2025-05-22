// src/components/ReportTab.tsx
import React, { useState, useMemo } from 'react';
import {
    Box, Button, TextField, Typography, Paper, CircularProgress, Alert,
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow
} from '@mui/material';
// Importamos las nuevas funciones de API y tipos
import { fetchReportePorTipo, fetchReportePorGrupo } from '../services/comprobanteService';
import { ReporteDTO,ReporteData } from '../types/reportes';
// Importamos funciones de date-fns para manejar meses
import { format as formatDate, parse as parseDate, eachMonthOfInterval, isValid } from 'date-fns';
import { es } from 'date-fns/locale/es';

// --- Componente Funcional ---
const ReportTab: React.FC = () => {
  // --- Estados ---
  const [mesInicio, setMesInicio] = useState<string>(''); // Formato "yyyy-MM"
  const [mesFin, setMesFin] = useState<string>('');       // Formato "yyyy-MM"
  const [reporteTipoData, setReporteTipoData] = useState<ReporteData | null>(null);
  const [reporteGrupoData, setReporteGrupoData] = useState<ReporteData | null>(null);
  const [monthHeaders, setMonthHeaders] = useState<string[]>([]); // Cabeceras de meses (ej. "Ene 2025")
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // --- Manejador para generar reportes ---
  const handleGenerateReport = async () => {
    setError(null); // Limpiar errores previos
    setReporteTipoData(null); // Limpiar datos previos
    setReporteGrupoData(null);
    setMonthHeaders([]);

    // Validación simple
    if (!mesInicio || !mesFin) {
      setError("Por favor, selecciona mes de inicio y fin.");
      return;
    }
    // Convertir "yyyy-MM" a objetos Date para comparar y generar cabeceras
    const startDate = parseDate(mesInicio, 'yyyy-MM', new Date());
    const endDate = parseDate(mesFin, 'yyyy-MM', new Date());

    if (!isValid(startDate) || !isValid(endDate)) {
         setError("Formato de fecha inválido. Usa yyyy-MM.");
         return;
    }

    if (endDate < startDate) {
      setError("El mes de fin no puede ser anterior al mes de inicio.");
      return;
    }

    setLoading(true);

    try {
      // Generar cabeceras de meses para las tablas
      const months = eachMonthOfInterval({ start: startDate, end: endDate });
      const headers = months.map(month => formatDate(month, 'MMM yyyy', { locale: es })); // ej. "abr 2025"
      setMonthHeaders(headers);
      console.log("Cabeceras de meses:", headers);

      // Llamar a las APIs en paralelo
      const filtros: ReporteDTO = { mesinicio: mesInicio, mesfin: mesFin };
      const [tipoData, grupoData] = await Promise.all([
        fetchReportePorTipo(filtros),
        fetchReportePorGrupo(filtros)
      ]);

       // Validar que la cantidad de datos recibidos coincida con los meses esperados
        if (tipoData.length !== headers.length || grupoData.length !== headers.length) {
            console.warn("Discrepancia entre meses esperados y datos recibidos", {
                headers: headers.length,
                tipoData: tipoData.length,
                grupoData: grupoData.length
            });
            // Podrías lanzar un error o intentar continuar con lo que se tenga
        }


      setReporteTipoData(tipoData);
      setReporteGrupoData(grupoData);

    } catch (err: any) {
      console.error("Error generando reportes:", err);
      setError(err.message || "Ocurrió un error al generar los reportes.");
    } finally {
      setLoading(false);
    }
  };

  // --- Lógica para calcular totales y formatear tablas (usando useMemo para optimizar) ---

   // Formateador de moneda (Ejemplo para CLP)
   const currencyFormatter = useMemo(() => new Intl.NumberFormat('es-CL', {
      style: 'currency',
      currency: 'CLP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
   }), []);

   // Dentro de ReportTab, antes del return

    const TIPO_RESERVA_LABELS = ["10 vueltas o 10 min", "15 vueltas o 15 min", "20 vueltas o 20 min"];
    const GRUPO_PERSONAS_LABELS = ["1-2", "3-5", "6-10", "11-15"];
  // --- Renderizado ---
  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>Generar Reportes</Typography>
      <Paper sx={{ p: 2, mb: 3, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <TextField
          label="Mes Inicio"
          type="month" // Input nativo para mes/año
          value={mesInicio}
          onChange={(e) => setMesInicio(e.target.value)}
          InputLabelProps={{ shrink: true }}
          sx={{ minWidth: '180px' }} // Ancho mínimo
        />
        <TextField
          label="Mes Fin"
          type="month"
          value={mesFin}
          onChange={(e) => setMesFin(e.target.value)}
          InputLabelProps={{ shrink: true }}
          sx={{ minWidth: '180px' }}
        />
        <Button
          variant="contained"
          onClick={handleGenerateReport}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : 'Generar'}
        </Button>
      </Paper>

      {/* Mensaje de Error */}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Indicador de Carga General */}
      {loading && !error && (
         <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
            <CircularProgress />
            <Typography sx={{ ml: 2 }}>Generando reportes...</Typography>
         </Box>
      )}


      {/* --- Sección Reporte por Tipo --- */}
      {!loading && reporteTipoData && monthHeaders.length > 0 && (
        <Box sx={{ mb: 4 }}>
           <Typography variant="h6" gutterBottom>Reporte por Tipo de Reserva</Typography>
           <TableContainer component={Paper} elevation={3}>
             <Table stickyHeader size="small" aria-label="reporte por tipo">
               <TableHead>
                 <TableRow>
                   <TableCell sx={{ fontWeight: 'bold' }}>Nro Vueltas / Tiempo</TableCell>
                   {/* Generamos una celda de cabecera por cada mes */}
                   {monthHeaders.map((header) => (
                     <TableCell key={header} align="right" sx={{ fontWeight: 'bold' }}>{header}</TableCell>
                   ))}
                   <TableCell align="right" sx={{ fontWeight: 'bold', borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>Total Tipo</TableCell>
                 </TableRow>
               </TableHead>
               <TableBody>
                 {/* Filas para cada tipo de reserva */}
                 {TIPO_RESERVA_LABELS.map((label, rowIndex) => {
                   // Calculamos el total para esta fila (tipo de reserva)
                   const rowTotal = reporteTipoData?.reduce((sum, monthData) => sum + (monthData?.[rowIndex] ?? 0), 0) ?? 0;
                   return (
                     <TableRow key={label}>
                       <TableCell component="th" scope="row">{label}</TableCell>
                       {/* Celdas de datos para cada mes */}
                       {monthHeaders.map((_, colIndex) => (
                         <TableCell key={`<span class="math-inline">\{label\}\-</span>{colIndex}`} align="right">
                           {/* Usamos el formateador de moneda. Accedemos al dato [colIndex][rowIndex] */}
                           {currencyFormatter.format(reporteTipoData?.[colIndex]?.[rowIndex] ?? 0)}
                         </TableCell>
                       ))}
                       {/* Celda del total de la fila */}
                       <TableCell align="right" sx={{ fontWeight: 'bold', borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>
                         {currencyFormatter.format(rowTotal)}
                       </TableCell>
                     </TableRow>
                   );
                 })}
                 {/* --- Fila de Totales por Mes --- */}
                 <TableRow sx={{ '& td, & th': { borderTop: '2px solid rgba(224, 224, 224, 1)', fontWeight: 'bold' } }}>
                   <TableCell component="th" scope="row">Total Mes</TableCell>
                   {/* Calculamos el total para cada columna (mes) */}
                   {monthHeaders.map((_, colIndex) => {
                     const colTotal = reporteTipoData?.[colIndex]?.reduce((sum, value) => sum + (value ?? 0), 0) ?? 0;
                     return (
                       <TableCell key={`total-col-${colIndex}`} align="right">
                         {currencyFormatter.format(colTotal)}
                       </TableCell>
                     );
                   })}
                   {/* Celda del Gran Total */}
                   <TableCell align="right" sx={{ borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>
                     {currencyFormatter.format(
                       reporteTipoData?.flat()?.reduce((sum, value) => sum + (value ?? 0), 0) ?? 0
                     )}
                   </TableCell>
                 </TableRow>
               </TableBody>
             </Table>
           </TableContainer>
        </Box>
      )}

      {/* --- Sección Reporte por Grupo --- */}
       {!loading && reporteGrupoData && monthHeaders.length > 0 && (
         <Box>
            <Typography variant="h6" gutterBottom>Reporte por Numero de personas</Typography>
            <TableContainer component={Paper} elevation={3}>
             <Table stickyHeader size="small" aria-label="reporte por grupo">
               <TableHead>
                 <TableRow>
                   <TableCell sx={{ fontWeight: 'bold' }}>Nro Personas</TableCell>
                   {/* Generamos una celda de cabecera por cada mes */}
                   {monthHeaders.map((header) => (
                     <TableCell key={header} align="right" sx={{ fontWeight: 'bold' }}>{header}</TableCell>
                   ))}
                   <TableCell align="right" sx={{ fontWeight: 'bold', borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>Total Grupo</TableCell>
                 </TableRow>
               </TableHead>
               <TableBody>
                 {/* Filas para cada grupo de personas */}
                 {GRUPO_PERSONAS_LABELS.map((label, rowIndex) => {
                   // Calculamos el total para esta fila (grupo)
                   const rowTotal = reporteGrupoData?.reduce((sum, monthData) => sum + (monthData?.[rowIndex] ?? 0), 0) ?? 0;
                   return (
                     <TableRow key={label}>
                       <TableCell component="th" scope="row">{label}</TableCell>
                       {/* Celdas de datos para cada mes */}
                       {monthHeaders.map((_, colIndex) => (
                         <TableCell key={`<span class="math-inline">\{label\}\-</span>{colIndex}`} align="right">
                           {/* Usamos el formateador de moneda. Accedemos al dato [colIndex][rowIndex] */}
                           {currencyFormatter.format(reporteGrupoData?.[colIndex]?.[rowIndex] ?? 0)}
                         </TableCell>
                       ))}
                       {/* Celda del total de la fila */}
                       <TableCell align="right" sx={{ fontWeight: 'bold', borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>
                         {currencyFormatter.format(rowTotal)}
                       </TableCell>
                     </TableRow>
                   );
                 })}
                 {/* --- Fila de Totales por Mes --- */}
                 <TableRow sx={{ '& td, & th': { borderTop: '2px solid rgba(224, 224, 224, 1)', fontWeight: 'bold' } }}>
                   <TableCell component="th" scope="row">Total Mes</TableCell>
                   {/* Calculamos el total para cada columna (mes) */}
                   {monthHeaders.map((_, colIndex) => {
                     const colTotal = reporteGrupoData?.[colIndex]?.reduce((sum, value) => sum + (value ?? 0), 0) ?? 0;
                     return (
                       <TableCell key={`total-col-${colIndex}`} align="right">
                         {currencyFormatter.format(colTotal)}
                       </TableCell>
                     );
                   })}
                   {/* Celda del Gran Total */}
                   <TableCell align="right" sx={{ borderLeft: '1px solid rgba(224, 224, 224, 1)' }}>
                     {currencyFormatter.format(
                       reporteGrupoData?.flat()?.reduce((sum, value) => sum + (value ?? 0), 0) ?? 0
                     )}
                   </TableCell>
                 </TableRow>
               </TableBody>
             </Table>
           </TableContainer>
         </Box>
      )}
    </Box>
  );
};

export default ReportTab;