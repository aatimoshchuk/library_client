package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.Schoolchild;
import nsu.fit.repository.AbstractEntityRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SchoolchildRepository extends AbstractEntityRepository<Schoolchild> {
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
    public String saveEntity(Schoolchild entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
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
            return "Невозможно сохранить: номер учебного класса должен быть от 1 до 11!";
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
