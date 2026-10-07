import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { IconComponent } from '../../shared/icon/icon.component';
import { BottomNavComponent } from '../../shared/bottom-nav/bottom-nav.component';
import { Usuario } from '../../models/routine.model';

interface OpcionPerfil {
  icono: string;
  etiqueta: string;
  valor?: string;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [IconComponent, BottomNavComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css'
})
export class PerfilComponent {
  usuario: Usuario = {
    nombre: 'Daniela Ortiz',
    programa: 'Ingeniería de Sistemas',
    semestre: 6,
    racha: 4,
    metaSemanal: 5,
    entrenamientosCompletados: 18,
  };

  opciones: OpcionPerfil[] = [
    { icono: 'ajustes', etiqueta: 'Datos personales' },
    { icono: 'reloj', etiqueta: 'Meta semanal', valor: `${this.usuario.metaSemanal} sesiones` },
    { icono: 'progreso', etiqueta: 'Unidades de medida', valor: 'kg / cm' },
  ];

  constructor(private router: Router) {}

  cerrarSesion() {
    this.router.navigate(['/login']);
  }
}
