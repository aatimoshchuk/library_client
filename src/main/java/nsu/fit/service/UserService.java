package nsu.fit.service;

import lombok.AllArgsConstructor;
import nsu.fit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserCredentialsDataSourceAdapter dataSourceAdapter;

    private final UserRepository userRepository;

    public void connectToDatabase(String username, String password) {
        dataSourceAdapter.setCredentialsForCurrentThread(username, password);
        userRepository.checkConnection();
        userRepository.setUsername(username);
    }
    public UserRole getUserRole() {
        return UserRole.valueOf(userRepository.getCurrentUserRole().toUpperCase());
    }
}
