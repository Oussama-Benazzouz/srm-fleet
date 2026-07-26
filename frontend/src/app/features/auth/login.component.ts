import { HttpErrorResponse } from '@angular/common/http';
import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { HOME_URL } from '../../core/auth/auth.guard';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly submitted = signal(false);
  protected readonly passwordVisible = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    login: ['', Validators.required],
    motDePasse: ['', Validators.required],
  });

  /** Vrai lorsqu'un champ doit afficher son erreur (après soumission ou sortie). */
  protected isInvalid(name: 'login' | 'motDePasse'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || this.submitted());
  }

  protected togglePassword(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  protected submit(): void {
    this.submitted.set(true);
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(this.safeReturnUrl(returnUrl));
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.messageFor(err));
        this.loading.set(false);
      },
    });
  }

  /**
   * N'accepte qu'un chemin interne : empêche une redirection ouverte via le
   * paramètre `returnUrl`.
   */
  private safeReturnUrl(url: string | null): string {
    return url?.startsWith('/') && !url.startsWith('//') ? url : HOME_URL;
  }

  private messageFor(err: HttpErrorResponse): string {
    if (err.status === 0) return 'Serveur injoignable. Vérifiez votre connexion.';
    if (err.status === 401 || err.status === 403) {
      return 'Identifiants invalides ou compte désactivé.';
    }
    return 'Une erreur est survenue. Veuillez réessayer.';
  }
}
