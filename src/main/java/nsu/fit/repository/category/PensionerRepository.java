package nsu.fit.repository.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.category.Pensioner;
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
public class PensionerRepository extends AbstractEntityRepository<Pensioner> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Pensioner> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"PensionerInformation\"",
                (rs, rowNum) -> new Pensioner(
                        rs.getInt("PensionerID"),
                        rs.getInt("LibraryCardNumber"),
                        rs.getString("PensionCertificateNumber"))
        );
    }

    @Override
    public Warning saveEntity(Pensioner entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"Номер читательского билета\" должен представлять из себя число!");
        }

        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"PensionerInformation\" SET \"LibraryCardNumber\" = ?, " +
                                "\"PensionCertificateNumber\" = ? WHERE \"PensionerID\" = ?",
                        entity.getLibraryCardNumber(),
                        entity.getPensionCertificateNumber(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"PensionerInformation\" (\"LibraryCardNumber\", \"PensionCertificateNumber\") " +
                                "VALUES (?, ?)",
                        entity.getLibraryCardNumber(),
                        entity.getPensionCertificateNumber()
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
    public void deleteEntity(Pensioner entity) {
        jdbcTemplate.update(
                "DELETE FROM \"PensionerInformation\" WHERE \"PensionerID\" = ?",
                entity.getId());
    }
}
