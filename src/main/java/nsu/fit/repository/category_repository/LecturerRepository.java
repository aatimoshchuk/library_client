package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.Lecturer;
import nsu.fit.repository.AbstractEntityRepository;
import nsu.fit.repository.LibraryRepository;
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
public class LecturerRepository extends AbstractEntityRepository<Lecturer> {
    private static final Logger logger = LoggerFactory.getLogger(LecturerRepository.class);
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
        if (!entity.checkEmptyFields()) {
            return new Warning(IMPOSSIBLE_TO_SAVE, "Поля не должны быть пустыми!");
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
