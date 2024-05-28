package com.example.demo.models.dto;

import com.example.demo.MessageDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpenChatPromptDTO {
    private List<MessageDTO> messages = new ArrayList<>();

    @JsonProperty("instruction_template")
    private String instructionTemplate = "Alpaca";

    private String mode = "instruct";

    private double temperature = 0.7;

    public static OpenChatPromptDTO createPrompt(String prompt) {
        OpenChatPromptDTO openChatPromptDTO = new OpenChatPromptDTO();
        openChatPromptDTO.getMessages().add(new MessageDTO(prompt));

        return openChatPromptDTO;
    }
}
