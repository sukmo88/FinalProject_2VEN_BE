package com.sysmatic2.finalbe.exception;

public class StrategyProposalSaveException extends RuntimeException{
    public StrategyProposalSaveException(String message) {super(message);}

    public StrategyProposalSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
