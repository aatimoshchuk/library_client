package nsu.fit.repository.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.category.Schoolchild;
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
    public Warning saveEntity(Schoolchild entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номер читательского билета\" и \"Учебный класс\" " +
                    "должны представлять из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
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
                    return new Warning(WarningType.SAVING_ERROR, "Номер учебного класса должен быть от 1 до 11!");
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
