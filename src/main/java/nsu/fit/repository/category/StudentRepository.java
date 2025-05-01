package nsu.fit.repository.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.category.Student;
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
public class StudentRepository extends AbstractEntityRepository<Student> {

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
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номер читательского билета\", \"Курс\" \"Номер " +
                    "группы\" и \"Номер студенческого билета\" должны представлять из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля \"Номер читательского билета\", \"Название " +
                    "учебного заведения\", \"Курс\", \"Номер студенческого билета\" и \"Срок продления\" не должны " +
                    "быть пустыми!");
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
                        (entity.getGroupNumber() != null && entity.getGroupNumber() == 0) ? null :
                                entity.getGroupNumber(),
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
                        (entity.getGroupNumber() != null && entity.getGroupNumber() == 0) ? null :
                                entity.getGroupNumber(),
                        entity.getStudentCardNumber(),
                        entity.getExtensionDate()
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

                if (sqlEx.getSQLState().equals(SqlState.CONSTRAINT_VIOLATION.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Номер курса должен быть от 1 до 6!");
                }

                if (sqlEx.getSQLState().equals(SqlState.INVALID_DATE.getCode()) ||
                        sqlEx.getSQLState().equals(SqlState.INVALID_DATE_FORMAT.getCode())) {
                    return new Warning(WarningType.SAVING_ERROR, "Срок продления должен быть в формате YYYY-MM-DD!");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
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
