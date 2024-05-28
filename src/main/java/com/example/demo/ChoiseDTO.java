package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChoiseDTO {
    private int index;

    @JsonProperty("finish_reason")
    private String finishReason;

    private MessageDTO message;
}
