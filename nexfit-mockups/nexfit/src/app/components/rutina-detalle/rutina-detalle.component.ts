import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IconComponent } from '../../shared/icon/icon.component';
import { Ejercicio } from '../../models/routine.model';

// Banco de ejercicios de ejemplo por rutina. En la app real esto vendría
// del servicio de rutinas (API), indexado por el id recibido en la ruta.
const BANCO_EJERCICIOS: Record<string, { nombre: string; ejercicios: Ejercicio[] }> = {
  'full-body-01': {
    nombre: 'Full Body Fuerza',
    ejercicios: [
      { id: 'e1', nombre: 'Sentadilla con barra', series: 4, repeticiones: '10', descanso: '60 seg' },
      { id: 'e2', nombre: 'Press de banca', series: 4, repeticiones: '8', descanso: '75 seg' },
      { id: 'e3', nombre: 'Remo con mancuerna', series: 3, repeticiones: '12', descanso: '45 seg' },
      { id: 'e4', nombre: 'Zancadas caminando', series: 3, repeticiones: '14 c/lado', descanso: '45 seg' },
      { id: 'e5', nombre: 'Plancha frontal', series: 3, repeticiones: '40 seg', descanso: '30 seg' },
    ],
  },
};

@Component({
  selector: 'app-rutina-detalle',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './rutina-detalle.component.html',
  styleUrl: './rutina-detalle.component.css'
})
export class RutinaDetalleComponent implements OnInit, OnDestroy {
  nombreRutina = '';
  ejercicios = signal<Ejercicio[]>([]);
  segundos = signal(0);
  private intervalo?: ReturnType<typeof setInterval>;

  completados = computed(() => this.ejercicios().filter(e => e.completado).length);
  progreso = computed(() => {
    const total = this.ejercicios().length || 1;
    return Math.round((this.completados() / total) * 100);
  });
  tiempoFormateado = computed(() => {
    const m = Math.floor(this.segundos() / 60).toString().padStart(2, '0');
    const s = (this.segundos() % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  });

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    const rutina = BANCO_EJERCICIOS[id] ?? BANCO_EJERCICIOS['full-body-01'];
    this.nombreRutina = rutina.nombre;
    this.ejercicios.set(rutina.ejercicios.map(e => ({ ...e })));

    this.intervalo = setInterval(() => this.segundos.update(s => s + 1), 1000);
  }

  ngOnDestroy() {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  alternarCompletado(id: string) {
    this.ejercicios.update(lista =>
      lista.map(e => e.id === id ? { ...e, completado: !e.completado } : e)
    );
  }

  salir() {
    this.router.navigate(['/rutinas']);
  }

  finalizar() {
    this.router.navigate(['/progreso']);
  }
}
