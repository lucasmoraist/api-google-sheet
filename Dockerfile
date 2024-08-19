FROM maven:3.8.7-eclipse-temurin-19-alpine AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:19-alpine

WORKDIR /app

RUN apk add --no-cache bash curl tar python3 \
    && python3 -m ensurepip \
    && pip3 install --no-cache --upgrade pip setuptools

RUN curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz \
    && tar -xf google-cloud-cli-linux-x86_64.tar.gz \
    && ./google-cloud-sdk/install.sh

ENV GOOGLE_APPLICATION_CREDENTIALS=${GOOGLE_APPLICATION_CREDENTIALS}

RUN echo '$GOOGLE_APPLICATION_CREDENTIALS' > /app/credentials-google.json

RUN ./google-cloud-sdk/bin/gcloud auth activate-service-account --key-file=/app/credentials-google.json

COPY --from=build /app/target/*.jar app.jar

ENV MAIL_USERNAME=${MAIL_USERNAME}
ENV MAIL_PASSWORD=${MAIL_PASSWORD}
ENV SPREADSHEET_ID=${SPREADSHEET_ID}
ENV SECRET_ID=${SECRET_ID}
ENV PROJECT_ID=${PROJECT_ID}
ENV PORT=${PORT}

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]