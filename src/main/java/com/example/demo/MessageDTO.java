package com.example.demo;

import lombok.Data;

@Data
public class MessageDTO {
    private String role = "user";
    private String content;

    public MessageDTO(String content) {
        this.content = content;
    }
}
