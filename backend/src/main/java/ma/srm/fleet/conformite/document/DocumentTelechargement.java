package ma.srm.fleet.conformite.document;

import org.springframework.core.io.Resource;

/** Contenu d'une pièce jointe prêt à être renvoyé au client (ressource + métadonnées d'entête). */
public record DocumentTelechargement(Resource resource, String nomFichier, String typeDocument) {}
