package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Library;
import nsu.fit.data.access.StorageLocation;
import nsu.fit.repository.LibraryRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.StorageLocationRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@FxmlView("storage_locations.fxml")
public class StorageLocationController extends AbstractEntityController<StorageLocation, StorageLocationRepository> {

    private final LibraryRepository libraryRepository;
    private final PublicationRepository publicationRepository;
    private final ObjectToMapConverter objectToMapConverter;

    @FXML
    private TableColumn<StorageLocation, String> idColumn;
    @FXML
    private TableColumn<StorageLocation, String> libraryIdColumn;
    @FXML
    private TableColumn<StorageLocation, String> roomNumberColumn;
    @FXML
    private TableColumn<StorageLocation, String> shelvingNumberColumn;
    @FXML
    private TableColumn<StorageLocation, String> shelfNumberColumn;

    @FXML
    private Button getLibraryInfoButton;
    @FXML
    private Button getPublicationsInStorageLocationButton;

    public StorageLocationController(FxWeaver fxWeaver, StorageLocationRepository entityRepository, UserService userService, NotificationService notificationService, TableColumnConfigurator tableColumnConfigurator, LibraryRepository libraryRepository, PublicationRepository publicationRepository, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.libraryRepository = libraryRepository;
        this.publicationRepository = publicationRepository;
        this.objectToMapConverter = objectToMapConverter;
    }

    @Override
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryIdColumn, "libraryID",
                StorageLocation.class);
        tableColumnConfigurator.configureEditableNumberColumn(roomNumberColumn, "roomNumber",
                StorageLocation.class);
        tableColumnConfigurator.configureEditableNumberColumn(shelvingNumberColumn, "shelvingNumber",
                StorageLocation.class);
        tableColumnConfigurator.configureEditableNumberColumn(shelfNumberColumn, "shelfNumber",
                StorageLocation.class);

        initializeBase();
    }

    public void getLibraryInfo(StorageLocation storageLocation) {
        if (storageLocation.getLibraryID() == null) {
            notificationService.showNotification("Библиотека с таким ID не найдена.");
            return;
        }

        Library library = libraryRepository.findOne(storageLocation.getLibraryID());

        if (library != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(library));
        } else {
            notificationService.showNotification("Библиотека с таким ID не найдена.");
        }
    }

    public void getPublicationsInStorageLocation(StorageLocation storageLocation) {
        List<Map<String, Object>> result =
                publicationRepository.getPublicationsInStorageLocation(storageLocation.getId());

        if (result.isEmpty()) {
            notificationService.showNotification("В данном месте хранения не размещено ни одного издания.");
        } else {
            notificationService.showResultsInTableView(result);
        }
    }

    @Override
    protected void applyUserPermissions() {
        UserRole currUserRole =  userService.getUserRole();

        if (currUserRole.equals(UserRole.ADMIN_FOND) || currUserRole.equals(UserRole.ADMIN_LIBRARY)) {
            entitiesTable.setEditable(true);

            saveButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true);
        }
    }

    @Override
    protected StorageLocation createEntity() {
        return new StorageLocation();
    }

    @Override
    protected boolean confirmDeletion(StorageLocation entity) {
        return notificationService.showConfirmationWindow(String.format(
                "Вы действительно хотите удалить место хранения с ID = %d?", entity.getId()));
    }

    @Override
    protected void setCustomButtonActions() {
        getLibraryInfoButton.setOnAction(e -> getLibraryInfo(selectedEntity));
        getPublicationsInStorageLocationButton.setOnAction(e -> getPublicationsInStorageLocation(selectedEntity));
    }
}
