package com.example.demo.models.dto;

import lombok.Data;

@Data
public class OpenChatModelDTO {
	private String id = "openchat_3.6";
	private int maxLength = 24576;
	private String name = "OpenChat 3.6 (latest)";
	private int tokenLimit = 8192;
}
