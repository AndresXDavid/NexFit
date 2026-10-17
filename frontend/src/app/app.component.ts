import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterOutlet } from '@angular/router';

type Estado = 'consultando' | 'UP' | 'sin conexión';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html'
})
export class AppComponent {
  private readonly http = inject(HttpClient);
  // Pantalla provisional del esqueleto: comprueba que web y API están conectadas.
  protected readonly estado = signal<Estado>('consultando');

  constructor() {
    this.http.get<{ status: string }>('/api/actuator/health').subscribe({
      next: (body) => this.estado.set(body.status === 'UP' ? 'UP' : 'sin conexión'),
      error: () => this.estado.set('sin conexión')
    });
  }
}
