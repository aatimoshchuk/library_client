package nsu.fit.repository.category_repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.category.Pensioner;
import nsu.fit.repository.AbstractEntityRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

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
    public String saveEntity(Pensioner entity) {
        if (!entity.checkEmptyFields()) {
            return "Невозможно сохранить: поля не должны быть пустыми!";
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
    public void deleteEntity(Pensioner entity) {
        jdbcTemplate.update(
                "DELETE FROM \"PensionerInformation\" WHERE \"PensionerID\" = ?",
                entity.getId());
    }
}
