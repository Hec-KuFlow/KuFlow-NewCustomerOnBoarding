/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.Activity;

import org.springframework.stereotype.Service;
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
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AppScriptActivitiesImpl implements AppScriptActivities {
    private static final String APPLICATION_NAME = "Apps Script API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
    Arrays.asList("https://www.googleapis.com/auth/drive.scripts  https://www.googleapis.com/auth/script.projects https://www.googleapis.com/auth/script.processes  https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.scripts");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    //Define some own variables
    private static final String SCRIPT_ID = "IMPLEMENTATION_SCRIPT_ID";
    private static final String FUNCTION_NAME = "YOUR_FUNCTION_NAME";  
    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
        throws IOException {
      // Load client secrets.
      InputStream in = AppScriptActivitiesImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
      }
      GoogleClientSecrets clientSecrets =
          GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
  
      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
          .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
          .setAccessType("offline")
          .build();
      LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
      return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
  
    @Override
    public String appScriptRun(String client, String project) {
      try{
      // Build a new authorized API client service.
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Script service =
          new Script.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
              .setApplicationName(APPLICATION_NAME)
              .build();
    
      // Initialize parameters for that function.
      String year = Year.now().toString();
      List<Object> params = new ArrayList<Object>();
      params.add(client);
      params.add(project);
      params.add(year);
  
      // Create execution request.
      ExecutionRequest request = new ExecutionRequest()
              .setFunction(FUNCTION_NAME)
              .setParameters(params);
  
      Operation response = service.scripts().run(SCRIPT_ID, request).execute();
  
      // Check if the response has a result
      if (response.getResponse() != null) {
        System.out.println("Result: " + response.getResponse());
      }
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Error: " + e.getMessage());
    }
      return client;
      
  }

  }
