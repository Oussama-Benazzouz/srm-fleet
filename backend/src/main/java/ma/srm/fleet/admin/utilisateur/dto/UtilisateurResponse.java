package ma.srm.fleet.admin.utilisateur.dto;

import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.admin.utilisateur.StatutCompte;

/**
 * DTO de reponse renvoye apres consultation, creation ou modification d'un
 * compte utilisateur.
 *
 * Ne contient jamais le mot de passe, meme hache : contrairement a
 * UtilisateurRequest (donnees entrantes, ou le mot de passe est necessaire
 * a la creation), ce DTO ne quitte le serveur que vers le client, donc
 * aucune raison d'y exposer un hash BCrypt qui n'a aucune utilite cote
 * frontend et representerait une fuite d'information inutile.
 *
 * statutCompte est expose ici (actif/desactive) pour permettre au
 * frontend d'afficher l'etat du compte et de proposer l'action
 * d'activation/desactivation correspondante (RF-27).
 */
public record UtilisateurResponse(
    Long id,
    String nom,
    String prenom,
    String login,
    String email,
    RoleUtilisateur role,
    StatutCompte statutCompte,
    Long provinceId
) {}
