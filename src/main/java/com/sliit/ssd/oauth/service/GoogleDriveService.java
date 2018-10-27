package com.sliit.ssd.oauth.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.sliit.ssd.oauth.util.AppConfig;

@Service
public class GoogleDriveService {

	@Autowired
	private AppConfig appConfig;

	private static Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);

	// Used in google APIs. To make the REST API calls
	private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	// Serialize and de-serialize the responses
	private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	// List of scopes we will be accessing in the Google Drive
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

	// Unique identifier to identify the user (similar to session)
	private static final String USER_IDENTIFIER_KEY = "MY_USER";

	private static final String APPLICATION_NAME = "UploaderSSD";

	private GoogleAuthorizationCodeFlow flow;

	private Drive drive;

	@PostConstruct
	public void init() throws IOException {
		logger.info("Started init...");
		GoogleClientSecrets secret = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(appConfig.getGdSecretKeys().getInputStream()));
		logger.info("Secret fetched...");
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secret, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(appConfig.getCredentialsFolder().getFile())).build();
		drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential()).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public Credential getCredential() throws IOException {
		return flow.loadCredential(USER_IDENTIFIER_KEY);
	}

	public boolean isUserAuthenticated() throws IOException {

		Credential credential = getCredential();
		boolean tokenValid = false;
		if (credential != null) {
			tokenValid = credential.refreshToken();
			return tokenValid;
		}
		return tokenValid;
	}

	public void googleSignIn(HttpServletResponse response) throws IOException {
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectURL = url.setRedirectUri(appConfig.getCALLBACK_URI()).setAccessType("offline").build();
		response.sendRedirect(redirectURL);
	}

	public boolean isStoreAuthorizationCode(HttpServletRequest request) throws IOException {
		String code = request.getParameter("code");

		if (code != null) {
			saveToken(code);
			return true;
		}
		return false;
	}

	private void saveToken(String code) throws IOException {
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(appConfig.getCALLBACK_URI()).execute();
		flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
	}

	public void uploadFileToDrive(MultipartFile multipartFile) throws IllegalStateException, IOException {

		String originalFileName = multipartFile.getOriginalFilename();
		String fileContentType = multipartFile.getContentType();

		String tempPath = appConfig.getTempFolder();

		File copyFile = new File(tempPath, originalFileName);

		multipartFile.transferTo(copyFile);

		com.google.api.services.drive.model.File metaDataFile = new com.google.api.services.drive.model.File();
		metaDataFile.setName(originalFileName);
		FileContent fileContent = new FileContent(fileContentType, copyFile);

		com.google.api.services.drive.model.File verifyFile = drive.files().create(metaDataFile, fileContent)
				.setFields("id").execute();
		logger.info("Created File: "+verifyFile.getId());

	}
	
	public void logout(HttpServletRequest request){
		HttpSession session = request.getSession(false);
		session = request.getSession(true);
		if (session != null) {
			session.invalidate();
			logger.info("Logged Out...");
		}
	}
}
