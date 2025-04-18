package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.Student;
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
public class StudentRepository extends AbstractEntityRepository<Student> {
    private static final Logger logger = LoggerFactory.getLogger(StudentRepository.class);
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Student> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"StudentInformation\"",
                (rs, rowNum) -> new Student(
                        rs.getInt("StudentID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getString("Faculty"),
                        rs.getInt("Course"),
                        rs.getInt("GroupNumber"),
                        rs.getInt("StudentCardNumber"),
                        rs.getDate("ExtensionDate").toString())
        );
    }

    @Override
    public Warning saveEntity(Student entity) {
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"StudentInformation\" SET \"LibraryCardNumber\" = ?, " +
                                "\"EducationalInstitutionName\" = ?, \"Faculty\" = ?, \"Course\" = ?, " +
                                "\"GroupNumber\" = ?, \"StudentCardNumber\" = ?, " +
                                "\"ExtensionDate\" = TO_DATE(?, 'YYYY-MM-DD') WHERE \"StudentID\" = ?",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        (entity.getFaculty() != null && entity.getFaculty().isEmpty()) ? null :
                                entity.getFaculty(),
                        entity.getCourse(),
                        (entity.getGroupNumber() == 0) ? null : entity.getGroupNumber(),
                        entity.getStudentCardNumber(),
                        entity.getExtensionDate(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"StudentInformation\" (\"LibraryCardNumber\", \"EducationalInstitutionName\", " +
                                "\"Faculty\", \"Course\", \"GroupNumber\", \"StudentCardNumber\", \"ExtensionDate\") " +
                                "VALUES (?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        (entity.getFaculty() != null && entity.getFaculty().isEmpty()) ? null :
                                entity.getFaculty(),
                        entity.getCourse(),
                        (entity.getGroupNumber() == 0) ? null : entity.getGroupNumber(),
                        entity.getStudentCardNumber(),
                        entity.getExtensionDate()
                );
            }
        } catch (DataIntegrityViolationException e) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Номер курса должен быть больше 0 и меньше 7!");
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
    public void deleteEntity(Student entity) {
        jdbcTemplate.update(
                "DELETE FROM \"StudentInformation\" WHERE \"StudentID\" = ?",
                entity.getId());
    }

    public List<Student> findEntities(String educationalInstitutionName, String faculty, String course) {
        return jdbcTemplate.query(
                "SELECT * FROM \"getStudentsWithCharacteristics\"(?, ?, ?)",
                (rs, rowNum) -> new Student(
                        rs.getInt("StudentID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getString("Faculty"),
                        rs.getInt("Course"),
                        rs.getInt("GroupNumber"),
                        rs.getInt("StudentCardNumber"),
                        rs.getDate("ExtensionDate").toString()),
                (educationalInstitutionName.isEmpty()) ? null : educationalInstitutionName,
                (faculty.isEmpty()) ? null : faculty,
                (course.isEmpty()) ? null : Integer.parseInt(course)
        );
    }
}
