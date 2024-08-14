package com.lucasmoraist.register_telecentro.infra.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
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

    @Value("${google.sheets.tokens.directory.path}")
    private String tokensDirectoryPath;

    @Value("${google.sheets.client.project.id}")
    private String projectId;
    @Value("${google.sheets.client.auth.uri}")
    private String authUri;
    @Value("${google.sheets.client.token.uri}")
    private String tokenUri;
    @Value("${google.sheets.client.auth.provider}")
    private String authProvider;
    @Value("${google.sheets.client.id}")
    private String clientId;
    @Value("${google.sheets.client.secret}")
    private String clientSecret;
    @Value("${google.sheets.client.redirect.uri}")
    private String redirectUri;

    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    @Bean
    public Sheets service() throws IOException, GeneralSecurityException {

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport);

        String applicationName = "Register Telecentro";
        return new Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {

        InputStream in = getInputStream();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }

    private InputStream getInputStream() {
        if (clientId == null || clientSecret == null) throw new IllegalArgumentException("Environment variables not found");

        String jsonCredentials = """
                {
                    "web":{
                        "client_id":"%s",
                        "project_id":"%s",
                        "auth_uri":"%s",
                        "token_uri":"%s",
                        "auth_provider_x509_cert_url":"%s",
                        "client_secret":"%s",
                        "redirect_uris":["%s"]
                        }
                }
                """.formatted(clientId, projectId, authUri, tokenUri, authProvider, clientSecret, redirectUri);

        return new ByteArrayInputStream(jsonCredentials.getBytes());
    }


}
