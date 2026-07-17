import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header class="app-header">SRM — Gestion de flotte</header>
    <main class="app-content">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .app-header {
      padding: 1rem 1.5rem;
      background: #144478;
      color: #fff;
      font-weight: 600;
    }
    .app-content {
      padding: 1.5rem;
    }
  `],
})
export class ShellComponent {}