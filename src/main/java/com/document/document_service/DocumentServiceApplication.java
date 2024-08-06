package com.document.document_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentServiceApplication.class, args);
	}

}
// upload ve download endpointlerini geliştir
// - upload ederken belirli dosya formatlarını kullansın
// - upload ve download için işlem sınırı koy (session tutarak)
