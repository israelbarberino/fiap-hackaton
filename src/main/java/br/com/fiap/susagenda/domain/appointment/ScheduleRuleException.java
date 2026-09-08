package br.com.fiap.susagenda.domain.appointment;

public class ScheduleRuleException extends RuntimeException {
    private final String code;

    public ScheduleRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
