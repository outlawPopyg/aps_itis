package com.example.demo.controller;

import com.example.demo.dao.ParameterDao;
import com.example.demo.dao.ParametersRepository;
import com.example.demo.enums.ParametersEnum;
import com.example.demo.models.Parameters;
import com.example.demo.models.dto.OpenChatPromptDTO;
import com.example.demo.models.dto.ParameterDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class BaseController {

    @Value("${ai.url}")
    private String aiUrl;

    private final ParameterDao parameterDao;
    private final RestTemplate restTemplate;
    private final ParametersRepository parametersRepository;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("context", ParametersEnum.CONTEXT.getParameterValue());
        model.addAttribute("fields", ParametersEnum.FIELDS.getParameterValue());

        return "index";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("context", ParametersEnum.CONTEXT.getParameterValue().getChildren());
        model.addAttribute("fields", ParametersEnum.FIELDS.getParameterValue().getChildren());
        return "settings";
    }

    @PostMapping("/settings/fields")
    @Transactional
    public String saveFields(@RequestBody List<ParameterDTO> requst) {
        for (ParameterDTO parameterDTO : requst) {
            if (StringUtils.isBlank(parameterDTO.getCode())) {
                Parameters parameters = new Parameters();
                parameters.setCode(parameterDTO.getName().toUpperCase());
                parameters.setName(parameterDTO.getName());
                parameters.setParent(parametersRepository.getReferenceById(2L));
                parameters.setDescription(parameterDTO.getDescription());
                parameters.setExampleRequest(parameterDTO.getExampleRequest());
                parameters.setExampleResponse(parameterDTO.getExampleResponse());

                parametersRepository.save(parameters);
            }
        }

        return "settings";
    }

    @PostMapping("/settings/context")
    @Transactional
    public String saveContext(@RequestBody List<ParameterDTO> requst) {
        for (ParameterDTO parameterDTO : requst) {
            if (StringUtils.isBlank(parameterDTO.getCode())) {
                Parameters parameters = new Parameters();
                parameters.setCode(parameterDTO.getName().toUpperCase());
                parameters.setName(parameterDTO.getName());
                parameters.setParent(parametersRepository.getReferenceById(1L));
                parameters.setDescription(parameterDTO.getDescription());

                parametersRepository.save(parameters);
            }
        }

        return "settings";
    }

    @PostMapping
    @Transactional
    public String post(@RequestParam("context") String context,
                       @RequestParam("fields") List<String> fields,
                       Model model) {

        List<ParameterDTO> fieldParams = fields.stream()
                .map(parameterDao::getParameterByCode)
                .map(ParameterDTO::new)
                .sorted(Comparator.comparing(ParameterDTO::getName))
                .toList();

        String prompt = """
                Извлеки сущности из текста. Формат ответа: JSON с ключами {STRUCTURE}.
                
                {EXAMPLES}
                
                Теперь обработай текст:
                Текст: {TEXT}
                Ответ:
                """;

        String structure = fieldParams.stream()
                .map(p -> String.format("\"%s\" (%s)", p.getName(), p.getDescription()))
                .collect(Collectors.joining(", "));

        String exampleTemplate = "Пример:\nТекст: {EXAMPLE_TEXT}\nОтвет:\n{EXAMPLE_RESPONSE}";
        String fetchedText = fieldParams.stream().map(ParameterDTO::getExampleRequest).collect(Collectors.joining(". "));

        StringBuilder exampleResponse = new StringBuilder();
        exampleResponse.append("{\n");
        fieldParams.forEach(field ->
                exampleResponse.append(String.format("\"%s\": \"%s\",\n", field.getName(), field.getExampleResponse())));
        exampleResponse.deleteCharAt(exampleResponse.lastIndexOf(",\n"));
        exampleResponse.append("}");

        prompt = prompt
                .replace("{STRUCTURE}", structure)
                .replace("{EXAMPLES}", ParametersEnum.EXAMPLES.getParameterValue().getDescription());


        Pattern pattern = Pattern.compile("\\{(?:[^{}]|\\{(?:[^{}]|\\{[^{}]*})*})*}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(ParametersEnum.EXAMPLES.getParameterValue().getDescription());
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();

        boolean exists = false;
        while (matcher.find()) {
            String group = matcher.group().replaceAll("\n", "");
            Integer distance = levenshtein.apply(exampleResponse.toString().toLowerCase().replaceAll("\n", ""),
                    group.toLowerCase());
            if (distance < 3) {
                exists = true;
                break;
            }
        }

        String exampleToSave = exampleResponse.toString();
        if (!exists) {
            exampleToSave = exampleTemplate
                    .replace("{EXAMPLE_TEXT}", fetchedText)
                    .replace("{EXAMPLE_RESPONSE}", exampleResponse);

            prompt = prompt.replace("{EXAMPLES}", exampleToSave);

            exampleToSave = ParametersEnum.EXAMPLES.getParameterValue().getDescription().replace("{EXAMPLES}", exampleToSave);
            exampleToSave += "\n{EXAMPLES}\n";
            parametersRepository.getReferenceById(ParametersEnum.EXAMPLES.getParameterValue().getId())
                    .setDescription(exampleToSave);
        }



        String serializedFields = fieldParams.stream().map(ParameterDTO::getName).collect(Collectors.joining(","));

        model.addAttribute("prompt", prompt);
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

        System.out.println(substring);


        return substring;
    }

}
