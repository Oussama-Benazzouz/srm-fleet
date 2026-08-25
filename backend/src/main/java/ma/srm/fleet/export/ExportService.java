package ma.srm.fleet.export;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ma.srm.fleet.affectation.AffectationService;
import ma.srm.fleet.affectation.StatutAffectation;
import ma.srm.fleet.affectation.dto.AffectationFilter;
import ma.srm.fleet.carte.cartecarburant.CarteService;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
import ma.srm.fleet.conformite.assurance.AssuranceService;
import ma.srm.fleet.conformite.sinistre.SinistreService;
import ma.srm.fleet.conformite.sinistre.StatutSinistre;
import ma.srm.fleet.conformite.visitetechnique.VisiteTechniqueService;
import ma.srm.fleet.referentiel.vehicule.StatutVehicule;
import ma.srm.fleet.referentiel.vehicule.VehiculeService;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.stereotype.Service;

/**
 * Produit les fichiers d'export en réutilisant les services de liste : les données exportées
 * reflètent donc exactement les mêmes filtres et le même cloisonnement province que les écrans
 * (RF-25). Le format (CSV/PDF) est choisi par l'appelant.
 */
@Service
public class ExportService {

    private final VehiculeService vehiculeService;
    private final AffectationService affectationService;
    private final CarteService carteService;
    private final SinistreService sinistreService;
    private final AssuranceService assuranceService;
    private final VisiteTechniqueService visiteService;

    public ExportService(VehiculeService vehiculeService,
                         AffectationService affectationService,
                         CarteService carteService,
                         SinistreService sinistreService,
                         AssuranceService assuranceService,
                         VisiteTechniqueService visiteService) {
        this.vehiculeService = vehiculeService;
        this.affectationService = affectationService;
        this.carteService = carteService;
        this.sinistreService = sinistreService;
        this.assuranceService = assuranceService;
        this.visiteService = visiteService;
    }

    public ExportFichier vehicules(UserPrincipal user, ExportFormat format,
                                   String immatriculation, String type, StatutVehicule statut) {
        List<List<String>> lignes = vehiculeService.rechercher(user, immatriculation, type, statut).stream()
            .map(v -> List.of(
                val(v.immatriculation()), val(v.typeVehicule()), val(v.modeAcquisition()), val(v.statut()),
                val(v.provinceLibelle()), val(v.departementLibelle()), val(v.fournisseurRaisonSociale()),
                val(v.dateMiseEnService()), val(v.numeroContrat()), val(v.loyerMensuel()), val(v.plafondKm())))
            .toList();
        return construire(format, "vehicules", "Véhicules",
            List.of("Immatriculation", "Type", "Acquisition", "Statut", "Province", "Département",
                "Fournisseur", "Mise en service", "N° contrat", "Loyer mensuel", "Plafond km"),
            lignes);
    }

    public ExportFichier affectations(UserPrincipal user, ExportFormat format,
                                      StatutAffectation statut, Long vehiculeId, Long conducteurId) {
        List<List<String>> lignes = affectationService
            .lister(new AffectationFilter(statut, vehiculeId, conducteurId), user).stream()
            .map(a -> List.of(
                val(a.immatriculation()), val(a.conducteurNomComplet()), val(a.matriculeConducteur()),
                val(a.provinceLibelle()), val(a.statut()), val(a.dateDebut()), val(a.dateFin()), val(a.motif())))
            .toList();
        return construire(format, "affectations", "Affectations",
            List.of("Véhicule", "Conducteur", "Matricule", "Province", "Statut",
                "Début", "Fin", "Motif"),
            lignes);
    }

    public ExportFichier cartes(UserPrincipal user, ExportFormat format, StatutCarte statut, Long affectationId) {
        List<List<String>> lignes = carteService.lister(statut, affectationId, user).stream()
            .map(c -> List.of(
                val(c.numeroCarte()), val(c.immatriculation()), val(c.provinceLibelle()), val(c.statut()),
                val(c.plafondMontant()), val(c.fournisseurRaisonSociale()), val(c.dateCreation())))
            .toList();
        return construire(format, "cartes-carburant", "Cartes carburant",
            List.of("N° carte", "Véhicule", "Province", "Statut", "Plafond", "Fournisseur", "Créée le"),
            lignes);
    }

    public ExportFichier sinistres(UserPrincipal user, ExportFormat format,
                                   StatutSinistre statut, Long vehiculeId) {
        List<List<String>> lignes = sinistreService.lister(statut, vehiculeId, user).stream()
            .map(s -> List.of(
                val(s.immatriculation()), val(s.dateSinistre()), val(s.statut()),
                val(s.conducteurNomComplet()), val(s.provinceLibelle()), val(s.description())))
            .toList();
        return construire(format, "sinistres", "Sinistres",
            List.of("Véhicule", "Date", "Statut", "Conducteur responsable", "Province", "Description"),
            lignes);
    }

    public ExportFichier assurances(UserPrincipal user, ExportFormat format, Long vehiculeId) {
        List<List<String>> lignes = assuranceService.lister(vehiculeId, user).stream()
            .map(a -> List.of(
                val(a.immatriculation()), val(a.nomCompagnie()), val(a.dateDebut()),
                val(a.dateEcheance()), val(a.provinceLibelle())))
            .toList();
        return construire(format, "assurances", "Assurances",
            List.of("Véhicule", "Compagnie", "Début", "Échéance", "Province"),
            lignes);
    }

    public ExportFichier visites(UserPrincipal user, ExportFormat format, Long vehiculeId) {
        List<List<String>> lignes = visiteService.lister(vehiculeId, user).stream()
            .map(v -> List.of(
                val(v.immatriculation()), val(v.nomCompagnie()), val(v.dateVisite()), val(v.dateEcheance()),
                val(v.conducteurNomComplet()), val(v.matriculeConducteur()), val(v.provinceLibelle())))
            .toList();
        return construire(format, "visites-techniques", "Visites techniques",
            List.of("Véhicule", "Compagnie", "Date visite", "Échéance", "Conducteur", "Matricule", "Province"),
            lignes);
    }

    private ExportFichier construire(ExportFormat format, String base, String titre,
                                     List<String> entetes, List<List<String>> lignes) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        if (format == ExportFormat.PDF) {
            return new ExportFichier(
                PdfGenerator.generer(titre, entetes, lignes), "application/pdf", base + "-" + date + ".pdf");
        }
        return new ExportFichier(
            CsvGenerator.generer(entetes, lignes), "text/csv;charset=UTF-8", base + "-" + date + ".csv");
    }

    /** Valeur de cellule sûre (jamais null), à partir de n'importe quel type. */
    private static String val(Object valeur) {
        return valeur == null ? "" : String.valueOf(valeur);
    }
}
