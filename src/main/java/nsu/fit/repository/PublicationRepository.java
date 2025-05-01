package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.PublicationState;
import nsu.fit.data.access.Reader;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.TriggerExceptionMessage;
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
public class PublicationRepository extends AbstractEntityRepository<Publication> {

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
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"ID места хранения\" и \"Срок возврата\" должны " +
                    "представлять собой число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля \"Название\", \"Издательство\", \"Дата поступления\", " +
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
                        (entity.getYearOfPrinting() != null && entity.getYearOfPrinting() == 0) ? null :
                                entity.getYearOfPrinting(),
                        (entity.getCategory() != null && entity.getCategory().isEmpty()) ? null : entity.getCategory() ,
                        (entity.getAgeRestriction() != null && entity.getAgeRestriction() == 0) ? null :
                                entity.getAgeRestriction(),
                        (entity.getStorageLocationID() != null && entity.getStorageLocationID() == 0) ? null :
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
                        (entity.getYearOfPrinting() != null && entity.getYearOfPrinting() == 0) ? null :
                        entity.getYearOfPrinting(),
                        (entity.getCategory() != null && entity.getCategory().isEmpty()) ? null : entity.getCategory() ,
                        (entity.getAgeRestriction() != null && entity.getAgeRestriction() == 0) ? null :
                        entity.getAgeRestriction(),
                        (entity.getStorageLocationID() != null && entity.getStorageLocationID() == 0) ? null :
                        entity.getStorageLocationID(),
                        entity.getState().toString(),
                        entity.getPermissionToIssue().get(),
                        entity.getDaysForReturn()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode()) &&
                        sqlEx.getMessage().contains("StorageLocationID")) {
                    return new Warning(WarningType.SAVING_ERROR, "Место хранения с таким ID не существует!");
                }

                if (sqlEx.getSQLState().equals(SqlState.INVALID_DATE.getCode()) ||
                        sqlEx.getSQLState().equals(SqlState.INVALID_DATE_FORMAT.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Дата поступления должна быть в формате YYYY-MM-DD!");
                }

                if (sqlEx.getSQLState().equals(SqlState.TRIGGER_EXCEPTION.getCode())) {
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.WRITTEN_OFF_PUBLICATION_CHANGING_LOCATION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Невозможно изменить место хранения списанного издания.");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.DATE_IN_THE_FUTURE.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Дата поступления не может быть в будущем.");
                    }
                }

                System.out.println(sqlEx.getSQLState());
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
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
