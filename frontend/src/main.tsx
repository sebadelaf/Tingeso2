// src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import { ThemeProvider, createTheme } from '@mui/material/styles';

// --- Definición del Tema ---
const theme = createTheme({
  palette: {
    // Modo oscuro o claro (puedes elegir 'light' o 'dark')
    mode: 'light',
    primary: {
      // Color principal (ej. un rojo para 'KarRM')
      main: '#DC0000', // Puedes buscar códigos hexadecimales de colores que te gusten
    },
    secondary: {
      // Color secundario (ej. un gris oscuro)
      main: '#424242',
    },
    background: {
      // Color de fondo general
      default: '#f5f5f5', // Un gris muy claro
      paper: '#ffffff', // Color para superficies como Cards, Modals, etc. (blanco)
    },
  },
  typography: {
    // Puedes definir fuentes aquí si quieres
    // fontFamily: 'Roboto, Arial, sans-serif',
    h6: { // Estilo para el título en la AppBar
        fontWeight: 700, // Más grueso
    }
  },
  // Puedes personalizar más cosas: forma de botones, espaciado, etc.
  // shape: {
  //  borderRadius: 8,
  // },
});
// -------------------------


ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <App />
    </ThemeProvider>
  </React.StrictMode>,
);