package ma.srm.fleet.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import ma.srm.fleet.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/** Génère un PDF paysage A4 : titre + tableau (en-tête coloré aux couleurs de la marque). */
final class PdfGenerator {

    private static final Color ACCENT = new Color(0x14, 0x49, 0x7E);

    private PdfGenerator() {}

    static byte[] generer(String titre, List<String> entetes, List<List<String>> lignes) {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 42, 36);
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, sortie);
            document.open();

            Paragraph titreParagraphe = new Paragraph(titre, new Font(Font.HELVETICA, 14, Font.BOLD, ACCENT));
            titreParagraphe.setSpacingAfter(12);
            document.add(titreParagraphe);

            PdfPTable table = new PdfPTable(entetes.size());
            table.setWidthPercentage(100);

            Font policeEntete = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            for (String entete : entetes) {
                PdfPCell cellule = new PdfPCell(new Phrase(entete, policeEntete));
                cellule.setBackgroundColor(ACCENT);
                cellule.setPadding(5);
                table.addCell(cellule);
            }

            Font policeCellule = new Font(Font.HELVETICA, 8);
            for (List<String> ligne : lignes) {
                for (String valeur : ligne) {
                    PdfPCell cellule = new PdfPCell(new Phrase(valeur == null ? "" : valeur, policeCellule));
                    cellule.setPadding(4);
                    table.addCell(cellule);
                }
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new BusinessException("Échec de la génération du PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return sortie.toByteArray();
    }
}
