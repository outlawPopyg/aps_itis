package com.example.demo.models.dto;

import com.example.demo.models.Parameters;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ParameterDTO {
	private Long id;
	public String name;
	private String description;
	private String code;
	private ParameterDTO parent;
	private List<ParameterDTO> children;

	public ParameterDTO(Parameters parameters) {
		this.id = parameters.getId();
		this.name = parameters.getName();
		this.description = parameters.getDescription();
		this.code = parameters.getCode();
		if (!parameters.getChildren().isEmpty()) {
			this.children = parameters.getChildren().stream().map(ParameterDTO::new).toList();
		}

	}
}
