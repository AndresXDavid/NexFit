export interface Ejercicio {
  id: string;
  nombre: string;
  series: number;
  repeticiones: string; // ej: "12" o "30 seg"
  descanso: string; // ej: "45 seg"
  completado?: boolean;
}

export interface Rutina {
  id: string;
  nombre: string;
  categoria: string;
  nivel: 'Principiante' | 'Intermedio' | 'Avanzado';
  duracionMin: number;
  caloriasAprox: number;
  ejercicios: Ejercicio[];
  colorAcento?: 'amarillo' | 'negro';
}

export interface RegistroSemanal {
  dia: string; // 'L', 'M', 'X', 'J', 'V', 'S', 'D'
  completado: boolean;
}

export interface Usuario {
  nombre: string;
  programa: string;
  semestre: number;
  racha: number;
  metaSemanal: number;
  entrenamientosCompletados: number;
}
