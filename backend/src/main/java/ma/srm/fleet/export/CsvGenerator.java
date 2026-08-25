package ma.srm.fleet.export;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Génère un CSV en UTF-8 précédé d'un BOM (pour qu'Excel affiche correctement les accents),
 * séparateur point-virgule (convention Excel francophone) et échappement RFC 4180.
 */
final class CsvGenerator {

    private static final String SEPARATEUR = ";";
    private static final String FIN_LIGNE = "\r\n";

    private CsvGenerator() {}

    static byte[] generer(List<String> entetes, List<List<String>> lignes) {
        StringBuilder sb = new StringBuilder("﻿");   // BOM UTF-8
        ecrireLigne(sb, entetes);
        for (List<String> ligne : lignes) {
            ecrireLigne(sb, ligne);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void ecrireLigne(StringBuilder sb, List<String> valeurs) {
        sb.append(valeurs.stream().map(CsvGenerator::echapper).collect(Collectors.joining(SEPARATEUR)));
        sb.append(FIN_LIGNE);
    }

    private static String echapper(String valeur) {
        if (valeur == null) {
            return "";
        }
        boolean aProteger = valeur.contains(SEPARATEUR) || valeur.contains("\"")
            || valeur.contains("\n") || valeur.contains("\r");
        String v = valeur.replace("\"", "\"\"");
        return aProteger ? "\"" + v + "\"" : v;
    }
}
