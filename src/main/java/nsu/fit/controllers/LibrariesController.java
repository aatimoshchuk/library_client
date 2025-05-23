package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Library;
import nsu.fit.repository.LibrarianRepository;
import nsu.fit.repository.LibraryRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.utils.warning.Warning;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@FxmlView("libraries.fxml")
public class LibrariesController extends AbstractEntityController<Library, LibraryRepository> {
    private final LibrarianRepository librarianRepository;
    private final ReaderRepository readerRepository;
    private final PublicationRepository publicationRepository;

    @FXML
    private TableColumn<Library, String> idColumn;
    @FXML
    private TableColumn<Library, String> nameColumn;
    @FXML
    private TableColumn<Library, String> addressColumn;

    @FXML
    private Button getLibrariansWhoWorksInTheRoomButton;
    @FXML
    private Button getIssuedPublicationsButton;
    @FXML
    private Button getReadersWhoRegisteredInTheLibraryButton;
    @FXML
    private Button registerReaderButton;
    @FXML
    private Button unregisterReaderButton;

    @FXML
    private TextField roomNumberField;
    @FXML
    private TextField libraryRoomNumberField;
    @FXML
    private TextField shelvingNumberField;
    @FXML
    private TextField shelfNumberField;
    @FXML
    private TextField libraryCardNumberField;

    public LibrariesController(FxWeaver fxWeaver, LibraryRepository entityRepository, UserService userService,
                               NotificationService notificationService, LibrarianRepository librarianRepository,
                               PublicationRepository publicationRepository, TableColumnConfigurator tableColumnConfigurator, ReaderRepository readerRepository) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.librarianRepository = librarianRepository;
        this.publicationRepository = publicationRepository;
        this.readerRepository = readerRepository;
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableTextColumn(nameColumn, "name", Library.class);
        tableColumnConfigurator.configureEditableTextColumn(addressColumn, "address", Library.class);

        initializeBase();
    }

    @Override
    protected void applyUserPermissions() {
        if (userService.getUserRole().equals(UserRole.ADMIN_FOND)) {
            saveButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true);

            entitiesTable.setEditable(true);
        }
    }

    @Override
    protected void setCustomButtonActions() {
        getLibrariansWhoWorksInTheRoomButton.setOnAction(e -> getLibrariansWhoWorksInTheRoom(selectedEntity));
        getIssuedPublicationsButton.setOnAction(e -> getIssuedPublications(selectedEntity));
        getReadersWhoRegisteredInTheLibraryButton.setOnAction(e -> getReadersWhoRegisteredInTheLibrary(selectedEntity));
        registerReaderButton.setOnAction(e -> registerReaderInTheLibrary(selectedEntity));
        unregisterReaderButton.setOnAction(e -> unregisterReaderInTheLibrary(selectedEntity));
    }

    public void getLibrariansWhoWorksInTheRoom(Library library) {
        if (!validateNumber(roomNumberField.getText())) {
            return;
        }

        List<Map<String, Object>> result = librarianRepository.getLibrariansWhoWorksInTheRoom(library,
                Integer.parseInt(roomNumberField.getText()));

        if (result.isEmpty()) {
            notificationService.showNotification("Список библиотекарей, работающих в данном читальном зале, пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }

    }

    public void getReadersWhoRegisteredInTheLibrary(Library library) {
        List<Map<String, Object>> result = readerRepository.getReadersWhoRegisteredInTheLibrary(library);

        if (result.isEmpty()) {
            notificationService.showNotification("Список читателей, зарегистрированных в данной библиотеке, пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    public void getIssuedPublications(Library library) {
        if (!validateNumber(libraryRoomNumberField.getText()) || !validateNumber(shelvingNumberField.getText()) ||
                !validateNumber(shelfNumberField.getText())) {
            return;
        }

        List<Map<String, Object>> result =
                publicationRepository.getPublicationsThatIssuedFromTheStorageLocation(library,
                        Integer.parseInt(libraryRoomNumberField.getText()),
                        Integer.parseInt(shelvingNumberField.getText()),
                        Integer.parseInt(shelfNumberField.getText()));

        if (result.isEmpty()) {
            notificationService.showNotification("Список литературы, выданной с данной полки, пуст.");
        } else {
            notificationService.showResultsInTableView(result);
        }

    }

    public void registerReaderInTheLibrary(Library library) {
        if (!validateNumber(libraryCardNumberField.getText())) {
            return;
        }

        Warning warning = entityRepository.registerReaderInTheLibrary(library.getId(),
                Integer.parseInt(libraryCardNumberField.getText()));

        if (warning != null) {
            notificationService.showWarning(warning);
        } else {
            notificationService.showNotification(String.format("Читатель с номером читательского билета %s успешно " +
                    "зарегистрирован в библиотеке c ID = %d.", libraryCardNumberField.getText(), library.getId()));
        }
    }

    public void unregisterReaderInTheLibrary(Library library) {
        if (!validateNumber(libraryCardNumberField.getText())) {
            return;
        }

        Warning warning = entityRepository.unregisterReaderInTheLibrary(library.getId(),
                Integer.parseInt(libraryCardNumberField.getText()));

        if (warning != null) {
            notificationService.showWarning(warning);
        } else {
            notificationService.showNotification(String.format("Регистрация читателя с номером читательского билета " +
                    "%s в библиотеке с ID = %d успешно отменена.", libraryCardNumberField.getText(), library.getId()));
        }
    }

    @Override
    protected Library createEntity() {
        return new Library();
    }

    @Override
    protected boolean confirmDeletion(Library entity) {
        return notificationService.showConfirmationWindow(String.format("Вы действительно хотите удалить %s из списка" +
                        " библиотек?", entity.getName()));
    }
}
