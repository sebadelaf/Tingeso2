// src/services/api.ts
import axios from 'axios';
import { ReporteDTO, ReporteData } from '../types/reportes';
// --- IMPORTANTE ---
//const API_BASE_URL = 'http://52.191.57.93:8080';
const API_BASE_URL = 'http://127.0.0.1:57049'; // por el momento es dinamico
// ---------------------

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// --- *** NUEVA FUNCIÓN para CREAR el comprobante *** ---
// Recibe el ID de la reserva recién creada.
// No esperamos que devuelva datos críticos por ahora (Promise<void>), pero maneja errores.
export const createComprobante = async (reservaId: number): Promise<void> => {
    // Verifica que el ID sea un número válido mayor que 0
    if (!reservaId || reservaId <= 0) {
        console.error('Error: ID de reserva inválido para crear comprobante:', reservaId);
        throw new Error('ID de reserva inválido.'); // Lanza un error si el ID no es válido
    }
    try {
        console.log(`Intentando crear comprobante para reserva ID: ${reservaId}`);
        // Hacemos una petición POST al endpoint '/comprobantes/crear/{idreserva}'
        // Pasamos el ID en la URL usando template literals (` `` `).
        // No enviamos cuerpo (tercer argumento de post es undefined o null)
        await apiClient.post(`/comprobantes/crear/${reservaId}`);
        console.log(`Comprobante creado (o creación iniciada) para reserva ID: ${reservaId}`);
        // Si el backend devolviera el comprobante, podríamos hacer:
        // const response = await apiClient.post<ComprobanteEntity>(`/comprobantes/crear/${reservaId}`);
        // return response.data;
    } catch (error) {
        console.error(`Error al crear el comprobante para reserva ID ${reservaId}:`, error);
         // Intentamos dar un mensaje más útil si el backend lo envió
        if (axios.isAxiosError(error) && error.response?.data?.message) {
            throw new Error(`Error al crear comprobante: ${error.response.data.message}`);
        }
        // Lanzamos un error genérico si no hay mensaje específico
        throw new Error('No se pudo crear el comprobante asociado a la reserva.');
    }
};

export const fetchReportePorTipo = async (filtros: ReporteDTO): Promise<ReporteData> => {
    // Validación simple de entrada
     if (!filtros.mesinicio || !filtros.mesfin) {
         throw new Error("Mes de inicio y fin son requeridos para el reporte por tipo.");
     }
      try {
          console.log("(comprobanteService) Fetching reporte por tipo:", filtros);
          // Hacemos POST a /comprobantes/reportetipo enviando los filtros en el cuerpo
          const response = await apiClient.post<ReporteData>('/reportes/tipo-reserva', filtros);
          console.log("(comprobanteService) Reporte por tipo recibido:", response.data);
          return response.data || []; // Devuelve los datos o un array vacío si no viene nada
      } catch (error) {
          console.error('(comprobanteService) Error fetching reporte por tipo:', error);
           if (axios.isAxiosError(error) && error.response?.data?.message) {
              throw new Error(`Error al obtener reporte por tipo: ${error.response.data.message}`);
          }
          throw new Error('No se pudo obtener el reporte por tipo de reserva.');
      }
  };
  
  // --- *** NUEVA FUNCIÓN: Reporte por Grupos *** ---
  export const fetchReportePorGrupo = async (filtros: ReporteDTO): Promise<ReporteData> => {
      // Validación simple de entrada
     if (!filtros.mesinicio || !filtros.mesfin) {
         throw new Error("Mes de inicio y fin son requeridos para el reporte por grupo.");
     }
      try {
          console.log("(comprobanteService) Fetching reporte por grupo:", filtros);
          // Hacemos POST a /comprobantes/reportegrupos enviando los filtros en el cuerpo
          const response = await apiClient.post<ReporteData>('/reportes/por-grupo', filtros);
          console.log("(comprobanteService) Reporte por grupo recibido:", response.data);
          return response.data || []; // Devuelve los datos o un array vacío
      } catch (error) {
          console.error('(comprobanteService) Error fetching reporte por grupo:', error);
          if (axios.isAxiosError(error) && error.response?.data?.message) {
              throw new Error(`Error al obtener reporte por grupo: ${error.response.data.message}`);
          }
          throw new Error('No se pudo obtener el reporte por tamaño de grupo.');
      }
  };