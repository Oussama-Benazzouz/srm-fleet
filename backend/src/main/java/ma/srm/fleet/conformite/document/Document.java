package ma.srm.fleet.conformite.document;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.srm.fleet.common.audit.AuditableEntity;
import ma.srm.fleet.conformite.sinistre.Sinistre;

@Entity
@Table(name = "document")
@Getter @Setter @NoArgsConstructor
public class Document extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Long id;

    @Column(name = "nom_fichier_document", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "type_document", length = 50)
    private String typeDocument;

    @Column(name = "chemin_stockage_document", nullable = false, length = 500)
    private String cheminStockage;

    @Column(name = "date_upload_document", nullable = false)
    private LocalDateTime dateUpload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sinistre", nullable = false)
    private Sinistre sinistre;
}