package nsu.fit.repository;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private String username;

    public String getCurrentUserRole() {
        List<String> roles = jdbcTemplate.queryForList(
                "SELECT rolname FROM pg_roles WHERE pg_has_role(?, oid, 'member')",
                String.class,
                username);

        return roles.stream()
                .filter(role -> role.equals("librarian") || role.equals("admin_fond") || role.equals("admin_library"))
                .findFirst()
                .orElse(null);
    }

    public void checkConnection() {
        jdbcTemplate.execute("SELECT 1");
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
