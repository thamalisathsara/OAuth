package com.sliit.ssd.oauth.model;

import org.springframework.web.multipart.MultipartFile;

public class FileModel {
	
	private static final long serialVersionUID = 1L;
	private MultipartFile multipartFile;

	public MultipartFile getMultipartFile() {
		return multipartFile;
	}

	public void setMultipartFile(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
