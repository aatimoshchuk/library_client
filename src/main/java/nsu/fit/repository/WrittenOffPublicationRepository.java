package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.WrittenOffPublication;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WrittenOffPublicationRepository extends AbstractEntityRepository<WrittenOffPublication> {
    private static final Logger logger = LoggerFactory.getLogger(WrittenOffPublicationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public void markPublicationAsWrittenOff(Publication publication) {
        try {
            jdbcTemplate.update("INSERT INTO \"WrittenOffPublications\"(\"PublicationNomenclatureNumber\", " +
                            "\"WriteOffDate\") VALUES (?, CURRENT_DATE)",
                    publication.getId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public List<WrittenOffPublication> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"WrittenOffPublications\"",
                (rs, rowNum) -> new WrittenOffPublication(
                        rs.getInt("WriteOffID"),
                        rs.getInt("PublicationNomenclatureNumber"),
                        rs.getDate("WriteOffDate").toString())
        );
    }

    @Override
    public Warning saveEntity(WrittenOffPublication entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"WrittenOffPublications\" SET \"PublicationNomenclatureNumber\" = ?, " +
                                "\"WriteOffDate\" = TO_DATE(?, 'YYYY-MM-DD') WHERE \"WriteOffID\" = ?",
                        entity.getPublicationNomenclatureNumber(),
                        entity.getWriteOffDate(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"WrittenOffPublications\" (\"PublicationNomenclatureNumber\", \"WriteOffDate\") " +
                                "VALUES (?, TO_DATE(?, 'YYYY-MM-DD'))",
                        entity.getPublicationNomenclatureNumber(),
                        entity.getWriteOffDate()
                );
            }
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
                if (sqlEx.getMessage().contains("publication is out of stock")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Издание не в наличии!");
                }

                if (sqlEx.getMessage().contains("publication is already been written off")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Издание уже списано!");
                }

                if (sqlEx.getMessage().contains("field PublicationNomenclatureNumber cannot be changed")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Поле \"Номенклатурный номер издания\" не " +
                            "может быть изменено.");
                }

                if (sqlEx.getMessage().contains("write off date cannot be in the future")) {
                    return new Warning(IMPOSSIBLE_TO_SAVE, "Дата списания не может быть в будущем!");
                }
            } else if (e.getCause() instanceof SQLException sqlEx && sqlEx.getMessage()
                    .contains("date/time field value out of range")) {
                return new Warning(IMPOSSIBLE_TO_SAVE, "Введенная Вами дата не существует!");
            } else {
                logger.error("Невозможно сохранить запись: {}", e.getMessage());
                return new Warning(IMPOSSIBLE_TO_SAVE, null);
            }
        }

        return null;
    }

    @Override
    public void deleteEntity(WrittenOffPublication entity) {
        jdbcTemplate.update(
                "DELETE FROM \"WrittenOffPublications\" WHERE \"WriteOffID\" = ?",
                entity.getId());
    }
}
