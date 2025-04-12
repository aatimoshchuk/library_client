package nsu.fit.controllers.category_controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import nsu.fit.controllers.AbstractEntityController;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.AbstractCategoryEntity;
import nsu.fit.repository.AbstractEntityRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import nsu.fit.view.ViewConstants;

import java.util.List;

public abstract class AbstractCategoryController<T extends AbstractCategoryEntity,
        U extends AbstractEntityRepository<T>> extends AbstractEntityController<T, U> {

    public final ReaderRepository readerRepository;
    private final ObjectToMapConverter objectToMapConverter;

    @FXML
    protected Button getLibraryCardButton;
    @FXML
    protected Button lecturerButton;
    @FXML
    protected ImageView lecturerIcon;
    @FXML
    protected Button scienceButton;
    @FXML
    protected ImageView scienceIcon;
    @FXML
    protected Button pensionerButton;
    @FXML
    protected ImageView pensionerIcon;
    @FXML
    protected Button studentButton;
    @FXML
    protected ImageView studentIcon;
    @FXML
    protected Button schoolButton;
    @FXML
    protected ImageView schoolIcon;

    public AbstractCategoryController(FxWeaver fxWeaver, U entityRepository, UserService userService,
                                      NotificationService notificationService, ReaderRepository readerRepository,
                                      TableColumnConfigurator tableColumnConfigurator, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.readerRepository = readerRepository;
        this.objectToMapConverter = objectToMapConverter;
    }

    protected abstract List<T> getEntities();

    public void findEntities(ActionEvent actionEvent) {
        actionPanel.setVisible(false);

        entities = getEntities();
        if (!entities.isEmpty()) {
            entitiesTable.getItems().setAll(entities);
        } else {
            notificationService.showNotification("Список преподавателей пуст.");
        }
    }

    public void switchToLecturerCategory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(LecturersController.class);
        stage.setScene(new Scene(root));
    }

    public void getLibraryCard(T entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());
        if (reader != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(reader));
        } else {
            notificationService.showNotification("Читатель с таким номер читательского билета не найден.");
        }
    }

    public void switchToScienceWorkerCategory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(ScientificWorkersController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToPensionerCategory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(PensionersController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToStudentCategory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(StudentsController.class);
        stage.setScene(new Scene(root));
    }

    public void switchToSchoolchildrenCategory(ActionEvent actionEvent) {
        Stage stage = (Stage) entitiesTable.getScene().getWindow();
        Parent root = fxWeaver.loadView(SchoolchildrenController.class);
        stage.setScene(new Scene(root));
    }

    protected void initializeNavigateButtons() {
        lecturerButton.setOnMouseEntered(e -> lecturerIcon.setImage(ViewConstants.hoverLecturerImage));
        lecturerButton.setOnMouseExited(e -> lecturerIcon.setImage(ViewConstants.normalLecturerImage));

        scienceButton.setOnMouseEntered(e -> scienceIcon.setImage(ViewConstants.hoverScienceImage));
        scienceButton.setOnMouseExited(e -> scienceIcon.setImage(ViewConstants.normalScienceImage));

        pensionerButton.setOnMouseEntered(e -> pensionerIcon.setImage(ViewConstants.hoverPensionerImage));
        pensionerButton.setOnMouseExited(e -> pensionerIcon.setImage(ViewConstants.normalPensionerImage));

        studentButton.setOnMouseEntered(e -> studentIcon.setImage(ViewConstants.hoverStudentImage));
        studentButton.setOnMouseExited(e -> studentIcon.setImage(ViewConstants.normalStudentImage));

        schoolButton.setOnMouseEntered(e -> schoolIcon.setImage(ViewConstants.hoverSchoolImage));
        schoolButton.setOnMouseExited(e -> schoolIcon.setImage(ViewConstants.normalSchoolImage));
    }

    @Override
    protected void applyUserPermissions() {
        entitiesTable.setEditable(true);
    }

    @Override
    protected void setCustomButtonActions() {
        getLibraryCardButton.setOnAction(e -> getLibraryCard(selectedEntity));
    }
}
