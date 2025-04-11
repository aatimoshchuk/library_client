package nsu.fit.repository;

import lombok.RequiredArgsConstructor;
import nsu.fit.data.access.Publication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WrittenOffPublicationRepository {
    private static final Logger logger = LoggerFactory.getLogger(WrittenOffPublicationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public void markPublicationAsWrittenOff(Publication publication) {
        try {
            jdbcTemplate.update("INSERT INTO \"WrittenOffPublications\"(\"PublicationNomenclatureNumber\", " +
                            "\"WriteOffDate\") VALUES (?, CURRENT_DATE)",
                    publication.getId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
