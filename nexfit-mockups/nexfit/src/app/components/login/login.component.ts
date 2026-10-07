import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/icon/icon.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, IconComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  correo = signal('');
  clave = signal('');
  mostrarClave = signal(false);

  constructor(private router: Router) {}

  alternarClave() {
    this.mostrarClave.update(v => !v);
  }

  ingresar() {
    // TODO: conectar con el servicio de autenticación real.
    // Por ahora, cualquier envío del formulario navega al dashboard.
    this.router.navigate(['/inicio']);
  }
}
