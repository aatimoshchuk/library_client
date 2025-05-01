package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.LiteraryWork;
import nsu.fit.data.access.Publication;
import nsu.fit.utils.ColumnTranslation;
import nsu.fit.utils.warning.SqlState;
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
    public Warning saveEntity(LiteraryWork entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля \"Название\" и \"Автор\" не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"LiteraryWork\" SET \"Title\" = ?, \"Author\" = ?, \"WritingYear\" = ?," +
                                "\"Category\" = ?::\"LiteraryWorkCategory\" WHERE \"ID\" = ?",
                        entity.getTitle(),
                        entity.getAuthor(),
                        (entity.getWritingYear() != null && entity.getWritingYear() == 0) ? null :
                                entity.getWritingYear(),
                        (entity.getCategory() != null && entity.getCategory().isEmpty()) ? null : entity.getCategory(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"LiteraryWork\" (\"Title\", \"Author\", \"WritingYear\", \"Category\") " +
                                "VALUES (?, ?, ?, ?::\"LiteraryWorkCategory\")",
                        entity.getTitle(),
                        entity.getAuthor(),
                        (entity.getWritingYear() != null && entity.getWritingYear() == 0) ? null :
                                entity.getWritingYear(),
                        (entity.getCategory() != null && entity.getCategory().isEmpty()) ? null : entity.getCategory()
                );
            }
        } catch (Exception e) {
            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(LiteraryWork entity) {
        jdbcTemplate.update(
                "DELETE FROM \"LiteraryWork\" WHERE \"ID\" = ?",
                entity.getId());
    }

    public Warning setRelationshipWithPublication(LiteraryWork literaryWork, int publicationNomenclatureNumber) {
        try {
            jdbcTemplate.update("INSERT INTO \"PublicationToLiteraryWork\" (\"LiteraryWorkID\", " +
                            "\"PublicationNomenclatureNumber\") VALUES (?, ?)",
                    literaryWork.getId(),
                    publicationNomenclatureNumber);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.DUPLICATE_KEY_VALUE.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Издание уже включает в себя данное произведение!");
                }

                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Издание с таким номенклатурным номером не " +
                            "существует!");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    public Warning removeRelationshipWithPublication(LiteraryWork literaryWork, int publicationNomenclatureNumber) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM \"PublicationToLiteraryWork\" WHERE \"LiteraryWorkID\" = ? AND " +
                            "\"PublicationNomenclatureNumber\" = ?",
                    literaryWork.getId(),
                    publicationNomenclatureNumber);
        } catch (Exception e) {
            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
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
