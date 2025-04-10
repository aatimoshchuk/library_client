package nsu.fit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.service.UserService;

import org.springframework.stereotype.Component;

@Component
@FxmlView("login.fxml")
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final FxWeaver fxWeaver;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    public void handleLogin(ActionEvent actionEvent) {
        try {
            userService.connectToDatabase(usernameField.getText(), passwordField.getText());
            errorLabel.setText("");
            switchScene();
        } catch (Exception e) {
            errorLabel.setText("Ошибка входа: введен неправильный логин и / или пароль");
        }
    }

    private void switchScene() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }
}

