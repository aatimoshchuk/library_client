package nsu.fit.utils.warning;

import lombok.Getter;

@Getter
public enum SqlState {
    FOREIGN_KEY_MISSING("23503"),
    DUPLICATE_KEY_VALUE("23505"),
    TRIGGER_EXCEPTION("P0001"),
    CONSTRAINT_VIOLATION("23514"),
    INVALID_DATE("22007"),
    INVALID_DATE_FORMAT("22008");

    private final String code;

    SqlState(String code) {
        this.code = code;
    }
}
