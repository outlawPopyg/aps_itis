package com.example.demo.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadModel {
    @JsonProperty("model_name")
    private String modelName;

    private Args args = new Args();

    private Settings settings = new Settings();

    @Data
    static class Args {
        @JsonProperty("n_gpu_layers")
        private int gpuLayers = 256;

        @JsonProperty("threads")
        private int threads = 12;
    }

    @Data
    static class Settings {
        @JsonProperty("instruction_template")
        private String instructionTemplate = "Alpaca";
    }
}
