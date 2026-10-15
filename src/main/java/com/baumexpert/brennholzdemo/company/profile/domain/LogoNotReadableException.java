package com.baumexpert.brennholzdemo.company.profile.domain;

/**
 * Das hochgeladene Logo ist kein lesbares Bild – ein dauerhafter Fehler, eine Wiederholung hilft nicht.
 */
public class LogoNotReadableException extends RuntimeException {

    public LogoNotReadableException(String message) {
        super(message);
    }

    public LogoNotReadableException(String message, Throwable cause) {
        super(message, cause);
    }
}
