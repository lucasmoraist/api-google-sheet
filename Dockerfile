FROM maven:3.8.7-eclipse-temurin-19-alpine AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:19-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY src/main/resources/google-credentials-iam.json /app/google-credentials-iam.json

ENV MAIL_USERNAME=${MAIL_USERNAME}
ENV MAIL_PASSWORD=${MAIL_PASSWORD}
ENV SPREADSHEET_ID=${SPREADSHEET_ID}
ENV CREDENTIALS_PATH=${CREDENTIALS_PATH}

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]