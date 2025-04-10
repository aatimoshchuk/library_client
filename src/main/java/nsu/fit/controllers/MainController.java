package nsu.fit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.controllers.category_controllers.StudentsController;
import org.springframework.stereotype.Component;

@Component
@FxmlView("home_page.fxml")
@RequiredArgsConstructor
public class MainController {

    private final FxWeaver fxWeaver;

    @FXML
    private ImageView libraryIcon;

    public void switchToLibraries(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(LibrariesController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToLibrarians(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(LibrariansController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToReaders(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(ReadersController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToCategories(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(StudentsController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToLiteraryWorks(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(LiteraryWorksController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToPublications(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToHistory(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToWrittenOffPublications(ActionEvent actionEvent) {
        Stage stage = (Stage) libraryIcon.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }
}
