package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.PublicationPermission;
import nsu.fit.data.access.ReaderCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PublicationPermissionRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<ReaderCategory> findAllForPublication(Publication publication) {
        Set<Integer> permittedIds = new HashSet<>(jdbcTemplate.query(
                "SELECT \"ReaderCategoryID\" FROM \"PublicationPermissionToIssue\" WHERE " +
                        "\"PublicationNomenclatureNumber\" = ?",
                new Object[]{publication.getId()},
                (rs, rowNum) -> rs.getInt("ReaderCategoryID")));


        return jdbcTemplate.query(
                "SELECT * FROM \"ReaderCategory\"",
                (rs, rowNum) -> new ReaderCategory(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        permittedIds.contains(rs.getInt("ID"))
                ));
    }

    public void savePermissions(Publication publication, List<Integer> permittedCategories) {
        jdbcTemplate.update("DELETE FROM \"PublicationPermissionToIssue\" WHERE \"PublicationNomenclatureNumber\" = ?",
                publication.getId());

        for (Integer categoryID : permittedCategories) {
            jdbcTemplate.update("INSERT INTO \"PublicationPermissionToIssue\"(\"PublicationNomenclatureNumber\", " +
                    "\"ReaderCategoryID\") VALUES (?, ?)",
                    publication.getId(),
                    categoryID);
        }
    }
}
