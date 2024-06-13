FROM maven:3.8.1-jdk-8-slim as builder

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

CMD ["javar","-jar","/app/target/Partner_Matching-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
