package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.view.ColumnTranslation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LiteraryWorkRepository extends AbstractEntityRepository<LiteraryWork>{
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<LiteraryWork> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"LiteraryWork\"",
                (rs, rowNum) -> new LiteraryWork(
                        rs.getInt("ID"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getInt("WritingYear"),
                        rs.getString("Category"))
        );
    }

    public List<String> loadLiteraryWorkCategories() {
        return jdbcTemplate.query(
                "SELECT unnest(enum_range(NULL::\"LiteraryWorkCategory\")) AS category",
                (rs, rowNum) -> rs.getString("category")
        );
    }

    @Override
    public String saveEntity(LiteraryWork entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"LiteraryWork\" SET \"Title\" = ?, \"Author\" = ?, \"WritingYear\" = ?," +
                                "\"Category\" = ? WHERE \"ID\" = ?",
                        entity.getTitle(),
                        entity.getAuthor(),
                        entity.getWritingYear(),
                        entity.getCategory(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"LiteraryWork\" (\"Title\", \"Author\", \"WritingYear\", \"Category\") " +
                                "VALUES (?, ?, ?, ?)",
                        entity.getTitle(),
                        entity.getAuthor(),
                        entity.getWritingYear(),
                        entity.getCategory()
                );
            }
        } catch (Exception e) {
            System.out.println(e.getCause());
            return e.getMessage();
        }

        return null;
    }

    @Override
    public void deleteEntity(LiteraryWork entity) {
        jdbcTemplate.update(
                "DELETE FROM \"LiteraryWork\" WHERE \"ID\" = ?",
                entity.getId());
    }

    public List<Map<String, Object>> getTheMostPopularLiteraryWorks(int maxCount) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT * FROM \"getTheMostPopularLiteraryWorks\"(?)",
                maxCount));
    }
}
