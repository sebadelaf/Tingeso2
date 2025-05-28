import axios from 'axios';
import { ReservaEntity, ReservaFormInput } from '../types/reserva';
//direccion de mi backend
const API_BASE_URL = 'http://127.0.0.1:57049';
// Creamos una "instancia" de axios. Es como configurar un mensajero
// que ya sabe a qué dirección base (API_BASE_URL) ir y que habla JSON.
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json', // Le decimos al backend que le enviaremos datos en formato JSON
    },
  });

//Obtener todas las reservas para rellenar el calendario
export const fetchReservas=async(): Promise<ReservaEntity[]> =>{
    try{
        const response = await apiClient.get<ReservaEntity[]>('/rack/reservas'); // Hacemos una petición GET a '/reservas'.
        return response.data;
    }
    catch(error){
        console.error('Error al obtener las reservas:', error); // Mostramos el error en la consola del navegador.
        throw error; // Lanzamos el error para que quien llamó a esta función sepa que falló.
    }
};

//Crear la reserva
    export const createReserva = async (reservaData: ReservaFormInput): Promise<ReservaEntity> => {
    try {
      // Hacemos una petición POST a '/reservas/crear'.
      // El segundo argumento ('reservaData') son los datos que enviamos en el cuerpo de la petición (en formato JSON).
      const response = await apiClient.post<ReservaEntity>('/reservas/crear', reservaData);
      // Devolvemos los datos de la reserva creada que nos devuelve el backend.
      return response.data;
    } catch (error) {
      console.error('Error al crear la reserva:', error);
      // Intentamos dar un mensaje más útil si el backend envió un error específico.
      // 'axios.isAxiosError(error)' comprueba si el error viene de axios.
      // 'error.response?.data?.message' intenta acceder al mensaje dentro de la respuesta de error del backend.
      if (axios.isAxiosError(error) && error.response?.data?.message) {
        // Si encontramos un mensaje específico, creamos un nuevo Error con ese mensaje y lo lanzamos.
        throw new Error(error.response.data.message);
      }
      // Si no, lanzamos el error original.
      throw error;
    }
  };

