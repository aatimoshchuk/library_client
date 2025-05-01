package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.Librarian;
import nsu.fit.data.access.Library;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LibrarianRepository extends AbstractEntityRepository<Librarian> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Librarian> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"Librarian\"",
                (rs, rowNum) -> new Librarian(
                        rs.getInt("LibrarianID"),
                        rs.getString("Surname"),
                        rs.getString("Name"),
                        rs.getString("Patronymic"),
                        rs.getDate("BirthDay").toString(),
                        rs.getString("PhoneNumber"),
                        rs.getInt("LibraryID"),
                        rs.getInt("RoomNumber"))
        );
    }

    public Librarian findOne(int librarianID) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM \"Librarian\" WHERE \"LibrarianID\" = ?",
                    (rs, rowNum) -> new Librarian(
                            rs.getInt("LibrarianID"),
                            rs.getString("Surname"),
                            rs.getString("Name"),
                            rs.getString("Patronymic"),
                            rs.getDate("BirthDay").toString(),
                            rs.getString("PhoneNumber"),
                            rs.getInt("LibraryID"),
                            rs.getInt("RoomNumber")),
                    librarianID
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Warning saveEntity(Librarian entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"ID библиотеки\" и \"Номер зала\" должны представлять из " +
                    "себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"Librarian\" SET \"Surname\" = ?, \"Name\" = ?, \"Patronymic\" = ?," +
                                "\"BirthDay\" = TO_DATE(?, 'YYYY-MM-DD'), \"PhoneNumber\" = ?, \"LibraryID\" = ?," +
                                "\"RoomNumber\" = ? WHERE \"LibrarianID\" = ?",
                        entity.getSurname(),
                        entity.getName(),
                        entity.getPatronymic(),
                        entity.getBirthDay(),
                        entity.getPhoneNumber(),
                        entity.getLibrary(),
                        entity.getRoomNumber(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"Librarian\" (\"Surname\", \"Name\", \"Patronymic\", \"BirthDay\", \"PhoneNumber\"," +
                                "\"LibraryID\", \"RoomNumber\") VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)",
                        entity.getSurname(),
                        entity.getName(),
                        entity.getPatronymic(),
                        entity.getBirthDay(),
                        entity.getPhoneNumber(),
                        entity.getLibrary(),
                        entity.getRoomNumber()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode()) &&
                        sqlEx.getMessage().contains("LibraryID")) {
                    return new Warning(WarningType.SAVING_ERROR, "Библиотека с таким ID не существует!");
                }

                if (sqlEx.getSQLState().equals(SqlState.CONSTRAINT_VIOLATION.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Возраст библиотекаря должен быть не меньше 18 лет!");
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
    public void deleteEntity(Librarian entity) {
        jdbcTemplate.update(
                "DELETE FROM \"Librarian\" WHERE \"LibrarianID\" = ?",
                entity.getId());
    }

    public int getDataOnLibrarianProductivityDuringThePeriod(Librarian librarian, String startDate, String endDate) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT \"NumberOfServedReaders\" FROM \"getDataOnLibrariansProductivityDuringThePeriod\"(TO_DATE(?, " +
                            "'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD')) WHERE \"LibrarianID\" = ?",
                    new Object[]{startDate, endDate, librarian.getId()},
                    Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Map<String, Object>> getLibrariansWhoWorksInTheRoom(Library library, int roomNumber) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                    "SELECT * FROM \"getLibrariansWhoWorksInTheLibraryRoom\"(?, ?)",
                    library.getId(), roomNumber));
    }
}
