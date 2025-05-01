package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.WrittenOffPublication;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.TriggerExceptionMessage;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WrittenOffPublicationRepository extends AbstractEntityRepository<WrittenOffPublication> {

    private final JdbcTemplate jdbcTemplate;

    public Warning markPublicationAsWrittenOff(Publication publication) {
        try {
            jdbcTemplate.update("INSERT INTO \"WrittenOffPublications\"(\"PublicationNomenclatureNumber\", " +
                            "\"WriteOffDate\") VALUES (?, CURRENT_DATE)",
                    publication.getId());
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.TRIGGER_EXCEPTION.getCode())) {
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.DATE_IN_THE_FUTURE.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Дата списания не может быть в будущем!");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.WRITTEN_OFF_PUBLICATION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание уже списано!");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.OUT_OF_STOCK_PUBLICATION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание не в наличии!");
                    }
                }

                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Издание с таким номенклатурным номером не " +
                            "существует! Вам необходимо создать издание, прежде чем списать его.");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
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
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номенклатурный номер издания\" должен представлять собой " +
                    "число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
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
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.TRIGGER_EXCEPTION.getCode())) {
                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.DATE_IN_THE_FUTURE.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Дата списания не может быть в будущем!");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.WRITTEN_OFF_PUBLICATION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание уже списано!");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.OUT_OF_STOCK_PUBLICATION.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Издание не в наличии!");
                    }

                    if (sqlEx.getMessage().contains(TriggerExceptionMessage.NOMENCLATURE_NUMBER_CHANGING_ATTEMPT.toString())) {
                        return new Warning(WarningType.SAVING_ERROR, "Поле \"Номенклатурный номер издания\" не " +
                                "может быть изменено.");
                    }
                }

                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Издание с таким номенклатурным номером не " +
                            "существует!");
                }

                if (sqlEx.getSQLState().equals(SqlState.INVALID_DATE.getCode()) ||
                        sqlEx.getSQLState().equals(SqlState.INVALID_DATE_FORMAT.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Дата списания должна быть в формате YYYY-MM-DD!");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
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
