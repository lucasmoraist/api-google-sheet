package com.lucasmoraist.register_telecentro.infra.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
@Slf4j
public class GoogleSheetsConfig {

    @Value("${google.cloud.secret.id}")
    private String secretId;
    @Value("${google.cloud.project.id}")
    private String projectId;

    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Bean
    public Sheets service() throws IOException, GeneralSecurityException {
        log.info("Iniciando configuração do Google Sheets Service.");

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");

            AccessSecretVersionRequest req = AccessSecretVersionRequest.newBuilder()
                    .setName(secretVersionName.toString())
                    .build();

            log.info("Acessando segredo do Secret Manager.");
            String secretData = client.accessSecretVersion(req).getPayload().getData().toStringUtf8();
            log.info("Segredo recuperado com sucesso.");

            try (InputStream credentialsStream = new ByteArrayInputStream(secretData.getBytes())) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(scopes);

                HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

                Sheets sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, requestInitializer)
                        .setApplicationName("Register Telecentro")
                        .build();

                log.info("Google Sheets Service configurado com sucesso.");
                return sheetsService;
            }

        } catch (IOException | GeneralSecurityException e) {
            log.error("Erro ao configurar o Google Sheets Service: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao acessar o Secret Manager ou configurar o Sheets Service: {}", e.getMessage());
            throw new RuntimeException("Erro ao configurar o Google Sheets Service", e);
        }
    }
}
