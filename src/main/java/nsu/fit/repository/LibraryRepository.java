package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.Reader;
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
public class LibraryRepository extends AbstractEntityRepository<Library> {

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
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM \"Library\" WHERE \"ID\" = ?",
                    (rs, rowNum) -> new Library(
                            rs.getInt("ID"),
                            rs.getString("Name"),
                            rs.getString("Address")),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Warning saveEntity(Library entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
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
            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
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
                if (sqlEx.getSQLState().equals(SqlState.DUPLICATE_KEY_VALUE.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Читатель уже зарегистрирован в данной библиотеке!");
                }

                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode())) {
                    if (sqlEx.getMessage().contains("ReaderLibraryCardNumber")) {
                        return new Warning(WarningType.SAVING_ERROR, "Читатель с таким номер читательского билета не " +
                                "существует!");
                    }

                    if (sqlEx.getMessage().contains("LibraryID")) {
                        return new Warning(WarningType.SAVING_ERROR, "Библиотека с таким ID не существует! Чтобы " +
                                "зарегистрировать читателя Вам необходимо создать библиотеку.");
                    }
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    public Warning unregisterReaderInTheLibrary(int libraryID, int readerLibraryCardNumber) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM \"ReaderToLibrary\" WHERE \"ReaderLibraryCardNumber\" = ? AND \"LibraryID\" = ?",
                    readerLibraryCardNumber,
                    libraryID);
        } catch (Exception e) {
            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }
}
