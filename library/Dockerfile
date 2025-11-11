# ---- Build stage (Maven on JDK 25) ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .

RUN mvn -B -ntp -q -e dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -e -DskipTests=true clean package

# ---- Run stage (JRE 25) ----
FROM eclipse-temurin:25-jre
WORKDIR /digilib


COPY --from=build /workspace/target/digilib.jar /digilib/digilib.jar

EXPOSE 8081
ENV JAVA_OPTS=""
CMD ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8081} -jar /digilib/digilib.jar"]
