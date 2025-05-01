package nsu.fit.repository.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.category.ScientificWorker;
import nsu.fit.repository.AbstractEntityRepository;
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
public class ScientificWorkerRepository extends AbstractEntityRepository<ScientificWorker> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ScientificWorker> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"ScientificWorkerInformation\"",
                (rs, rowNum) -> new ScientificWorker(
                        rs.getInt("ScientificWorkerID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("OrganizationName"),
                        rs.getString("ScientificTopic"))
        );
    }

    @Override
    public Warning saveEntity(ScientificWorker entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номер читательского билета\" должен представлять " +
                    "из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"ScientificWorkerInformation\" SET \"LibraryCardNumber\" = ?, " +
                                "\"OrganizationName\" = ?, \"ScientificTopic\" = ? WHERE \"ScientificWorkerID\" = ?",
                        entity.getLibraryCardNumber(),
                        entity.getOrganizationName(),
                        entity.getScientificTopic(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"ScientificWorkerInformation\" (\"LibraryCardNumber\", \"OrganizationName\", " +
                                "\"ScientificTopic\") VALUES (?, ?, ?)",
                        entity.getLibraryCardNumber(),
                        entity.getOrganizationName(),
                        entity.getScientificTopic()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode()) &&
                        sqlEx.getMessage().contains("LibraryCardNumber")) {
                    return new Warning(WarningType.SAVING_ERROR, "Читатель с таким номером читательского билета не " +
                            "существует.");
                }

                if (sqlEx.getSQLState().equals(SqlState.TRIGGER_EXCEPTION.getCode()) &&
                        sqlEx.getMessage().contains(TriggerExceptionMessage.REDEFINING_READER_CATEGORY.toString())) {
                    return new Warning(WarningType.SAVING_ERROR, "Этот читатель уже принадлежит к одной из категорий!");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(ScientificWorker entity) {
        jdbcTemplate.update(
                "DELETE FROM \"ScientificWorkerInformation\" WHERE \"ScientificWorkerID\" = ?",
                entity.getId());
    }

    public List<ScientificWorker> findEntities(String organizationName, String scientificTopic) {
        return jdbcTemplate.query(
                "SELECT * FROM \"getScientificWorkersWithCharacteristics\"(?, ?)",
                (rs, rowNum) -> new ScientificWorker(
                        rs.getInt("ScientificWorkerID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("OrganizationName"),
                        rs.getString("ScientificTopic")),
                (organizationName.isEmpty()) ? null : organizationName,
                (scientificTopic.isEmpty()) ? null : scientificTopic
        );
    }
}
