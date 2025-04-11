package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Librarian;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.Reader;
import nsu.fit.view.ColumnTranslation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
    public String saveEntity(Reader entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
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
        } catch (DataIntegrityViolationException e) {
            return "Невозможно сохранить: возраст читателя должен быть не меньше 12 лет!";
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
                "SELECT \"LibraryCardNumber\", \"Surname\", \"Name\", \"Patronymic\", \"BirthDay\" FROM \"Reader\" " +
                        "WHERE \"LibraryCardNumber\" = ?",
                (rs, rowNum) -> new Reader(
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("Surname"),
                        rs.getString("Name"),
                        rs.getString("Patronymic"),
                        rs.getDate("BirthDay").toString(),
                        null),
                libraryCardNumber
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
}
