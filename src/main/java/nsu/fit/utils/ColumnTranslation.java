package nsu.fit.utils;

import java.util.*;

public enum ColumnTranslation {
    LIBRARY_CARD_NUMBER("LibraryCardNumber", "Номер читательского билета"),
    SURNAME("Surname", "Фамилия"),
    NAME("Name", "Имя"),
    PATRONYMIC("Patronymic", "Отчество"),
    LIBRARIAN_ID("LibrarianID", "ID Библиотекаря"),
    PUBLICATION_NOMENCLATURE_NUMBER("PublicationNomenclatureNumber", "Номенклатурный номер издания"),
    TITLE("Title", "Название"),
    DAYS_OVERDUE("DaysOverdue", "Дней просрочки"),
    LITERARY_WORK_ID("LiteraryWorkID", "ID произведения"),
    AUTHOR("Author", "Автор"),
    READERS_BORROW_COUNT("ReadersBorrowCount", "Кол-во читателей, бравших произведение"),
    ISSUE_DATE("IssueDate", "Дата выдачи"),
    LITERARY_WORK_TITLE("LiteraryWorkTitle", "Название произведения"),
    PUBLICATION_TITLE("PublicationTitle", "Название издания"),
    RECEIPT_DATE("ReceiptDate", "Дата поступления"),
    WRITE_OFF_DATE("WriteOffDate", "Дата списания"),
    WRITING_YEAR("WritingYear", "Год написания"),
    CATEGORY("Category", "Категория"),
    NOMENCLATURE_NUMBER("NomenclatureNumber", "Номенклатурный номер"),
    PUBLISHER("Publisher","Издательство"),
    STATE("State", "Состояние"),
    READER_NAME("ReaderName", "Имя"),
    CATEGORY_NAME("CategoryName", "Категория"),
    BIRTHDAY("BirthDay", "Дата рождения");

    private static final Map<String, String> MAP = new HashMap<>();

    static {
        for (ColumnTranslation column : values()) {
            MAP.put(column.englishName, column.russianName);
        }
    }

    private final String englishName;
    private final String russianName;

    ColumnTranslation(String englishName, String russianName) {
        this.englishName = englishName;
        this.russianName = russianName;
    }

    public static String translate(String columnName) {
        return MAP.getOrDefault(columnName, formatColumnName(columnName));
    }

    public static List<Map<String, Object>> formatColumnNames(List<Map<String, Object>> data) {
        if (data.isEmpty()) return data;

        List<Map<String, Object>> formattedData = new ArrayList<>();

        for (Map<String, Object> row : data) {
            Map<String, Object> formattedRow = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String formattedKey = translate(entry.getKey());
                formattedRow.put(formattedKey, entry.getValue());
            }

            formattedData.add(formattedRow);
        }

        return formattedData;
    }

    private static String formatColumnName(String columnName) {
        String spaced = columnName.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("_", " ");
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1).toLowerCase();
    }
}
