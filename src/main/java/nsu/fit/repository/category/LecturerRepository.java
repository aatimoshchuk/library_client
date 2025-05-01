package nsu.fit.repository.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.category.Lecturer;
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
public class LecturerRepository extends AbstractEntityRepository<Lecturer> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Lecturer> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"LecturerInformation\"",
                (rs, rowNum) -> new Lecturer(
                        rs.getInt("LecturerID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getString("JobTitle"))
        );
    }

    @Override
    public Warning saveEntity(Lecturer entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номер читательского билета\" должен представлять из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля \"Номер читательского билета\" и \"Название " +
                    "учебного заведения\" не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"LecturerInformation\" SET \"LibraryCardNumber\" = ?, " +
                                "\"EducationalInstitutionName\" = ?, \"JobTitle\" = ? WHERE \"LecturerID\" = ?",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        (entity.getJobTitle() != null && entity.getJobTitle().isEmpty()) ? null :
                                entity.getJobTitle(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"LecturerInformation\" (\"LibraryCardNumber\", \"EducationalInstitutionName\", " +
                                "\"JobTitle\") VALUES (?, ?, ?)",
                        entity.getLibraryCardNumber(),
                        entity.getEducationalInstitutionName(),
                        (entity.getJobTitle() != null && entity.getJobTitle().isEmpty()) ? null :
                                entity.getJobTitle()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode()) &&
                        sqlEx.getMessage().contains("LibraryCardNumber")) {
                    return new Warning(WarningType.SAVING_ERROR, "Читатель с таким номером читательского билета " +
                            "не существует.");

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
    public void deleteEntity(Lecturer entity) {
        jdbcTemplate.update(
                "DELETE FROM \"LecturerInformation\" WHERE \"LecturerID\" = ?",
                entity.getId());
    }

    public List<Lecturer> findEntities(String educationalInstitutionName, String jobTitle) {
        return jdbcTemplate.query(
                "SELECT * FROM \"getLecturersWithCharacteristics\"(?, ?)",
                (rs, rowNum) -> new Lecturer(
                        rs.getInt("LecturerID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("EducationalInstitutionName"),
                        rs.getString("JobTitle")),
                (educationalInstitutionName.isEmpty()) ? null : educationalInstitutionName,
                (jobTitle.isEmpty()) ? null : jobTitle
        );
    }
}
