package ma.srm.fleet.carte.demande;

import java.time.LocalDate;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.admin.utilisateur.Utilisateur;
import ma.srm.fleet.admin.utilisateur.UtilisateurRepository;
import ma.srm.fleet.affectation.Affectation;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.carte.cartecarburant.CarteCarburant;
import ma.srm.fleet.carte.cartecarburant.CarteCarburantRepository;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
import ma.srm.fleet.carte.demande.dto.DemandeRequest;
import ma.srm.fleet.carte.demande.dto.DemandeResponse;
import ma.srm.fleet.carte.demande.dto.ValiderRequest;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.referentiel.fournisseur.Fournisseur;
import ma.srm.fleet.referentiel.fournisseur.FournisseurRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemandeService {

    private final DemandeCarteCarburantRepository demandeRepository;
    private final AffectationRepository affectationRepository;
    private final CarteCarburantRepository carteRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DemandeService(DemandeCarteCarburantRepository demandeRepository,
                          AffectationRepository affectationRepository,
                          CarteCarburantRepository carteRepository,
                          FournisseurRepository fournisseurRepository,
                          UtilisateurRepository utilisateurRepository) {
        this.demandeRepository = demandeRepository;
        this.affectationRepository = affectationRepository;
        this.carteRepository = carteRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> lister(StatutDemandeCarte statut, Long affectationId, UserPrincipal principal) {
        Specification<DemandeCarteCarburant> spec = Specification.allOf(
            DemandeSpecifications.statut(statut),
            DemandeSpecifications.affectation(affectationId));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) return List.of();
            spec = spec.and(DemandeSpecifications.province(principal.getProvinceId()));
        }
        return demandeRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DemandeResponse parId(Long id, UserPrincipal principal) {
        DemandeCarteCarburant d = charger(id);
        verifierPortee(d, principal);
        return toResponse(d);
    }

    /** RF-15 : créer une demande (sans plafond), scopée par province pour un provincial. */
    public DemandeResponse creer(DemandeRequest req, UserPrincipal principal) {
        Affectation aff = affectationRepository.findById(req.affectationId())
            .orElseThrow(() -> new BusinessException("Affectation introuvable", HttpStatus.BAD_REQUEST));
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Province prov = aff.getVehicule().getProvince();
            if (prov == null || !prov.getId().equals(principal.getProvinceId())) {
                throw new BusinessException("Affectation introuvable : " + aff.getId(), HttpStatus.NOT_FOUND);
            }
        }

        // RG4 : refuser la demande en amont si l'affectation porte déjà une carte active,
        // plutôt que de laisser le central le découvrir à la validation. Vaut pour tout rôle.
        if (carteRepository.existsByAffectationIdAndStatut(aff.getId(), StatutCarte.ACTIVE)) {
            throw new BusinessException(
                "Cette affectation possède déjà une carte carburant active ; "
                    + "aucune nouvelle demande ne peut être soumise tant qu'elle n'est pas clôturée",
                HttpStatus.CONFLICT);
        }

        Utilisateur demandeur = utilisateurRepository.findById(principal.getId())
            .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.BAD_REQUEST));

        DemandeCarteCarburant d = new DemandeCarteCarburant();
        d.setAffectation(aff);
        d.setMotif(req.motif());
        d.setDateDemande(LocalDate.now());
        d.setStatut(StatutDemandeCarte.EN_ATTENTE);
        d.setDemandeur(demandeur);
        return toResponse(demandeRepository.save(d));
    }

    /** RF-16 + RG5 : le central valide → fixe le plafond + fournisseur → émet la carte ACTIVE. */
    public DemandeResponse valider(Long id, ValiderRequest req, UserPrincipal principal) {
        DemandeCarteCarburant d = charger(id);
        exigerEnAttente(d);
        Affectation aff = d.getAffectation();

        if (carteRepository.existsByNumeroCarte(req.numeroCarte())) {
            throw new BusinessException("Ce numéro de carte existe déjà", HttpStatus.CONFLICT);
        }
        if (carteRepository.existsByAffectationIdAndStatut(aff.getId(), StatutCarte.ACTIVE)) {
            throw new BusinessException(
                "Cette affectation a déjà une carte active ; clôturez-la avant d'en émettre une nouvelle",
                HttpStatus.CONFLICT);
        }
        Fournisseur f = fournisseurRepository.findById(req.fournisseurId())
            .orElseThrow(() -> new BusinessException("Fournisseur introuvable", HttpStatus.BAD_REQUEST));

        CarteCarburant carte = new CarteCarburant();
        carte.setNumeroCarte(req.numeroCarte());
        carte.setPlafondMontant(req.plafondMontant());
        carte.setStatut(StatutCarte.ACTIVE);
        carte.setDateCreation(LocalDate.now());
        carte.setAffectation(aff);
        carte.setFournisseur(f);
        carte.setDemandeOrigine(d);
        try {
            carteRepository.saveAndFlush(carte);   // l'index partiel tranche en cas de concurrence
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Cette affectation a déjà une carte active (RG4)", HttpStatus.CONFLICT);
        }

        d.setStatut(StatutDemandeCarte.VALIDEE);
        d.setDateTraitement(LocalDate.now());
        d.setValidateur(utilisateurRepository.findById(principal.getId())
            .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.BAD_REQUEST)));
        return toResponse(d);
    }

    /** RF-16 : refus (sans plafond ni carte). Le schéma ne stocke pas de motif de refus. */
    public DemandeResponse refuser(Long id, UserPrincipal principal) {
        DemandeCarteCarburant d = charger(id);
        exigerEnAttente(d);
        d.setStatut(StatutDemandeCarte.REFUSEE);
        d.setDateTraitement(LocalDate.now());
        d.setValidateur(utilisateurRepository.findById(principal.getId())
            .orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.BAD_REQUEST)));
        return toResponse(d);
    }

    private void exigerEnAttente(DemandeCarteCarburant d) {
        if (d.getStatut() != StatutDemandeCarte.EN_ATTENTE) {
            throw new BusinessException("Cette demande a déjà été traitée", HttpStatus.CONFLICT);
        }
    }

    private void verifierPortee(DemandeCarteCarburant d, UserPrincipal principal) {
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            Province prov = d.getAffectation().getVehicule().getProvince();
            if (prov == null || !prov.getId().equals(principal.getProvinceId())) {
                throw new BusinessException("Demande introuvable : " + d.getId(), HttpStatus.NOT_FOUND);
            }
        }
    }

    private DemandeCarteCarburant charger(Long id) {
        return demandeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Demande introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    private DemandeResponse toResponse(DemandeCarteCarburant d) {
        Affectation a = d.getAffectation();
        Vehicule v = a.getVehicule();
        Province p = v.getProvince();
        Utilisateur dem = d.getDemandeur();
        Utilisateur val = d.getValidateur();
        return new DemandeResponse(
            d.getId(), a.getId(), v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            d.getDateDemande(), d.getStatut(), d.getMotif(),
            dem.getLogin(), dem.getPrenom() + " " + dem.getNom(),
            d.getDateTraitement(), val != null ? val.getLogin() : null);
    }
}
