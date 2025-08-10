#FROM maven:3.9.6-eclipse-temurin-21 AS build
#WORKDIR /app
#
## Copy pom.xml and install dependencies
#COPY pom.xml .
#RUN mvn -B -q dependency:go-offline
#
## Copy source code and build the jar
#COPY src ./src
#RUN mvn -B -DskipTests clean package
#
#FROM eclipse-temurin:21-jre
#WORKDIR /app
## copy from stage build
#COPY --from=build /app/target/toeic.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]


# ===== Stage 1: Build JAR =====
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build JAR
COPY src ./src
RUN mvn -B -DskipTests clean package

# ===== Stage 2: Lite runtime =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/toeic.jar app.jar

# JVM options
ENV JAVA_OPTS="-Xmx256m -Xms64m -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
