package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.ScientificWorker;
import nsu.fit.repository.AbstractEntityRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

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
    public String saveEntity(ScientificWorker entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
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
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
                return "Невозможно сохранить: этот читатель уже принадлежит к одной из категорий!";
            } else {
                return e.getMessage();
            }
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
