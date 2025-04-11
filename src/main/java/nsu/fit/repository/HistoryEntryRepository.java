package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Publication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoryEntryRepository {
    private static final Logger logger = LoggerFactory.getLogger(HistoryEntryRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public void markPublicationAsReturned(Publication publication) {
        try {
            jdbcTemplate.update("UPDATE \"HistoryOfIssueOfPublications\" SET \"ReturnDate\" = CURRENT_DATE WHERE " +
                            "\"PublicationNomenclatureNumber\" = ?",
                    publication.getId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public int getNumberOfDaysOverdue(Publication publication) {
        return jdbcTemplate.queryForObject(
                "SELECT \"getNumberOfDaysOverdue\"(?, CURRENT_DATE)",
                Integer.class,
                publication.getId());
    }
}
