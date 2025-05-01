package nsu.fit.config;

import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.PerUserPoolDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter;

@Configuration
public class DatabaseConfig {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public UserCredentialsDataSourceAdapter dataSource() throws ClassNotFoundException {
        UserCredentialsDataSourceAdapter adapter = new UserCredentialsDataSourceAdapter();
        adapter.setTargetDataSource(perUserPoolDataSource());
        return adapter;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(UserCredentialsDataSourceAdapter dataSourceAdapter) {
        return new JdbcTemplate(dataSourceAdapter);
    }

    private PerUserPoolDataSource perUserPoolDataSource() throws ClassNotFoundException {
        DriverAdapterCPDS driverAdapter = new DriverAdapterCPDS();
        driverAdapter.setDriver(driverClassName);
        driverAdapter.setUrl(url);
        driverAdapter.setUser("default");
        driverAdapter.setPassword("default");
        driverAdapter.setAccessToUnderlyingConnectionAllowed(true);

        PerUserPoolDataSource pool = new PerUserPoolDataSource();
        pool.setConnectionPoolDataSource(driverAdapter);

        return pool;
    }
}
