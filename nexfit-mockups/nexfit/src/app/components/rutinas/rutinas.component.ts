import { Component, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IconComponent } from '../../shared/icon/icon.component';
import { BottomNavComponent } from '../../shared/bottom-nav/bottom-nav.component';
import { Rutina } from '../../models/routine.model';

@Component({
  selector: 'app-rutinas',
  standalone: true,
  imports: [IconComponent, BottomNavComponent],
  templateUrl: './rutinas.component.html',
  styleUrl: './rutinas.component.css'
})
export class RutinasComponent {
  categorias = ['Todas', 'Fuerza', 'Cardio', 'Movilidad'];
  categoriaActiva = signal('Todas');

  // Datos de ejemplo — vendrán del servicio de rutinas cuando se conecte al backend.
  rutinas: Rutina[] = [
    {
      id: 'full-body-01', nombre: 'Full Body Fuerza', categoria: 'Fuerza',
      nivel: 'Intermedio', duracionMin: 42, caloriasAprox: 310, ejercicios: [],
    },
    {
      id: 'cardio-hiit-02', nombre: 'Cardio HIIT 20', categoria: 'Cardio',
      nivel: 'Avanzado', duracionMin: 20, caloriasAprox: 260, ejercicios: [],
    },
    {
      id: 'fuerza-tren-sup-03', nombre: 'Tren Superior', categoria: 'Fuerza',
      nivel: 'Intermedio', duracionMin: 35, caloriasAprox: 220, ejercicios: [],
    },
    {
      id: 'movilidad-04', nombre: 'Movilidad y Estiramiento', categoria: 'Movilidad',
      nivel: 'Principiante', duracionMin: 18, caloriasAprox: 90, ejercicios: [],
    },
    {
      id: 'piernas-05', nombre: 'Piernas y Glúteo', categoria: 'Fuerza',
      nivel: 'Avanzado', duracionMin: 48, caloriasAprox: 340, ejercicios: [],
    },
  ];

  rutinasFiltradas = computed(() => {
    const cat = this.categoriaActiva();
    return cat === 'Todas' ? this.rutinas : this.rutinas.filter(r => r.categoria === cat);
  });

  constructor(private router: Router) {}

  seleccionarCategoria(cat: string) {
    this.categoriaActiva.set(cat);
  }

  abrirRutina(id: string) {
    this.router.navigate(['/rutinas', id]);
  }
}
