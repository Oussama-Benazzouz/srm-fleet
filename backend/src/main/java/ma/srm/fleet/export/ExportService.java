package ma.srm.fleet.export;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ma.srm.fleet.affectation.AffectationService;
import ma.srm.fleet.affectation.StatutAffectation;
import ma.srm.fleet.affectation.dto.AffectationFilter;
import ma.srm.fleet.carte.cartecarburant.CarteService;
import ma.srm.fleet.carte.cartecarburant.StatutCarte;
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

    public ExportService(VehiculeService vehiculeService,
                         AffectationService affectationService,
                         CarteService carteService) {
        this.vehiculeService = vehiculeService;
        this.affectationService = affectationService;
        this.carteService = carteService;
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
