# ==========================================
# Étape 1 : Build de l'application avec Maven & JDK 21
# ==========================================
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copie du descripteur pom.xml pour mettre en cache les dépendances
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && mvn dependency:go-offline -B

# Copie du code source et compilation
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Étape 2 : Image d'exécution légère (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Création d'un utilisateur système non-root pour des raisons de sécurité
RUN groupadd -r novagroup && useradd -r -g novagroup novauser
USER novauser

# Copie du binaire JAR généré à l'étape précédente
COPY --from=builder --chown=novauser:novagroup /app/target/novamarket-backend-*.jar app.jar

# Exposition du port applicatif
EXPOSE 8080

# Options JVM de production : gestion de la mémoire conteneurisée
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
