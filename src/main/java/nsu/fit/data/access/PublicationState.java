package nsu.fit.data.access;

public enum PublicationState {
    ISSUED("Выдано"),
    WRITTEN_OFF("Списано"),
    AVAILABLE("В наличии");

    public final String russianName;

    PublicationState(String russianName) {
        this.russianName = russianName;
    }

    @Override
    public String toString() {
        return russianName;
    }

    public static PublicationState fromRussianName(String value) {
        for (PublicationState state : values()) {
            if (state.russianName.equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Неизвестное значение: " + value);
    }
}
