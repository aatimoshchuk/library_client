package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Library;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public String saveEntity(Library entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
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
            return e.getMessage();
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
