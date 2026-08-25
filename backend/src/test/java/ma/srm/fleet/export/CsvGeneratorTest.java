package ma.srm.fleet.export;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvGeneratorTest {

    @Test
    void echappe_separateurs_et_guillemets() {
        byte[] octets = CsvGenerator.generer(
            List.of("Col A", "Col B"),
            List.of(List.of("x;y", "il a dit \"bonjour\"")));

        String csv = new String(octets, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"x;y\"");                       // séparateur protégé
        assertThat(csv).contains("\"il a dit \"\"bonjour\"\"\"");   // guillemets doublés
        assertThat(csv).startsWith("﻿");                            // BOM UTF-8 présent
    }
}
