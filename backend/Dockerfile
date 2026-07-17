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
USER srm

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]