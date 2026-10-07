import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { IconComponent } from '../../shared/icon/icon.component';
import { BottomNavComponent } from '../../shared/bottom-nav/bottom-nav.component';
import { RegistroSemanal, Usuario } from '../../models/routine.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [IconComponent, BottomNavComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  // Datos de ejemplo — en la app real vendrán del servicio de usuario/rutinas.
  usuario: Usuario = {
    nombre: 'Daniela Ortiz',
    programa: 'Ingeniería de Sistemas',
    semestre: 6,
    racha: 4,
    metaSemanal: 5,
    entrenamientosCompletados: 18,
  };

  semana: RegistroSemanal[] = [
    { dia: 'L', completado: true },
    { dia: 'M', completado: true },
    { dia: 'X', completado: true },
    { dia: 'J', completado: true },
    { dia: 'V', completado: false },
    { dia: 'S', completado: false },
    { dia: 'D', completado: false },
  ];

  rutinaDeHoy = {
    id: 'full-body-01',
    nombre: 'Full Body Fuerza',
    duracionMin: 42,
    caloriasAprox: 310,
    nivel: 'Intermedio',
  };

  constructor(private router: Router) {}

  comenzarEntrenamiento() {
    this.router.navigate(['/rutinas', this.rutinaDeHoy.id]);
  }
}
