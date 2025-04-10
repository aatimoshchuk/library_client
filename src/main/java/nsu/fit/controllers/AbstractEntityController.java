package nsu.fit.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import nsu.fit.controllers.category_controllers.StudentsController;
import nsu.fit.data.access.AbstractEntity;
import nsu.fit.repository.AbstractEntityRepository;
import nsu.fit.service.UserService;
import nsu.fit.util.TableColumnConfigurator;
import nsu.fit.view.NotificationService;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractEntityController<T extends AbstractEntity, U extends AbstractEntityRepository<T>> {

    protected final FxWeaver fxWeaver;
    protected final U entityRepository;
    protected final UserService userService;
    protected final NotificationService notificationService;
    protected final TableColumnConfigurator tableColumnConfigurator;

    @FXML
    protected TableView<T> entitiesTable;
    @FXML
    protected AnchorPane actionPanel;
    @FXML
    protected Button addButton;
    @FXML
    protected Button saveButton;
    @FXML
    protected Button deleteButton;

    protected T selectedEntity;
    protected List<T> entities;
    protected ObjectProperty<TableRow<T>> lastSelectedRow = new SimpleObjectProperty<>();

    public abstract void initialize();
    protected abstract void applyUserPermissions();
    protected abstract T createEntity();

    public void initializeBase() {
        entitiesTable.setRowFactory(tableView -> {
            TableRow<T> row = new TableRow<>();
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    lastSelectedRow.set(row);
                }
            });
            return row ;
        });

        entitiesTable.addEventFilter(ScrollEvent.ANY, event -> actionPanel.setVisible(false));

        loadData();
        applyUserPermissions();
    }

    public void handleRowClick(MouseEvent mouseEvent) {
        selectedEntity = entitiesTable.getSelectionModel().getSelectedItem();

        if (selectedEntity != null) {
            TableRow<T> row = lastSelectedRow.get();
            Bounds panelBounds = actionPanel.getParent().sceneToLocal(row.localToScene(row.getBoundsInLocal()));

            actionPanel.setLayoutY(panelBounds.getMaxY());
            actionPanel.setLayoutX(panelBounds.getMinX());
            actionPanel.setVisible(true);

            saveButton.setOnAction(e -> saveEntity(selectedEntity));
            deleteButton.setOnAction(e -> deleteEntity(selectedEntity));

            setCustomButtonActions();
        }
    }

    public void saveEntity(T entity) {
        String error = entityRepository.saveEntity(entity);

        if (error != null) {
            notificationService.showWarning(error);
            return;
        }

        loadData();
        actionPanel.setVisible(false);
    }

    public void deleteEntity(T entity) {
        entityRepository.deleteEntity(entity);
        loadData();
        actionPanel.setVisible(false);
    }

    public void addEntity(ActionEvent actionEvent) {
        entities.add(createEntity());
        entitiesTable.getItems().setAll(entities);
        actionPanel.setVisible(false);
    }

    public void switchToHomePage(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToLibraries(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(LibrariesController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToLibrarians(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(LibrariansController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToCategories(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(StudentsController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToLiteraryWorks(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(LiteraryWorksController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToReaders(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(ReadersController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToPublications(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToHistory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToWrittenOffPublications(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(MainController.class);
        stage.setScene(new Scene(root));
    }

    protected void setCustomButtonActions() {

    }

    protected void loadData() {
        entities = entityRepository.findAll();
        entitiesTable.getItems().setAll(entities);
    }

    protected boolean validateDate(String date) {
        if (date.isEmpty()) {
            notificationService.showWarning("Невозможно выполнить запрос: поля не должны быть пустыми!");
            return false;
        }

        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            notificationService.showWarning("Невозможно выполнить запрос: даты должны быть в виде \"YYYY-MM-DD\"");
            return false;
        }

        return true;
    }

    protected boolean validateNumber(String stringNumber) {
        try {
            if (Integer.parseInt(stringNumber) <= 0) {
                notificationService.showWarning("Невозможно выполнить запрос: значение поля должно представлять " +
                        "собой положительное число.");
                return false;
            }
        } catch (NumberFormatException e) {
            notificationService.showWarning("Невозможно выполнить запрос: значение поля должно представлять " +
                    "собой положительное число.");
            return false;
        }

        return true;
    }
}
