# ---------- Étape 1 : build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier d'abord le pom.xml : le cache des dépendances n'est invalidé
# que si ce fichier change, pas à chaque modification du code source.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Copier le code source seulement ensuite
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ---------- Étape 2 : runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S srm && adduser -S srm -G srm
COPY --from=build /app/target/backend-*.jar app.jar
# Répertoire des pièces jointes détenu par l'utilisateur applicatif : un volume nommé
# initialisé sur ce point de montage hérite de cette propriété (sinon root => écriture refusée).
RUN mkdir -p /app/data/documents && chown -R srm:srm /app/data
USER srm

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]