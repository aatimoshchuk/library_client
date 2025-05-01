package nsu.fit.utils.warning;

public enum WarningType {
    VALIDATION_ERROR("Ошибка валидации"),
    SAVING_ERROR("Невозможно выполнить сохранение"),
    SERVER_EXCEPTION("Ошибка сервера"),
    WARNING("Предупреждение");

    private final String type;

    WarningType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
