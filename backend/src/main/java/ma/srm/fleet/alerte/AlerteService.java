package ma.srm.fleet.alerte;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.alerte.dto.AlerteResponse;
import ma.srm.fleet.common.exception.BusinessException;
import ma.srm.fleet.conducteur.conducteur.Conducteur;
import ma.srm.fleet.conformite.assurance.AssuranceRepository;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueRepository;
import ma.srm.fleet.referentiel.province.Province;
import ma.srm.fleet.referentiel.vehicule.Vehicule;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final AssuranceRepository assuranceRepository;
    private final VisiteTechniqueRepository visiteRepository;

    public AlerteService(AlerteRepository alerteRepository,
                         AssuranceRepository assuranceRepository,
                         VisiteTechniqueRepository visiteRepository) {
        this.alerteRepository = alerteRepository;
        this.assuranceRepository = assuranceRepository;
        this.visiteRepository = visiteRepository;
    }

    /**
     * RF-22 : tableau de bord des alertes non lues, trié par urgence (échéance la plus proche —
     * ou la plus en retard — d'abord). Un responsable ne voit que les alertes de sa province.
     */
    @Transactional(readOnly = true)
    public List<AlerteResponse> lister(UserPrincipal principal) {
        LocalDate aujourdhui = LocalDate.now();
        List<AlerteResponse> resultat = new ArrayList<>();
        for (Alerte alerte : alerteRepository.findByStatutLecture(StatutLecture.NON_LUE)) {
            Contexte ctx = resoudre(alerte);
            if (ctx == null || horsPortee(ctx.vehicule(), principal)) {
                continue;
            }
            resultat.add(toResponse(alerte, ctx, aujourdhui));
        }
        resultat.sort(Comparator.comparingLong(AlerteResponse::joursRestants));   // plus urgent d'abord
        return resultat;
    }

    /** Marque une alerte comme lue (elle quitte le tableau de bord des alertes actives). */
    public AlerteResponse marquerLue(Long id, UserPrincipal principal) {
        Alerte alerte = alerteRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Alerte introuvable : " + id, HttpStatus.NOT_FOUND));
        Contexte ctx = resoudre(alerte);
        if (ctx == null || horsPortee(ctx.vehicule(), principal)) {
            throw new BusinessException("Alerte introuvable : " + id, HttpStatus.NOT_FOUND);
        }
        alerte.setStatutLecture(StatutLecture.LUE);
        return toResponse(alerte, ctx, LocalDate.now());
    }

    /** Résout l'objet source d'une alerte (assurance ou visite) pour l'enrichir. */
    private Contexte resoudre(Alerte alerte) {
        return switch (alerte.getConfiguration().getTypeSource()) {
            case ASSURANCE -> assuranceRepository.findById(alerte.getIdReference())
                .map(a -> new Contexte(a.getVehicule(), a.getDateEcheance(), null))
                .orElse(null);
            case VISITE_TECHNIQUE -> visiteRepository.findById(alerte.getIdReference())
                .map(v -> new Contexte(v.getVehicule(), v.getDateEcheance(), v.getConducteurAccompagnateur()))
                .orElse(null);
        };
    }

    private boolean horsPortee(Vehicule v, UserPrincipal principal) {
        if (principal.getRole() != RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            return false;
        }
        Province prov = v.getProvince();
        return prov == null || !prov.getId().equals(principal.getProvinceId());
    }

    private AlerteResponse toResponse(Alerte alerte, Contexte ctx, LocalDate aujourdhui) {
        Vehicule v = ctx.vehicule();
        Province p = v.getProvince();
        Conducteur c = ctx.conducteur();
        return new AlerteResponse(
            alerte.getId(),
            alerte.getConfiguration().getTypeSource(),
            v.getId(), v.getImmatriculation(),
            p != null ? p.getId() : null, p != null ? p.getLibelle() : null,
            ctx.echeance(), ChronoUnit.DAYS.between(aujourdhui, ctx.echeance()),
            c != null ? c.getPrenom() + " " + c.getNom() : null,
            alerte.getStatutLecture(), alerte.getDateDeclenchement());
    }

    /** Contexte source résolu d'une alerte (véhicule concerné, échéance, conducteur éventuel). */
    private record Contexte(Vehicule vehicule, LocalDate echeance, Conducteur conducteur) {}
}
