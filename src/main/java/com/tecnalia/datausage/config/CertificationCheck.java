package com.tecnalia.datausage.config;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.tecnalia.datausage.Swagger2SpringBoot;

@Configuration
public class CertificationCheck {

	private static final Logger logger = LoggerFactory.getLogger(CertificationCheck.class);

	private static final String[] CERTIFIED_VERSION = { "1.7.4" };
	@PostConstruct
	public void checkIfVerionsIsCertified() {
		String version = Objects.requireNonNullElse(Swagger2SpringBoot.class.getPackage().getImplementationVersion(), "");
		logger.info("Certified version: " + 
				 Arrays.stream(CERTIFIED_VERSION).anyMatch(version::equals));
	}
}
