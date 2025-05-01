package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.Librarian;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.Reader;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReaderRepository extends AbstractEntityRepository<Reader> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Reader> findAll() {
        return jdbcTemplate.query(
                "SELECT \"LibraryCardNumber\", \"Surname\", \"Reader\".\"Name\" AS \"ReaderName\", \"Patronymic\", " +
                        "\"BirthDay\", \"ReaderCategory\".\"Name\" AS \"CategoryName\"" +
                        "FROM \"Reader\" LEFT JOIN \"ReaderCategory\" ON \"Reader\".\"CategoryID\" = \"ReaderCategory\".\"ID\"",
                (rs, rowNum) -> new Reader(
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("Surname"),
                        rs.getString("ReaderName"),
                        rs.getString("Patronymic"),
                        rs.getDate("BirthDay").toString(),
                        rs.getString("CategoryName"))
        );
    }

    @Override
    public Warning saveEntity(Reader entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"Reader\" SET \"Surname\" = ?, \"Name\" = ?, \"Patronymic\" = ?," +
                                "\"BirthDay\" = TO_DATE(?, 'YYYY-MM-DD') WHERE \"LibraryCardNumber\" = ?",
                        entity.getSurname(),
                        entity.getName(),
                        entity.getPatronymic(),
                        entity.getBirthDay(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"Reader\" (\"Surname\", \"Name\", \"Patronymic\", \"BirthDay\", \"CategoryID\") " +
                                "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), NULL)",
                        entity.getSurname(),
                        entity.getName(),
                        entity.getPatronymic(),
                        entity.getBirthDay()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.CONSTRAINT_VIOLATION.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Возраст читателя должен быть не меньше 12 лет!");
                }

                if (sqlEx.getSQLState().equals(SqlState.INVALID_DATE.getCode()) ||
                        sqlEx.getSQLState().equals(SqlState.INVALID_DATE_FORMAT.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Дата рождения должна быть в формате YYYY-MM-DD!");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(Reader entity) {
        jdbcTemplate.update(
                "DELETE FROM \"Reader\" WHERE \"LibraryCardNumber\" = ?",
                entity.getId());
    }

    public Reader findOne(int libraryCardNumber) {
        return jdbcTemplate.queryForObject(
                "SELECT \"LibraryCardNumber\", \"Surname\", \"Reader\".\"Name\" AS \"ReaderName\", \"Patronymic\", " +
                        "\"BirthDay\", \"ReaderCategory\".\"Name\" AS \"CategoryName\"" +
                        "FROM \"Reader\" LEFT JOIN \"ReaderCategory\" ON \"Reader\".\"CategoryID\" = " +
                        "\"ReaderCategory\".\"ID\"" + " WHERE \"LibraryCardNumber\" = ?",
                (rs, rowNum) -> new Reader(
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("Surname"),
                        rs.getString("ReaderName"),
                        rs.getString("Patronymic"),
                        rs.getDate("BirthDay").toString(),
                        rs.getString("CategoryName")),
                libraryCardNumber
        );
    }

    public List<Reader> findEntities(int libraryID) {
        return jdbcTemplate.query(
                "SELECT \"LibraryCardNumber\", \"Surname\", \"Reader\".\"Name\" AS \"ReaderName\", \"Patronymic\", " +
                        "\"BirthDay\", \"ReaderCategory\".\"Name\" AS \"CategoryName\"" +
                        "FROM \"Reader\" LEFT JOIN \"ReaderCategory\" ON \"Reader\".\"CategoryID\" = " +
                        "\"ReaderCategory\".\"ID\" JOIN \"ReaderToLibrary\" ON \"Reader\".\"LibraryCardNumber\" = " +
                        "\"ReaderToLibrary\".\"ReaderLibraryCardNumber\" WHERE \"LibraryID\" = ?",
                (rs, rowNum) -> new Reader(
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("Surname"),
                        rs.getString("ReaderName"),
                        rs.getString("Patronymic"),
                        rs.getDate("BirthDay").toString(),
                        rs.getString("CategoryName")),
                libraryID
        );
    }

    public List<Map<String, Object>> getReadersWhoWereServedByTheLibrarianDuringThePeriod(Librarian librarian,
                                                                                          String startDate,
                                                                                          String endDate) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWhoWereServedByTheLibrarianDuringThePeriod\"" +
                        "(?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'))",
                librarian.getId(), startDate, endDate));
    }

    public List<Map<String, Object>> getReadersWhoHaveNotVisitedTheLibraryDuringThePeriod(String startDate,
                                                                                          String endDate) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWhoHaveNotVisitedTheLibraryDuringThePeriod\"" +
                        "(TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'))",
                startDate, endDate));
    }

    public List<Map<String, Object>> getReadersWithExpiredPublications() {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWithExpiredPublications\"()"));
    }

    public List<Map<String, Object>> getReadersWithLiteraryWork(LiteraryWork literaryWork) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWithLiteraryWork\"(?)",
                literaryWork.getId()));
    }

    public List<Map<String, Object>> getReadersWhoReceivedLiteraryWorkDuringThePeriod(LiteraryWork literaryWork,
                                                                                      String startDate,
                                                                                      String endDate) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWhoReceivedLiteraryWorkDuringThePeriod\"(?, TO_DATE(?, 'YYYY-MM-DD'), " +
                        "TO_DATE(?, 'YYYY-MM-DD'))",
                literaryWork.getId(), startDate, endDate));
    }

    public List<Map<String, Object>> getReadersWithPublication(Publication publication) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getReadersWithPublication\"(?)",
                publication.getId()));
    }

    public List<Map<String, Object>> getReadersWhoRegisteredInTheLibrary(Library library) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"LibraryCardNumber\", \"Surname\", \"Reader\".\"Name\" AS \"ReaderName\", \"Patronymic\", " +
                        "\"BirthDay\", \"ReaderCategory\".\"Name\" AS \"CategoryName\"" +
                        "FROM \"Reader\" JOIN \"ReaderToLibrary\" ON \"Reader\".\"LibraryCardNumber\" = " +
                        "\"ReaderToLibrary\".\"ReaderLibraryCardNumber\" LEFT JOIN \"ReaderCategory\" ON " +
                        "\"Reader\".\"CategoryID\" = \"ReaderCategory\".\"ID\" WHERE \"LibraryID\" = ?",
                library.getId()));
    }
}
