package ma.srm.fleet.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Classe de base (superclasse mappee, non-entite a part entiere) apportant
 * les quatre colonnes d'audit standard a toute entite metier qui en herite
 * (Vehicule, Affectation, CarteCarburant, etc.).
 *
 * Objectif : centraliser la tracabilite (qui a cree/modifie quoi, et quand)
 * en un seul endroit, plutot que de dupliquer ces quatre champs dans
 * chacune des entites du projet - exigence de tracabilite du cahier des
 * charges.
 *
 * Uniquement des getters (@Getter) : ces champs sont renseignes
 * automatiquement par le listener d'auditing, jamais manuellement par le
 * code metier -> aucun setter expose volontairement.
 */
@MappedSuperclass
// @MappedSuperclass (et non @Entity) : cette classe ne correspond a aucune
// table dediee ; ses colonnes sont simplement ajoutees a la table de
// chaque entite fille (Vehicule, Affectation...) lors du mapping JPA.
@EntityListeners(AuditingEntityListener.class)
// Ce listener est ce qui declenche reellement le remplissage des champs
// @CreatedDate/@CreatedBy... a chaque persist()/merge() Hibernate. Sans
// lui, les annotations d'auditing seraient presentes mais jamais executees.
@Getter
public abstract class AuditableEntity {

    /**
     * Date et heure de creation de l'enregistrement.
     * Renseignee une seule fois, jamais modifiee ensuite (updatable = false).
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de la derniere modification de l'enregistrement.
     * Mise a jour automatiquement a chaque modification persistee.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Auteur de la creation de l'enregistrement (fourni par AuditorAware,
     * cf. JpaAuditingConfig). Jamais modifie apres creation (updatable = false).
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Auteur de la derniere modification de l'enregistrement.
     * Mis a jour automatiquement a chaque modification persistee.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
