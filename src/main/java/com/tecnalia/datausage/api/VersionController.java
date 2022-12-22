package com.tecnalia.datausage.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/about" })
public class VersionController {

	@Autowired
	BuildProperties buildProperties;

	@GetMapping("/version")
	@ResponseBody
	public String getVersion() {
		return buildProperties.getVersion();
	}
}
