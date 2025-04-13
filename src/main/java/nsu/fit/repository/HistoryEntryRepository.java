package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.HistoryEntry;
import nsu.fit.data.access.Publication;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HistoryEntryRepository extends AbstractEntityRepository<HistoryEntry> {
    private static final Logger logger = LoggerFactory.getLogger(HistoryEntryRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public void markPublicationAsReturned(int publicationNomenclatureNumber) {
        try {
            jdbcTemplate.update("UPDATE \"HistoryOfIssueOfPublications\" SET \"ReturnDate\" = CURRENT_DATE WHERE " +
                            "\"PublicationNomenclatureNumber\" = ?",
                    publicationNomenclatureNumber);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
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
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля \"Номенклатурный номер издания\", \"Номер читательского " +
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
                        (entity.getLibrarianID() == 0) ? null : entity.getLibrarianID(),
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
                        (entity.getLibrarianID() == null || entity.getLibrarianID() == 0) ? null : entity.getLibrarianID()
                );
            }
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
                if (sqlEx.getMessage().contains("selected publication is not available for readers without category")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Издание недоступно для выдачи читателям, не относящимся " +
                            "ни к одной из категорий.");
                }
                if (sqlEx.getMessage().contains("selected publication is not available for this category of readers")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Издание недоступно для выдачи читателям данной категории.");
                }
                if (sqlEx.getMessage().contains("the student subscription renewal period has expired")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Срок продления студенческого абонемента данного читателя " +
                            "истек.");
                }
                if (sqlEx.getMessage().contains("the schoolchild subscription renewal period has expired")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Срок продления школьного абонемента данного читателя " +
                            "истек.");
                }
                if (sqlEx.getMessage().contains("reader does not meet the age restriction")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Читатель не соответствует ограничению по возрасту, " +
                            "существующему для данного издания.");
                }
            }

        } catch (Exception e) {
            logger.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(IMPOSSIBLE_TO_SAVE, null);
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
