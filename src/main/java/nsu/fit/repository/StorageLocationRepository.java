package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nsu.fit.data.access.StorageLocation;
import nsu.fit.utils.warning.SqlState;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StorageLocationRepository extends AbstractEntityRepository<StorageLocation> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<StorageLocation> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM \"PublicationStorageLocation\"",
                (rs, rowNum) -> new StorageLocation(
                        rs.getInt("StorageLocationID"),
                        rs.getInt("LibraryID"),
                        rs.getInt("RoomNumber"),
                        rs.getInt("ShelvingNumber"),
                        rs.getInt("ShelfNumber"))
        );
    }

    @Override
    public Warning saveEntity(StorageLocation entity) {
        if (!entity.validateNumericFields()) {
            return new Warning(WarningType.SAVING_ERROR, "\"ID библиотеки\", \"Номер зала\", \"Номер стеллажа\" и " +
                    "\"Номер полки\" должны представлять собой число!");
        }
        if (!entity.checkEmptyFields()) {
            return new Warning(WarningType.SAVING_ERROR, "Поля не должны быть пустыми!");
        }

        try {
            if (entity.getId() != 0) {
                jdbcTemplate.update(
                        "UPDATE \"PublicationStorageLocation\" SET \"LibraryID\" = ?, \"RoomNumber\" = ?, " +
                                "\"ShelvingNumber\" = ?, \"ShelfNumber\" = ? WHERE \"StorageLocationID\" = ?",
                        entity.getLibraryID(),
                        entity.getRoomNumber(),
                        entity.getShelvingNumber(),
                        entity.getShelfNumber(),
                        entity.getId());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO \"PublicationStorageLocation\" (\"LibraryID\", \"RoomNumber\", " +
                                "\"ShelvingNumber\", \"ShelfNumber\") VALUES (?, ?, ?, ?)",
                        entity.getLibraryID(),
                        entity.getRoomNumber(),
                        entity.getShelvingNumber(),
                        entity.getShelfNumber()
                );
            }
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException sqlEx) {
                if (sqlEx.getSQLState().equals(SqlState.FOREIGN_KEY_MISSING.getCode()) &&
                        sqlEx.getMessage().contains("LibraryID")) {
                    return new Warning(WarningType.SAVING_ERROR, "Библиотека с таким ID не существует");
                }
            }

            log.error("Невозможно сохранить запись: {}", e.getMessage());
            return new Warning(WarningType.SAVING_ERROR, null);
        }

        return null;
    }

    @Override
    public void deleteEntity(StorageLocation entity) {
        jdbcTemplate.update(
                "DELETE FROM \"PublicationStorageLocation\" WHERE \"StorageLocationID\" = ?",
                entity.getId());
    }

    public StorageLocation findOne(int storageLocationID) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM \"PublicationStorageLocation\" WHERE \"StorageLocationID\" = ?",
                    (rs, rowNum) -> new StorageLocation(
                            rs.getInt("StorageLocationID"),
                            rs.getInt("LibraryID"),
                            rs.getInt("RoomNumber"),
                            rs.getInt("ShelvingNumber"),
                            rs.getInt("ShelfNumber")),
                    storageLocationID
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
