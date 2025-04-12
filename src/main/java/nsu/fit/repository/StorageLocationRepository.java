package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.StorageLocation;
import nsu.fit.utils.Warning;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StorageLocationRepository extends AbstractEntityRepository<StorageLocation> {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<StorageLocation> findAll() {
        return null;
    }

    @Override
    public Warning saveEntity(StorageLocation entity) {
        return null;
    }

    @Override
    public void deleteEntity(StorageLocation entity) {

    }

    public StorageLocation findOne(int storageLocationID) {
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
    }
}
