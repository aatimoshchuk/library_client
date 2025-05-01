package nsu.fit.utils.warning;

public enum TriggerExceptionMessage {

    SCHOOLCHILD_SUBSCRIPTION_EXPIRED("the schoolchild subscription renewal period has expired"),
    STUDENT_SUBSCRIPTION_EXPIRED("the student subscription renewal period has expired"),
    AGE_RESTRICTION_EXCEPTION("reader does not meet the age restriction"),
    MISSING_CATEGORY("selected publication is not available for readers without category"),
    INCORRECT_CATEGORY("selected publication is not available for this category of readers"),
    REDEFINING_READER_CATEGORY("reader already belongs to one of the categories"),
    WRITTEN_OFF_PUBLICATION_CHANGING_LOCATION("field StorageLocationID cannot be changed while publication is written" +
            " off"),
    DATE_IN_THE_FUTURE("date cannot be in the future"),
    OUT_OF_STOCK_PUBLICATION("publication is out of stock"),
    WRITTEN_OFF_PUBLICATION("publication has already been written off"),
    NOMENCLATURE_NUMBER_CHANGING_ATTEMPT("field PublicationNomenclatureNumber cannot be changed");

    private final String message;

    TriggerExceptionMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
