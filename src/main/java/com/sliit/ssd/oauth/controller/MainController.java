package com.sliit.ssd.oauth.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.sliit.ssd.oauth.model.FileModel;
import com.sliit.ssd.oauth.service.GoogleDriveService;

@Controller
public class MainController {

	@Autowired
	GoogleDriveService googleDriveService;

	private static Logger logger = LoggerFactory.getLogger(MainController.class);

	@GetMapping("/")
	public String showIndex() throws IOException {
		return googleDriveService.isUserAuthenticated() ? "home.html" : "index.html";
	}

	@GetMapping("/googlesignin")
	public void goGoogleSignIn(HttpServletResponse response) throws IOException {
		googleDriveService.googleSignIn(response);
	}

	@GetMapping("/oauth")
	public String storeCredentialsFromGoogle(HttpServletRequest request) throws IOException {
		return googleDriveService.isStoreAuthorizationCode(request) ? "home.html" : "index.html";
	}

	@PostMapping("/upload")
	public String uploadFile(HttpServletRequest servletRequest, @ModelAttribute FileModel file)
			throws IllegalStateException, IOException {
		googleDriveService.uploadFileToDrive(file.getMultipartFile());
		return "home.html";
	}

	@GetMapping("/logout")
	public String getLogoutPage(HttpServletRequest request) throws IOException {
		googleDriveService.logout(request);
		return "index.html/";
	}

}
