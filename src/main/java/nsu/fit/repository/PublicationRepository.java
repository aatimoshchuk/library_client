package nsu.fit.repository;

import nsu.fit.data.access.Library;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Reader;
import nsu.fit.view.ColumnTranslation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PublicationRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
}
