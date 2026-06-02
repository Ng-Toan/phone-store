package com.ngtoan.phone_store.dto.response;

import java.util.List;

public class ChatbotPolicyResponseDTO {

    private String answer;
    private List<String> sources;

    public ChatbotPolicyResponseDTO() {
    }

    public ChatbotPolicyResponseDTO(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}