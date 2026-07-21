package ma.srm.fleet.auth.dto;

import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;

/**
 * DTO de reponse renvoye apres une connexion reussie, ou lors de la
 * consultation du profil courant (/me).
 *
 * Ne contient jamais le mot de passe (meme hache) : seul le jeton JWT et
 * les informations necessaires au frontend pour adapter l'interface selon
 * le role et le perimetre geographique de l'utilisateur (RG12-RG14).
 *
 * token est nullable en pratique : voir la remarque sur /me ci-dessous.
 */
public record LoginResponse(
    String token,
    String login,
    RoleUtilisateur role,
    Long provinceId) {}
