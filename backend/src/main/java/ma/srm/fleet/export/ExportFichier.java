package ma.srm.fleet.export;

/** Fichier d'export prêt à être renvoyé : contenu binaire, type MIME et nom proposé. */
public record ExportFichier(byte[] contenu, String contentType, String nomFichier) {}
