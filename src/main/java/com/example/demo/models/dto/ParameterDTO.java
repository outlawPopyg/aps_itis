package com.example.demo.models.dto;

import com.example.demo.models.Parameters;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterDTO {
	private Long id;
	private String name;
	private String description;
	private String code;
	private String exampleRequest;
	private String exampleResponse;

	private ParameterDTO parent;
	private List<ParameterDTO> children;

	public ParameterDTO(Parameters parameters) {
		this.id = parameters.getId();
		this.name = parameters.getName();
		this.description = parameters.getDescription();
		this.code = parameters.getCode();
		this.exampleRequest = parameters.getExampleRequest();
		this.exampleResponse = parameters.getExampleResponse();
		if (!parameters.getChildren().isEmpty()) {
			this.children = parameters.getChildren().stream().map(ParameterDTO::new).toList();
		}

	}
}
