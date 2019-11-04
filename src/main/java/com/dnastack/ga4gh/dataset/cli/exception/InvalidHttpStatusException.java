package com.dnastack.ga4gh.dataset.cli.exception;

import lombok.Getter;

@Getter
public class InvalidHttpStatusException extends RuntimeException {

    private int code;

    public InvalidHttpStatusException(String message, int code) {
        super(message);
    }
}
