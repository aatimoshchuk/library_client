package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LiteraryWorkRepository extends AbstractEntityRepository<LiteraryWork>{
    private static final Logger logger = LoggerFactory.getLogger(LiteraryWorkRepository.class);
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
    public Warning saveEntity(LiteraryWork entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"LiteraryWork\" SET \"Title\" = ?, \"Author\" = ?, \"WritingYear\" = ?," +
                                "\"Category\" = ?::\"LiteraryWorkCategory\" WHERE \"ID\" = ?",
                        entity.getTitle(),
                        entity.getAuthor(),
                        entity.getWritingYear(),
                        entity.getCategory(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"LiteraryWork\" (\"Title\", \"Author\", \"WritingYear\", \"Category\") " +
                                "VALUES (?, ?, ?, ?::\"LiteraryWorkCategory\")",
                        entity.getTitle(),
                        entity.getAuthor(),
                        entity.getWritingYear(),
                        entity.getCategory()
                );
            }
        } catch (Exception e) {
            logger.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(IMPOSSIBLE_TO_SAVE, null);
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

    public List<Map<String, Object>> getLiteraryWorksIncludedInThePublication(Publication publication) {
        return ColumnTranslation.formatColumnNames(jdbcTemplate.queryForList(
                "SELECT \"LiteraryWorkID\", \"Title\", \"Author\", \"WritingYear\", \"Category\" FROM " +
                        "\"LiteraryWork\" JOIN \"PublicationToLiteraryWork\" ON \"LiteraryWork\".\"ID\" = " +
                        "\"PublicationToLiteraryWork\".\"LiteraryWorkID\" WHERE \"PublicationNomenclatureNumber\" = ?",
                publication.getId()));
    }
}
