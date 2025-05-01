package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.HistoryEntry;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.TriggerExceptionMessage;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HistoryEntryRepository extends AbstractEntityRepository<HistoryEntry> {

    private final JdbcTemplate jdbcTemplate;

    public Warning markPublicationAsReturned(int publicationNomenclatureNumber) {
        try {
            jdbcTemplate.update("UPDATE \"HistoryOfIssueOfPublications\" SET \"ReturnDate\" = CURRENT_DATE WHERE " +
                            "\"PublicationNomenclatureNumber\" = ?",
                    publicationNomenclatureNumber);
        } catch (Exception e) {
            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    public int getNumberOfDaysOverdue(int publicationNomenclatureNumber) {
        return jdbcTemplate.queryForObject(
                "SELECT \"getNumberOfDaysOverdue\"(?, CURRENT_DATE)",
                Integer.class,
                publicationNomenclatureNumber);
    }

    @Override
    public List<HistoryEntry> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"HistoryOfIssueOfPublications\"",
                (rs, rowNum) -> new HistoryEntry(
                        rs.getInt("IssuedPublicationID"),
                        rs.getInt("PublicationNomenclatureNumber"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getDate("IssueDate").toString(),
                        (rs.getDate("ReturnDate") != null) ? rs.getDate("ReturnDate").toString() : null,
                        rs.getInt("LibrarianID"))
        );
    }

    @Override
    public Warning saveEntity(HistoryEntry entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номенклатурный номер издания\", " +
                    "\"Номер читательского билета\" и \"ID библиотекаря\" должны представлять из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля \"Номенклатурный номер издания\", \"Номер читательского " +
                    "билета\" и \"Дата выдачи\" не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"HistoryOfIssueOfPublications\" SET \"PublicationNomenclatureNumber\" = ?, " +
                                "\"LibraryCardNumber\" = ?, \"IssueDate\" = TO_DATE(?, 'YYYY-MM-DD')," +
                                "\"ReturnDate\" = TO_DATE(?, 'YYYY-MM-DD'), \"LibrarianID\" = ? " +
                                "WHERE \"IssuedPublicationID\" = ?",
                        entity.getPublicationNomenclatureNumber(),
                        entity.getLibraryCardNumber(),
                        entity.getIssueDate(),
                        (entity.getReturnDate() != null && entity.getReturnDate().isEmpty()) ? null :
                                entity.getReturnDate(),
                        (entity.getLibrarianID() != null && entity.getLibrarianID() == 0) ? null :
                                entity.getLibrarianID(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"HistoryOfIssueOfPublications\" (\"PublicationNomenclatureNumber\", " +
                                "\"LibraryCardNumber\", \"IssueDate\", \"ReturnDate\", \"LibrarianID\")" +
                                " VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?)",
                        entity.getPublicationNomenclatureNumber(),
                        entity.getLibraryCardNumber(),
                        entity.getIssueDate(),
                        (entity.getReturnDate() != null && entity.getReturnDate().isEmpty()) ? null :
                                entity.getReturnDate(),
                        (entity.getLibrarianID() != null && entity.getLibrarianID() == 0) ? null :
                                entity.getLibrarianID()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode())) {
                    if (sqlEx.getMessage().contains("PublicationNomenclatureNumber")) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание с таким номенклатурным номером не существует.");
                    }

                    if (sqlEx.getMessage().contains("LibrarianID")) {
                        return new Warning(WarningType.SAVING_ERROR, "Библиотекарь с таким ID не существует.");
                    }

                    if (sqlEx.getMessage().contains("LibraryCardNumber")) {
                        return new Warning(WarningType.SAVING_ERROR, "Читатель с таким номером читательского билета " +
                                "не существует.");
                    }
                }

                if (sqlEx.getSQLState().equals(SqlState.TRIGGER_EXCEPTION.getCode())) {
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.MISSING_CATEGORY.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание недоступно для выдачи читателям, не относящимся " +
                                "ни к одной из категорий.");
                    }
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.INCORRECT_CATEGORY.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание недоступно для выдачи читателям данной категории.");
                    }
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.STUDENT_SUBSCRIPTION_EXPIRED.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Срок продления студенческого абонемента данного читателя " +
                                "истек.");
                    }
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.SCHOOLCHILD_SUBSCRIPTION_EXPIRED.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Срок продления школьного абонемента данного читателя " +
                                "истек.");
                    }
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.AGE_RESTRICTION_EXCEPTION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Читатель не соответствует ограничению по возрасту, " +
                                "существующему для данного издания.");
                    }
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(HistoryEntry entity) {
        jdbcTemplate.update(
                "DELETE FROM \"HistoryOfIssueOfPublications\" WHERE \"IssuedPublicationID\" = ?",
                entity.getId());
    }
}
