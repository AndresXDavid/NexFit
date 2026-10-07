import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RutinasComponent } from './components/rutinas/rutinas.component';
import { RutinaDetalleComponent } from './components/rutina-detalle/rutina-detalle.component';
import { ProgresoComponent } from './components/progreso/progreso.component';
import { PerfilComponent } from './components/perfil/perfil.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'inicio', component: DashboardComponent },
  { path: 'rutinas', component: RutinasComponent },
  { path: 'rutinas/:id', component: RutinaDetalleComponent },
  { path: 'progreso', component: ProgresoComponent },
  { path: 'perfil', component: PerfilComponent },
  { path: '**', redirectTo: 'login' },
];
