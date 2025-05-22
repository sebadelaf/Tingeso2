// Al principio del archivo, o en tu archivo de tipos (ej. src/types/Reporte.ts)
export interface ReporteDTO {
    mesinicio: string; // Formato "yyyy-MM"
    mesfin: string;    // Formato "yyyy-MM"
  }
  
  // También podemos definir el tipo de dato que esperamos de vuelta del backend.
  // El backend devuelve List<Object> donde cada Object es List<Long>.
  // En TypeScript, esto se traduce a un array de arrays de números.
  export type ReporteData = number[][];