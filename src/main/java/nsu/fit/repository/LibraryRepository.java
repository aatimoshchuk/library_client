package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Library;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
