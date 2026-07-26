package ma.srm.fleet.admin.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;

/**
 * DTO de requete pour la creation et la modification d'un compte
 * utilisateur (RF-27), reserve a l'Administrateur Central (RG14).
 *
 * Un seul record sert aux deux operations (creation et modification)
 * plutot que deux DTO distincts, avec une nuance geree au niveau du
 * service plutot que de la validation Bean Validation elle-meme :
 * motDePasse est obligatoire uniquement a la creation.
 */
public record UtilisateurRequest(
    @NotBlank String nom,
    @NotBlank String prenom,
    @NotBlank String login,
    @NotBlank @Email String email,
    String motDePasse,                 // obligatoire a la creation, ignore a la modification
    @NotNull RoleUtilisateur role,
    Long provinceId                    // NULL si administrateur central (RG14)
) {}
