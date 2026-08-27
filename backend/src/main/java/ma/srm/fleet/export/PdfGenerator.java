package ma.srm.fleet.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Génère un PDF paysage A4 conforme à la charte SRM Souss-Massa : logo centré en en-tête,
 * filet tricolore (bleu / vert / jaune), titre du document, tableau à en-tête bleu, et pied
 * de page (date de génération, mention, numéro de page) répétés sur chaque page.
 *
 * <p>Le logo est chargé depuis {@code classpath:/branding/logo-srm.png} ; s'il est absent,
 * un libellé texte « SRM Souss-Massa » le remplace afin que l'export reste fonctionnel.</p>
 */
final class PdfGenerator {

    // Couleurs officielles de la charte graphique.
    private static final Color BLEU = new Color(0x14, 0x50, 0x8F);
    private static final Color VERT = new Color(0x5A, 0xB2, 0x4B);
    private static final Color JAUNE = new Color(0xFA, 0xB6, 0x1D);
    private static final Color ENCRE = new Color(0x16, 0x18, 0x1D);
    private static final Color GRIS = new Color(0x5F, 0x64, 0x6E);
    private static final Color BORDURE = new Color(0xE3, 0xE5, 0xE9);
    private static final float MARGE = 36f;

    private PdfGenerator() {}

    static byte[] generer(String titre, List<String> entetes, List<List<String>> lignes) {
        Document document = new Document(PageSize.A4.rotate(), MARGE, MARGE, 100f, 46f);
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            writer.setPageEvent(new BrandingEvent(titre));
            document.open();
            document.add(construireTable(entetes, lignes));
            document.close();
        } catch (DocumentException e) {
            throw new BusinessException("Échec de la génération du PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return sortie.toByteArray();
    }

    private static PdfPTable construireTable(List<String> entetes, List<List<String>> lignes) {
        PdfPTable table = new PdfPTable(entetes.size());
        table.setWidthPercentage(100);

        Font policeEntete = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        for (String entete : entetes) {
            PdfPCell cellule = new PdfPCell(new Phrase(entete, policeEntete));
            cellule.setBackgroundColor(BLEU);
            cellule.setPadding(5);
            table.addCell(cellule);
        }

        Font policeCellule = new Font(Font.HELVETICA, 8, Font.NORMAL, ENCRE);
        boolean pair = false;
        for (List<String> ligne : lignes) {
            Color fond = pair ? new Color(0xF5, 0xF6, 0xF8) : Color.WHITE;
            for (String valeur : ligne) {
                PdfPCell cellule = new PdfPCell(new Phrase(valeur == null ? "" : valeur, policeCellule));
                cellule.setPadding(4);
                cellule.setBackgroundColor(fond);
                table.addCell(cellule);
            }
            pair = !pair;
        }
        return table;
    }

    /** Dessine l'en-tête (logo centré + filet tricolore + titre) et le pied sur chaque page. */
    private static final class BrandingEvent extends PdfPageEventHelper {

        private final String titre;
        private final String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        private final byte[] logo = chargerLogo();

        private BrandingEvent(String titre) {
            this.titre = titre;
        }

        private static byte[] chargerLogo() {
            // Tolérant à l'extension : on prend le premier logo présent (png/jpg/jpeg).
            for (String chemin : new String[] {
                "/branding/logo-srm.png", "/branding/logo-srm.jpg", "/branding/logo-srm.jpeg"
            }) {
                try (InputStream in = PdfGenerator.class.getResourceAsStream(chemin)) {
                    if (in != null) {
                        return in.readAllBytes();
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle page = document.getPageSize();
            float w = page.getWidth();
            float h = page.getHeight();

            // Logo centré (ou libellé de repli).
            if (logo != null) {
                try {
                    Image image = Image.getInstance(logo);
                    float hauteur = 44f;
                    float echelle = hauteur / image.getHeight();
                    float largeur = image.getWidth() * echelle;
                    image.scaleAbsolute(largeur, hauteur);
                    image.setAbsolutePosition((w - largeur) / 2f, h - 54f);
                    cb.addImage(image);
                } catch (Exception ignore) {
                    // en cas d'image illisible, on continue sans logo
                }
            } else {
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("SRM Souss-Massa", new Font(Font.HELVETICA, 15, Font.BOLD, BLEU)),
                    w / 2f, h - 40f, 0);
            }

            // Filet tricolore.
            float y = h - 62f;
            float tiers = (w - 2 * MARGE) / 3f;
            bande(cb, BLEU, MARGE, y, tiers);
            bande(cb, VERT, MARGE + tiers, y, tiers);
            bande(cb, JAUNE, MARGE + 2 * tiers, y, tiers);

            // Titre du document.
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase(titre, new Font(Font.HELVETICA, 13, Font.BOLD, ENCRE)), w / 2f, h - 80f, 0);

            // Pied de page.
            cb.setColorFill(BORDURE);
            cb.rectangle(MARGE, 34f, w - 2 * MARGE, 0.6f);
            cb.fill();
            Font pied = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Généré le " + date, pied), MARGE, 22f, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("Gestion de flotte — Région Souss-Massa", pied), w / 2f, 22f, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Page " + writer.getPageNumber(), pied), w - MARGE, 22f, 0);
        }

        private static void bande(PdfContentByte cb, Color couleur, float x, float y, float largeur) {
            cb.setColorFill(couleur);
            cb.rectangle(x, y, largeur, 2.5f);
            cb.fill();
        }
    }
}
