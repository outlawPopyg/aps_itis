package com.example.demo.controller;

import com.example.demo.dao.ParameterDao;
import com.example.demo.enums.ParametersEnum;
import com.example.demo.models.dto.OpenChatPromptDTO;
import com.example.demo.models.dto.ParameterDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class BaseController {

	@Value("${ai.url}")
	private String aiUrl;

	private final ParameterDao parameterDao;
	private final RestTemplate restTemplate;

	@GetMapping
	public String home(Model model) {
		model.addAttribute("context", ParametersEnum.CONTEXT.getParameterValue());
		model.addAttribute("fields", ParametersEnum.FIELDS.getParameterValue());

		return "index";
	}

	@PostMapping
	@Transactional
	public String post(@RequestParam("context") String context,
	                   @RequestParam("fields") List<String> fields,
	                   Model model) {

		List<ParameterDTO> fieldParams = fields.stream()
				.map(parameterDao::getParameterByCode)
				.map(ParameterDTO::new)
				.toList();
		ParameterDTO contextParameter = new ParameterDTO(parameterDao.getParameterByCode(context));
		StringBuilder builder = new StringBuilder();
		builder.append(contextParameter.getDescription());
		builder.append('\n');
		builder.append("Сформируй ответ в формате JSON со следующей структурой: \n");

		builder.append("{ ");
		fieldParams.forEach(field -> {
			builder.append(String.format("'%s': '<%s>', ", field.getName(), field.getDescription()));
		});
		builder.deleteCharAt(builder.lastIndexOf(","));
		builder.append("}");

		String serializedFields = fieldParams.stream().map(ParameterDTO::getName).collect(Collectors.joining(","));

		model.addAttribute("prompt", builder.toString());
		model.addAttribute("fields", fieldParams);
		model.addAttribute("serializedFields", serializedFields);

		return "transcribe";
	}

	@PostMapping("/gpt")
	@ResponseBody
	public String gptRequest(@RequestBody String prompt) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.TEXT_PLAIN));

		OpenChatPromptDTO openChatPrompt = OpenChatPromptDTO.createPrompt(prompt);

		ResponseEntity<JsonNode> response = restTemplate
				.exchange(aiUrl, HttpMethod.POST, new HttpEntity<>(openChatPrompt, headers), JsonNode.class);

		String text = Objects.requireNonNull(response.getBody())
				.get("choices")
				.get(0)
				.get("message")
				.get("content")
				.asText();

		String substring = text.substring(text.indexOf('{'), text.lastIndexOf('}') + 1)
				.replaceAll("\\n", "");


		return substring;
	}

}
