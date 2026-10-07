import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'nf-bottom-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, IconComponent],
  template: `
    <nav class="nf-nav">
      <a routerLink="/inicio" routerLinkActive="activo" class="nf-nav-item">
        <span class="nf-nav-icon"><nf-icon name="inicio" /></span>
        <span>Inicio</span>
      </a>
      <a routerLink="/rutinas" routerLinkActive="activo" class="nf-nav-item">
        <span class="nf-nav-icon"><nf-icon name="rutinas" /></span>
        <span>Rutinas</span>
      </a>
      <a routerLink="/progreso" routerLinkActive="activo" class="nf-nav-item">
        <span class="nf-nav-icon"><nf-icon name="progreso" /></span>
        <span>Progreso</span>
      </a>
      <a routerLink="/perfil" routerLinkActive="activo" class="nf-nav-item">
        <span class="nf-nav-icon"><nf-icon name="perfil" /></span>
        <span>Perfil</span>
      </a>
    </nav>
  `,
  styles: [`
    .nf-nav {
      position: sticky;
      bottom: 0;
      display: flex;
      background: var(--nf-black);
      padding: 10px 6px calc(10px + env(safe-area-inset-bottom, 0px));
      border-top: 3px solid var(--nf-yellow);
    }
    .nf-nav-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      padding: 6px 0;
      color: #8A8A82;
      font-size: 11px;
      font-weight: 600;
      border-radius: var(--r-sm);
    }
    .nf-nav-icon { width: 22px; height: 22px; }
    .nf-nav-item.activo { color: var(--nf-yellow); }
  `]
})
export class BottomNavComponent {}
