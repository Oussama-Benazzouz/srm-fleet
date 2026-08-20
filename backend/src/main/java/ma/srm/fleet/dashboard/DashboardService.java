package ma.srm.fleet.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ma.srm.fleet.admin.utilisateur.RoleUtilisateur;
import ma.srm.fleet.affectation.AffectationRepository;
import ma.srm.fleet.alerte.AlerteService;
import ma.srm.fleet.alerte.configuration.TypeSourceAlerte;
import ma.srm.fleet.alerte.dto.AlerteResponse;
import ma.srm.fleet.carte.cartecarburant.CarteCarburantRepository;
import ma.srm.fleet.carte.demande.DemandeCarteCarburantRepository;
import ma.srm.fleet.conducteur.conducteur.ConducteurRepository;
import ma.srm.fleet.conformite.sinistre.SinistreRepository;
import ma.srm.fleet.conformite.sinistre.StatutSinistre;
import ma.srm.fleet.dashboard.dto.CompteParLibelle;
import ma.srm.fleet.dashboard.dto.DashboardResponse;
import ma.srm.fleet.referentiel.vehicule.ModeAcquisition;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeRepository;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final AffectationRepository affectationRepository;
    private final CarteCarburantRepository carteRepository;
    private final DemandeCarteCarburantRepository demandeRepository;
    private final SinistreRepository sinistreRepository;
    private final AlerteService alerteService;

    public DashboardService(VehiculeRepository vehiculeRepository,
                            ConducteurRepository conducteurRepository,
                            AffectationRepository affectationRepository,
                            CarteCarburantRepository carteRepository,
                            DemandeCarteCarburantRepository demandeRepository,
                            SinistreRepository sinistreRepository,
                            AlerteService alerteService) {
        this.vehiculeRepository = vehiculeRepository;
        this.conducteurRepository = conducteurRepository;
        this.affectationRepository = affectationRepository;
        this.carteRepository = carteRepository;
        this.demandeRepository = demandeRepository;
        this.sinistreRepository = sinistreRepository;
        this.alerteService = alerteService;
    }

    public DashboardResponse construire(UserPrincipal principal) {
        // Portée : national pour l'Administrateur Central (prov = null), sinon la province du responsable.
        Long prov = null;
        if (principal.getRole() == RoleUtilisateur.RESPONSABLE_PROVINCIAL) {
            if (principal.getProvinceId() == null) {
                return vide();   // responsable sans province rattachée : périmètre vide
            }
            prov = principal.getProvinceId();
        }

        Map<StatutVehicule, Long> parStatut = enumCounts(vehiculeRepository.compterParStatut(prov));
        Map<ModeAcquisition, Long> parAcquisition = enumCounts(vehiculeRepository.compterParAcquisition(prov));
        Map<StatutSinistre, Long> sinistres = enumCounts(sinistreRepository.compterParStatut(prov));

        long vehicules = parStatut.values().stream().mapToLong(Long::longValue).sum();
        List<AlerteResponse> alertes = alerteService.lister(principal);

        return new DashboardResponse(
            vehicules,
            parStatut.getOrDefault(StatutVehicule.DISPONIBLE, 0L),
            parStatut.getOrDefault(StatutVehicule.AFFECTE, 0L),
            parStatut.getOrDefault(StatutVehicule.IMMOBILISE, 0L),
            conducteurRepository.compter(prov),
            affectationRepository.compterActives(prov),
            carteRepository.compterActives(prov),
            demandeRepository.compterEnAttente(prov),
            alertes.size(),
            sinistres.getOrDefault(StatutSinistre.OUVERT, 0L) + sinistres.getOrDefault(StatutSinistre.EN_COURS, 0L),
            List.of(
                new CompteParLibelle("Disponible", parStatut.getOrDefault(StatutVehicule.DISPONIBLE, 0L)),
                new CompteParLibelle("Affecté", parStatut.getOrDefault(StatutVehicule.AFFECTE, 0L)),
                new CompteParLibelle("Immobilisé", parStatut.getOrDefault(StatutVehicule.IMMOBILISE, 0L)),
                new CompteParLibelle("Sorti du parc", parStatut.getOrDefault(StatutVehicule.SORTI_DU_PARC, 0L))),
            List.of(
                new CompteParLibelle("Propriété", parAcquisition.getOrDefault(ModeAcquisition.PROPRIETE, 0L)),
                new CompteParLibelle("Location", parAcquisition.getOrDefault(ModeAcquisition.LOCATION, 0L))),
            libelleCounts(vehiculeRepository.compterParProvince(prov)),
            List.of(
                new CompteParLibelle("Ouvert", sinistres.getOrDefault(StatutSinistre.OUVERT, 0L)),
                new CompteParLibelle("En cours", sinistres.getOrDefault(StatutSinistre.EN_COURS, 0L)),
                new CompteParLibelle("Clôturé", sinistres.getOrDefault(StatutSinistre.CLOTURE, 0L))),
            alertesParType(alertes));
    }

    private List<CompteParLibelle> alertesParType(List<AlerteResponse> alertes) {
        long assurance = alertes.stream().filter(a -> a.typeSource() == TypeSourceAlerte.ASSURANCE).count();
        long visite = alertes.stream().filter(a -> a.typeSource() == TypeSourceAlerte.VISITE_TECHNIQUE).count();
        return List.of(
            new CompteParLibelle("Assurance", assurance),
            new CompteParLibelle("Visite technique", visite));
    }

    /** Transforme des lignes (enum, count) en table de comptage. */
    private static <E extends Enum<E>> Map<E, Long> enumCounts(List<Object[]> lignes) {
        Map<E, Long> map = new HashMap<>();
        for (Object[] ligne : lignes) {
            @SuppressWarnings("unchecked")
            E cle = (E) ligne[0];
            map.put(cle, (Long) ligne[1]);
        }
        return map;
    }

    /** Transforme des lignes (libellé, count) en parts de graphe. */
    private static List<CompteParLibelle> libelleCounts(List<Object[]> lignes) {
        List<CompteParLibelle> parts = new ArrayList<>();
        for (Object[] ligne : lignes) {
            parts.add(new CompteParLibelle((String) ligne[0], (Long) ligne[1]));
        }
        return parts;
    }

    private DashboardResponse vide() {
        List<CompteParLibelle> aucun = List.of();
        return new DashboardResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, aucun, aucun, aucun, aucun, aucun);
    }
}
