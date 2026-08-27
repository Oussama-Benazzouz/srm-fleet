import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/theme/theme.service';
import { ToastContainerComponent } from './core/ui/toast-container.component';
import { ConfirmDialogComponent } from './core/ui/confirm-dialog.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainerComponent, ConfirmDialogComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  // Instancié dès le démarrage pour appliquer le thème mémorisé.
  private readonly theme = inject(ThemeService);
}
