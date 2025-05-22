// src/App.tsx
import React from 'react';
// Importaciones de MUI para Layout, AppBar, Tabs, etc.
import {
    Box, Tabs, Tab, Typography, CssBaseline, AppBar, Toolbar, IconButton, Container
} from '@mui/material';
// Importamos un icono de coche
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
// Importamos nuestro componente de la primera pestaña
import BookingTab from './components/BookingTab';
import ReportTab from './components/ReportTab'; // Añade esta línea

// --- Componente Helper para el Panel de cada Pestaña ---
interface TabPanelProps { children?: React.ReactNode; index: number; value: number; }
function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} id={`tabpanel-${index}`} {...other}>
      {/* Usamos Container para centrar y limitar el ancho del contenido en pantallas grandes */}
      {value === index && <Container maxWidth="lg" sx={{ py: 3 }}>{children}</Container>}
    </div>
  );
}

// --- Componente Principal App ---
function App() {
  const [tabIndex, setTabIndex] = React.useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabIndex(newValue);
  };

  return (
    // Box principal que ocupa toda la altura y aplica el color de fondo del tema
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
      <CssBaseline /> {/* Aplica estilos base consistentes */}

      {/* --- Barra de Navegación Superior --- */}
      <AppBar position="static" color="primary"> {/* Usa el color primario del tema */}
        {/* Toolbar ayuda a alinear elementos dentro de la AppBar */}
        <Toolbar>
          {/* IconButton para el logo */}
          <IconButton
            size="large"
            edge="start"
            color="inherit" // Hereda el color del texto de la AppBar (blanco por defecto si primary es oscuro)
            aria-label="logo"
            sx={{ mr: 1 }} // Margen derecho pequeño
          >
            <DirectionsCarFilledIcon /> {/* El icono del coche */}
          </IconButton>
          {/* Título de la Empresa */}
          <Typography
            variant="h6" // Tamaño del texto (definido en el tema)
            component="div" // Renderiza como un div
            sx={{ flexGrow: 1 }} // Hace que ocupe el espacio restante, empujando otros elementos a la derecha
          >
            KartingGO
          </Typography>
        </Toolbar>
      </AppBar>
      {/* --- Fin Barra de Navegación --- */}


      {/* --- Contenedor para las Pestañas --- */}
      {/* Lo ponemos debajo de la AppBar con un borde */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Tabs
          value={tabIndex}
          onChange={handleTabChange}
          aria-label="pestañas principales"
          variant="fullWidth" // Hace que las pestañas ocupen todo el ancho
          indicatorColor="secondary" // Color del indicador de la pestaña activa
          textColor="secondary" // Color del texto de la pestaña activa
        >
          <Tab label="Reservar Horario" id="tab-0" />
          <Tab label="Reportes" id="tab-1" aria-controls="tabpanel-1"/>
        </Tabs>
      </Box>
      {/* --- Fin Contenedor Pestañas --- */}


      {/* --- Área de Contenido Principal --- */}
      {/* Este Box ocupa el espacio restante */}
      <Box component="main" sx={{ flexGrow: 1 }}>
        {/* Contenido de la primera pestaña */}
        <TabPanel value={tabIndex} index={0}>
          <BookingTab />
        </TabPanel>

        {/* Contenido de la segunda pestaña */}
        <TabPanel value={tabIndex} index={1}>
          <ReportTab />
        </TabPanel>
      </Box>
      {/* --- Fin Área de Contenido --- */}

    </Box> // Fin Box principal
  );
}

export default App;