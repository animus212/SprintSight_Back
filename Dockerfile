FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre AS extract

WORKDIR /workspace
COPY --from=build /workspace/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre

RUN groupadd --system --gid 1001 sprintsight \
 && useradd  --system --uid 1001 --gid sprintsight --shell /bin/false sprintsight

WORKDIR /app

COPY --from=extract --chown=sprintsight:sprintsight /workspace/dependencies/ ./
COPY --from=extract --chown=sprintsight:sprintsight /workspace/spring-boot-loader/ ./
COPY --from=extract --chown=sprintsight:sprintsight /workspace/snapshot-dependencies/ ./
COPY --from=extract --chown=sprintsight:sprintsight /workspace/application/ ./

USER sprintsight

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Duser.timezone=UTC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
