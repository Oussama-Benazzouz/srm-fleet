package ma.srm.fleet.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import ma.srm.fleet.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service de generation et de lecture des jetons JWT (JSON Web Token).
 *
 * Centralise toute la logique cryptographique liee a l'authentification
 * stateless : un jeton signe est emis a la connexion, puis renvoye par le
 * client a chaque requete pour prouver son identite, sans que le serveur
 * n'ait besoin de maintenir une session en memoire.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * @param secret        cle secrete de signature, lue depuis la
     *                       configuration (app.jwt.secret) plutot que codee
     *                       en dur, pour rester coherent avec la separation
     *                       configuration/code deja appliquee au reste du
     *                       projet (datasource, mots de passe...).
     * @param expirationMs   duree de validite du jeton, en millisecondes,
     *                       egalement externalisee dans la configuration.
     *
     * Keys.hmacShaKeyFor(...) derive une cle HMAC-SHA a partir du secret
     * fourni ; la meme cle sert a la fois a signer (generateToken) et a
     * verifier (parse) les jetons.
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret doit faire au moins 256 bits (32 octets)");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Genere un nouveau jeton JWT signe pour l'utilisateur authentifie.
     *
     * Le jeton embarque, en plus du sujet (login), trois informations
     * necessaires aux controles d'acces du reste de l'application :
     * - userId    : identifiant technique de l'utilisateur ;
     * - role      : role (administrateur_central / responsable_provincial),
     *               utilise par les regles d'autorisation (RG12-RG14) ;
     * - provinceId: perimetre geographique du Responsable Provincial,
     *               utilise pour le filtrage par province ; null pour
     *               l'Administrateur Central (portee nationale).
     *
     * Ces claims evitent d'avoir a rechercher l'utilisateur en base a
     * chaque requete pour connaitre son role ou sa province : ils sont
     * lus directement depuis le jeton, deja verifie et non falsifiable
     * grace a la signature.
     *
     * @param user le principal de l'utilisateur qui vient de s'authentifier
     * @return le jeton JWT signe, encode en chaine compacte (format standard
     *         JWT : header.payload.signature)
     */
    public String generateToken(UserPrincipal user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole().name())
            .claim("provinceId", user.getProvinceId())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Extrait le login (subject) contenu dans un jeton JWT valide.
     *
     * Utilise typiquement par le filtre d'authentification pour retrouver
     * l'utilisateur correspondant a chaque requete entrante, a partir du
     * jeton fourni dans l'en-tete Authorization.
     *
     * @param token le jeton JWT recu du client
     * @return le login de l'utilisateur associe a ce jeton
     * @throws io.jsonwebtoken.JwtException si le jeton est invalide, expire
     *         ou a ete altere (signature incorrecte) — a intercepter dans
     *         le filtre de securite pour rejeter proprement la requete
     *         plutot que de laisser remonter une exception non geree.
     */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * Verifie la signature du jeton et decode son contenu (claims).
     *
     * verifyWith(key) : rejette tout jeton dont la signature ne correspond
     * pas a la cle du serveur — c'est ce qui garantit qu'un jeton ne peut
     * pas etre fabrique ou modifie par un client sans connaitre le secret.
     *
     * @param token le jeton JWT brut recu du client
     * @return les claims (donnees) contenues dans le jeton, une fois la
     *         signature validee
     */
    private Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
