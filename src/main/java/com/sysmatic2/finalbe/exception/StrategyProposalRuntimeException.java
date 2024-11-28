package com.sysmatic2.finalbe.exception;

import java.util.Map;

public class StrategyProposalRuntimeException extends RuntimeException {
    private final Map<String, Object> response;

    public StrategyProposalRuntimeException(Map<String, Object> response) {
        super(response.get("message").toString());
        this.response = response;
    }

    public Map<String, Object> getResponse() {
        return response;
    }
}
