package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.PublicationState;
import nsu.fit.data.access.Reader;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PublicationRepository extends AbstractEntityRepository<Publication> {
    private static final Logger logger = LoggerFactory.getLogger(PublicationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Publication> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"Publication\"",
                (rs, rowNum) -> new Publication(
                        rs.getInt("NomenclatureNumber"),
                        rs.getString("Title"),
                        rs.getString("Publisher"),
                        rs.getDate("ReceiptDate").toString(),
                        rs.getInt("YearOfPrinting"),
                        rs.getString("Category"),
                        rs.getInt("AgeRestriction"),
                        rs.getInt("StorageLocationID"),
                        PublicationState.fromRussianName(rs.getString("State")),
                        rs.getBoolean("PermissionToIssue"),
                        rs.getInt("DaysForReturn"))
        );
    }

    public Publication findOne(int publicationNomenclatureNumber) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM \"Publication\" WHERE \"NomenclatureNumber\" = ?",
                (rs, rowNum) -> new Publication(
                        rs.getInt("NomenclatureNumber"),
                        rs.getString("Title"),
                        rs.getString("Publisher"),
                        rs.getDate("ReceiptDate").toString(),
                        rs.getInt("YearOfPrinting"),
                        rs.getString("Category"),
                        rs.getInt("AgeRestriction"),
                        rs.getInt("StorageLocationID"),
                        PublicationState.fromRussianName(rs.getString("State")),
                        rs.getBoolean("PermissionToIssue"),
                        rs.getInt("DaysForReturn")),
                publicationNomenclatureNumber
        );
    }

    public List<Publication> findEntities(int libraryID) {
        return jdbcTemplate.query(
                "SELECT \"Publication\".* FROM \"Publication\" JOIN \"PublicationStorageLocation\" ON " +
                        "\"Publication\".\"StorageLocationID\" = \"PublicationStorageLocation\"." +
                        "\"StorageLocationID\" WHERE \"LibraryID\" = ?",
                (rs, rowNum) -> new Publication(
                        rs.getInt("NomenclatureNumber"),
                        rs.getString("Title"),
                        rs.getString("Publisher"),
                        rs.getDate("ReceiptDate").toString(),
                        rs.getInt("YearOfPrinting"),
                        rs.getString("Category"),
                        rs.getInt("AgeRestriction"),
                        rs.getInt("StorageLocationID"),
                        PublicationState.fromRussianName(rs.getString("State")),
                        rs.getBoolean("PermissionToIssue"),
                        rs.getInt("DaysForReturn")),
                libraryID
        );
    }

    @Override
    public Warning saveEntity(Publication entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля \"Название\", \"Издательство\", \"Дата поступления\", " +
                    "\"Разрешение на выдачу\" и \"Срок возврата\" не должны " +
                    "быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"Publication\" SET \"Title\" = ?, \"Publisher\" = ?, \"ReceiptDate\" = " +
                                "TO_DATE(?, 'YYYY-MM-DD'), \"YearOfPrinting\" = ?, \"Category\" = ?::\"PublicationCategory\", " +
                                "\"AgeRestriction\" = ?, \"StorageLocationID\" = ?, \"State\" = ?::\"PublicationState\", " +
                                "\"PermissionToIssue\" = ?, \"DaysForReturn\" = ? WHERE \"NomenclatureNumber\" = ?",
                        entity.getTitle(),
                        entity.getPublisher(),
                        entity.getReceiptDate(),
                        entity.getYearOfPrinting(),
                        entity.getCategory(),
                        entity.getAgeRestriction(),
                        entity.getStorageLocationID(),
                        entity.getState().toString(),
                        entity.getPermissionToIssue().get(),
                        entity.getDaysForReturn(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"Publication\" (\"Title\", \"Publisher\", \"ReceiptDate\", \"YearOfPrinting\", " +
                                "\"Category\", \"AgeRestriction\", \"StorageLocationID\", \"State\", \"PermissionToIssue\", " +
                                "\"DaysForReturn\") VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, " +
                                "?::\"PublicationCategory\", ?, ?, ?::\"PublicationState\", ?, ?)",
                        entity.getTitle(),
                        entity.getPublisher(),
                        entity.getReceiptDate(),
                        entity.getYearOfPrinting(),
                        entity.getCategory(),
                        entity.getAgeRestriction(),
                        entity.getStorageLocationID(),
                        entity.getState().toString(),
                        entity.getPermissionToIssue().get(),
                        entity.getDaysForReturn()
                );
            }
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
                if (sqlEx.getMessage().contains("field StorageLocationID cannot be changed while publication is written off")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Невозможно изменить место хранения списанного издания.");
                }
            }
        } catch (Exception e) {
            logger.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(IMPOSSIBLE_TO_SAVE, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(Publication entity) {
        jdbcTemplate.update(
                "DELETE FROM \"Publication\" WHERE \"NomenclatureNumber\" = ?",
                entity.getId());
    }

    public List<String> loadPublicationCategories() {
        return jdbcTemplate.query(
                "SELECT unnest(enum_range(NULL::\"PublicationCategory\")) AS category",
                (rs, rowNum) -> rs.getString("category")
        );
    }

    public List<Map<String, Object>> getPublicationsThatIssuedFromTheStorageLocation(Library library, int roomNumber,
                                                                                     int shelvingNumber,
                                                                                     int shelfNumber) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getPublicationsThatIssuedFromTheSpecificStorageLocation\"(?, ?, ?, ?)",
                library.getId(), roomNumber, shelvingNumber, shelfNumber));
    }

    public List<Map<String, Object>> getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered(Reader reader) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered\"(?)",
                reader.getId()));
    }

    public List<Map<String, Object>> getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered(Reader reader) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered\"(?)",
                reader.getId()));
    }

    public List<Map<String, Object>> getPublicationsWithTheAuthorsWorks(String authorName) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getPublicationsWithTheAuthorsWorks\"(?)",
                authorName));
    }

    public List<Map<String, Object>> getPublicationsWithLiteraryWork(LiteraryWork literaryWork) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getPublicationsWithLiteraryWork\"(?)",
                literaryWork.getId()));
    }

    public List<Map<String, Object>> getPublicationsThatWereReceiptDuringThePeriod(String startDate, String endDate) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"PublicationNomenclatureNumber\", \"Title\", \"ReceiptDate\" FROM " +
                        "\"getPublicationsThatWereReceiptOrWrittenOffDuringThePeriod\"(TO_DATE(?, 'YYYY-MM-DD')" +
                        ", TO_DATE(?, 'YYYY-MM-DD')) WHERE \"ReceiptDate\" IS NOT NULL",
                startDate, endDate));
    }

    public List<Map<String, Object>> getPublicationsThatWereWrittenOffDuringThePeriod(String startDate, String endDate) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"PublicationNomenclatureNumber\", \"Title\", \"WriteOffDate\" FROM " +
                        "\"getPublicationsThatWereReceiptOrWrittenOffDuringThePeriod\"(TO_DATE(?, 'YYYY-MM-DD'), " +
                        "TO_DATE(?, 'YYYY-MM-DD')) WHERE \"WriteOffDate\" IS NOT NULL",
                startDate, endDate));
    }

    public List<Map<String, Object>> getPublicationsInStorageLocation(int storageLocationId) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"NomenclatureNumber\", \"Title\", \"Publisher\", \"Category\", \"State\" FROM " +
                        "\"Publication\" WHERE \"StorageLocationID\" = ?",
                storageLocationId
        ));
    }
}
