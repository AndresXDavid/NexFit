import { Component } from '@angular/core';
import { BottomNavComponent } from '../../shared/bottom-nav/bottom-nav.component';

interface PuntoPeso { semana: string; kg: number; }

@Component({
  selector: 'app-progreso',
  standalone: true,
  imports: [BottomNavComponent],
  templateUrl: './progreso.component.html',
  styleUrl: './progreso.component.css'
})
export class ProgresoComponent {
  resumenMes = { entrenamientos: 18, minutos: 612, promedioSemanal: 4.2 };

  // Constancia de las últimas 6 semanas (sesiones completadas de 5 posibles).
  constancia = [3, 4, 5, 2, 4, 5];
  metaSemanal = 5;

  // Peso corporal — puntos de ejemplo para trazar la tendencia.
  peso: PuntoPeso[] = [
    { semana: 'S1', kg: 74.2 },
    { semana: 'S2', kg: 73.8 },
    { semana: 'S3', kg: 73.9 },
    { semana: 'S4', kg: 73.1 },
    { semana: 'S5', kg: 72.6 },
    { semana: 'S6', kg: 72.4 },
  ];

  get deltaPeso(): string {
    const delta = this.peso[this.peso.length - 1].kg - this.peso[0].kg;
    return `${delta > 0 ? '+' : ''}${delta.toFixed(1)} kg`;
  }

  get anchoSvg() { return 320; }
  get altoSvg() { return 120; }

  get puntosLinea(): string {
    const valores = this.peso.map(p => p.kg);
    const min = Math.min(...valores);
    const max = Math.max(...valores);
    const rango = max - min || 1;
    const pasoX = this.anchoSvg / (this.peso.length - 1);
    const margen = 14;

    return this.peso
      .map((p, i) => {
        const x = i * pasoX;
        const y = margen + (1 - (p.kg - min) / rango) * (this.altoSvg - margen * 2);
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  }
}
