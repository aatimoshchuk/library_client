package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.Schoolchild;
import nsu.fit.repository.AbstractEntityRepository;
import nsu.fit.utils.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SchoolchildRepository extends AbstractEntityRepository<Schoolchild> {
    private static final Logger logger = LoggerFactory.getLogger(SchoolchildRepository.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Schoolchild> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"SchoolchildInformation\"",
                (rs, rowNum) -> new Schoolchild(
                        rs.getInt("SchoolchildID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getInt("Grade"),
                        rs.getDate("ExtensionDate").toString())
        );
    }

    @Override
    public Warning saveEntity(Schoolchild entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"SchoolchildInformation\" SET \"LibraryCardNumber\" = ?, " +
                                "\"EducationalInstitutionName\" = ?, \"Grade\" = ?, " +
                                "\"ExtensionDate\" = TO_DATE(?, 'YYYY-MM-DD') WHERE \"SchoolchildID\" = ?",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        entity.getGrade(),
                        entity.getExtensionDate(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"SchoolchildInformation\" (\"LibraryCardNumber\", \"EducationalInstitutionName\", " +
                                "\"Grade\", \"ExtensionDate\") VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        entity.getGrade(),
                        entity.getExtensionDate()
                );
            }
        } catch (DataIntegrityViolationException e) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Номер учебного класса должен быть от 1 до 11!");
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
                return new Warning(IMPOSSIBLE_TO_SAVE, "Этот читатель уже принадлежит к одной из категорий!");
            } else {
                logger.error("Невозможно сохранить запись: {}", e.getMessage());
                return new Warning(IMPOSSIBLE_TO_SAVE, null);
            }
        }

        return null;
    }

    @Override
    public void deleteEntity(Schoolchild entity) {
        jdbcTemplate.update(
                "DELETE FROM \"SchoolchildInformation\" WHERE \"SchoolchildID\" = ?",
                entity.getId());
    }

    public List<Schoolchild> findEntities(String educationalInstitutionName, String grade) {
        return jdbcTemplate.query(
                "SELECT * FROM \"getSchoolchildrenWithCharacteristics\"(?, ?)",
                (rs, rowNum) -> new Schoolchild(
                        rs.getInt("SchoolchildID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getInt("Grade"),
                        rs.getDate("ExtensionDate").toString()),
                (educationalInstitutionName.isEmpty()) ? null : educationalInstitutionName,
                (grade.isEmpty()) ? null : Integer.parseInt(grade)
        );
    }
}
