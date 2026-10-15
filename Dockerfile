# Mehrstufiger Build (F-043 S8): Maven-Build im JDK-Image, Laufzeit im schlanken JRE-Image.
# Architektur folgt dem Build-Rechner – auf Apple Silicon entsteht arm64, passend zur EC2 t4g (Graviton).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -q -B -DskipTests package && cp target/brennholzdemo-*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /src/app.jar app.jar
USER app
EXPOSE 8080
# Heap relativ zum Container-Limit (docker-compose.prod.yml)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-Djava.awt.headless=true", "-jar", "/app/app.jar"]
