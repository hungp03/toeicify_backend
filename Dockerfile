FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and install dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy source code and build the jar
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
# copy from stage build
COPY --from=build /app/target/toeic.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
