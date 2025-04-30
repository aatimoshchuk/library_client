package nsu.fit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.repository.LibraryRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("readers.fxml")
public class ReadersController extends AbstractEntityController<Reader, ReaderRepository> {
    private final PublicationRepository publicationRepository;
    private final LibraryRepository libraryRepository;

    @FXML
    private TableColumn<Reader, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Reader, String> surnameColumn;
    @FXML
    private TableColumn<Reader, String> nameColumn;
    @FXML
    private TableColumn<Reader, String> patronymicColumn;
    @FXML
    private TableColumn<Reader, String> birthDayColumn;
    @FXML
    private TableColumn<Reader, String> categoryColumn;

    @FXML
    private Button getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegisteredButton;
    @FXML
    private Button getPublicationsThatIssuedFromLibraryWhereReaderIsRegisteredButton;
    @FXML
    private Button getLibrariesWhereReaderIsRegisteredButton;

    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;
    @FXML
    private TextField libraryIdField;

    public ReadersController(FxWeaver fxWeaver, ReaderRepository entityRepository, UserService userService,
                             NotificationService notificationService, PublicationRepository publicationRepository,
                             TableColumnConfigurator tableColumnConfigurator, LibraryRepository libraryRepository) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.publicationRepository = publicationRepository;
        this.libraryRepository = libraryRepository;
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(libraryCardNumberColumn, "id");
        tableColumnConfigurator.configureEditableTextColumn(surnameColumn, "surname", Reader.class);
        tableColumnConfigurator.configureEditableTextColumn(nameColumn, "name", Reader.class);
        tableColumnConfigurator.configureEditableTextColumn(patronymicColumn, "patronymic", Reader.class);
        tableColumnConfigurator.configureEditableTextColumn(birthDayColumn, "birthDay", Reader.class);
        tableColumnConfigurator.configureNotEditableTextColumn(categoryColumn, "category");

        initializeBase();
    }

    @Override
    protected void applyUserPermissions() {
        entitiesTable.setEditable(true);

        if (userService.getUserRole().equals(UserRole.ADMIN_FOND)) {
            deleteButton.setVisible(true);
        }
    }

    @Override
    protected void setCustomButtonActions() {
        getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegisteredButton
                .setOnAction(e -> getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered(selectedEntity));
        getPublicationsThatIssuedFromLibraryWhereReaderIsRegisteredButton
                .setOnAction(e -> getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered(selectedEntity));
        getLibrariesWhereReaderIsRegisteredButton.setOnAction(e -> getLibrariesWhereReaderIsRegistered(selectedEntity));
    }

    @Override
    protected Reader createEntity() {
        return new Reader();
    }

    @Override
    protected boolean confirmDeletion(Reader entity) {
        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + entity.getSurname() +
                " " + entity.getName() + " " + entity.getPatronymic() + " из числа читателей?");
    }

    public void getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered(Reader reader) {
        List<Map<String, Object>> result =
                publicationRepository.getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered(reader);

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getLibrariesWhereReaderIsRegistered(Reader reader) {
        List<Map<String, Object>> result = libraryRepository.getLibrariesWhereReaderIsRegistered(reader);

        if (result.isEmpty()) {
            notificationService.showNotification("Читатель не зарегистрирован ни в одной из библиотек.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered(Reader reader) {
        List<Map<String, Object>> result =
                publicationRepository.getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered(reader);

        if (result.isEmpty()) {
            notificationService.showNotification("Список изданий пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getReadersWithExpiredPublications(ActionEvent actionEvent) {
        List<Map<String, Object>> result = entityRepository.getReadersWithExpiredPublications();

        if (result.isEmpty()) {
            notificationService.showNotification("Список читателей пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getReadersRegisteredInTheLibrary(ActionEvent actionEvent) {
        if (!validateNumber(libraryIdField.getText())) {
            return;
        }

        actionPanel.setVisible(false);

        entities = entityRepository.findEntities(Integer.parseInt(libraryIdField.getText()));
        if (!entities.isEmpty()) {
            entitiesTable.getItems().setAll(entities);
        } else {
            notificationService.showNotification("В данной библиотеке не зарегистрирован ни один читатель.");
        }
    }

    public void getReadersWhoHaveNotVisitedTheLibraryDuringThePeriod(ActionEvent actionEvent) {
        if (!validateDate(startDateField.getText()) || !validateDate(endDateField.getText())) {
            return;
        }

        List<Map<String, Object>> result = entityRepository.getReadersWhoHaveNotVisitedTheLibraryDuringThePeriod(
                startDateField.getText(),
                endDateField.getText());

        if (result.isEmpty()) {
            notificationService.showNotification("Список читателей пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }
}
