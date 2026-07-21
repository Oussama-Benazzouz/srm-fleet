package ma.srm.fleet.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requete pour l'endpoint de connexion (POST /api/v1/auth/login).
 *
 * Record (immuable, sans logique) : contrat d'entree, jamais persiste tel
 * quel. Les deux champs sont obligatoires (@NotBlank) — une requete avec un
 * champ vide ou absent est rejetee automatiquement par la validation Bean
 * Validation, avant meme d'atteindre le controleur, et produit une reponse
 * d'erreur RFC 7807 via le GlobalExceptionHandler deja en place.
 */
public record LoginRequest(
    @NotBlank String login,
    @NotBlank String motDePasse) {}
