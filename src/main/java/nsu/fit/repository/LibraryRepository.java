package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.Reader;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LibraryRepository extends AbstractEntityRepository<Library> {
    private static final Logger logger = LoggerFactory.getLogger(LibraryRepository.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Library> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"Library\"",
                (rs, rowNum) -> new Library(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getString("Address"))
        );
    }

    public Library findOne(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM \"Library\" WHERE \"ID\" = ?",
                (rs, rowNum) -> new Library(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getString("Address")),
                id
        );
    }

    @Override
    public Warning saveEntity(Library entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"Library\" SET \"Name\" = ?, \"Address\" = ? WHERE \"ID\" = ?",
                        entity.getName(),
                        entity.getAddress(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"Library\" (\"Name\", \"Address\") VALUES (?, ?)",
                        entity.getName(),
                        entity.getAddress()
                );
            }
        } catch (Exception e) {
            logger.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(IMPOSSIBLE_TO_SAVE, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(Library entity) {
        jdbcTemplate.update(
                "DELETE FROM \"Library\" WHERE \"ID\" = ?",
                entity.getId());
    }

    public List<Map<String, Object>> getLibrariesWhereReaderIsRegistered(Reader reader) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"ID\", \"Name\" AS \"LibraryName\", \"Address\" FROM \"Library\" LEFT JOIN " +
                        "\"ReaderToLibrary\" ON \"Library\".\"ID\" = \"ReaderToLibrary\".\"LibraryID\" " +
                        "WHERE \"ReaderLibraryCardNumber\" = ?",
                reader.getId()));
    }

    public Warning registerReaderInTheLibrary(int libraryID, int readerLibraryCardNumber) {
        try {
            jdbcTemplate.update("INSERT INTO \"ReaderToLibrary\" (\"ReaderLibraryCardNumber\", \"LibraryID\") " +
                            "VALUES (?, ?)",
                    readerLibraryCardNumber,
                    libraryID);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getMessage().contains("duplicate key value")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Читатель уже зарегистрирован в данной библиотеке!");
                }

                if (sqlEx.getMessage().contains("violates foreign key constraint")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Читатель с таким номер читательского билета не " +
                            "существует!");
                }
            }

            logger.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(IMPOSSIBLE_TO_SAVE, null);
        }

        return null;
    }
}
