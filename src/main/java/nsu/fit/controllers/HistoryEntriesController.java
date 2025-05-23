package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.HistoryEntry;
import nsu.fit.data.access.Librarian;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.Reader;
import nsu.fit.repository.HistoryEntryRepository;
import nsu.fit.repository.LibrarianRepository;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

@Component
@FxmlView("history.fxml")
public class HistoryEntriesController extends AbstractEntityController<HistoryEntry, HistoryEntryRepository> {

    private final ReaderRepository readerRepository;
    private final PublicationRepository publicationRepository;
    private final LibrarianRepository librarianRepository;
    private final ObjectToMapConverter objectToMapConverter;

    @FXML
    private TableColumn<HistoryEntry, String> idColumn;
    @FXML
    private TableColumn<HistoryEntry, String> publicationNomenclatureNumberColumn;
    @FXML
    private TableColumn<HistoryEntry, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<HistoryEntry, String> issueDateColumn;
    @FXML
    private TableColumn<HistoryEntry, String> returnDateColumn;
    @FXML
    private TableColumn<HistoryEntry, String> librarianIdColumn;

    @FXML
    private Button markPublicationAsReturnedButton;
    @FXML
    private Button getPublicationInfoButton;
    @FXML
    private Button getLibraryCardButton;
    @FXML
    private Button getLibrarianInfoButton;

    public HistoryEntriesController(FxWeaver fxWeaver, HistoryEntryRepository entityRepository, UserService userService, NotificationService notificationService, TableColumnConfigurator tableColumnConfigurator, ReaderRepository readerRepository, PublicationRepository publicationRepository, LibrarianRepository librarianRepository, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.readerRepository = readerRepository;
        this.publicationRepository = publicationRepository;
        this.librarianRepository = librarianRepository;
        this.objectToMapConverter = objectToMapConverter;
    }

    public void markPublicationAsReturned(HistoryEntry historyEntry) {
        if (historyEntry.getPublicationNomenclatureNumber() == null) {
            notificationService.showNotification("Издание с таким номенклатурным номером не найдено.");
            return;
        }

        int numberOfDaysOverdue = entityRepository.getNumberOfDaysOverdue(historyEntry.getPublicationNomenclatureNumber());
        Warning warning = entityRepository.markPublicationAsReturned(historyEntry.getPublicationNomenclatureNumber());

        if (warning != null) {
            notificationService.showWarning(warning);
            return;
        }

        if (numberOfDaysOverdue > 0) {
            notificationService.showWarning(new Warning(WarningType.WARNING,
                    String.format("Срок возврата издания истек %d дней назад! Издание было успешно возвращено.",
                            numberOfDaysOverdue)));
        } else {
            notificationService.showNotification("Издание было успешно возвращено.");
        }

        loadData();
        actionPanel.setVisible(false);
    }

    public void getPublicationInfo(HistoryEntry historyEntry) {
        if (historyEntry.getPublicationNomenclatureNumber() == null) {
            notificationService.showNotification("Издание с таким номенклатурным номером не найдено.");
            return;
        }

        Publication publication = publicationRepository.findOne(historyEntry.getPublicationNomenclatureNumber());
        if (publication != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(publication));
        } else {
            notificationService.showNotification("Издание с таким номенклатурным номером не найдено.");
        }
    }

    public void getLibraryCard(HistoryEntry historyEntry) {
        if (historyEntry.getLibraryCardNumber() == null) {
            notificationService.showNotification("Читатель с таким номером читательского билета не найден.");
            return;
        }

        Reader reader = readerRepository.findOne(historyEntry.getLibraryCardNumber());
        if (reader != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(reader));
        } else {
            notificationService.showNotification("Читатель с таким номером читательского билета не найден.");
        }
    }

    public void getLibrarianInfo(HistoryEntry historyEntry) {
        if (historyEntry.getLibrarianID() == null) {
            notificationService.showNotification("Библиотекарь с таким ID не найден.");
            return;
        }

        Librarian librarian = librarianRepository.findOne(historyEntry.getLibrarianID());
        if (librarian != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(librarian));
        } else {
            notificationService.showNotification("Библиотекарь с таким ID не найден.");
        }
    }

    @Override
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(publicationNomenclatureNumberColumn,
                "publicationNomenclatureNumber", HistoryEntry.class);
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                HistoryEntry.class);
        tableColumnConfigurator.configureEditableTextColumn(issueDateColumn, "issueDate", HistoryEntry.class);
        tableColumnConfigurator.configureEditableTextColumn(returnDateColumn, "returnDate", HistoryEntry.class);
        tableColumnConfigurator.configureEditableNumberColumn(librarianIdColumn, "librarianID", HistoryEntry.class);

        initializeBase();
    }

    @Override
    protected void applyUserPermissions() {
        entitiesTable.setEditable(true);

        saveButton.setVisible(true);
        addButton.setVisible(true);

        if (userService.getUserRole().equals(UserRole.ADMIN_FOND)) {
            deleteButton.setVisible(true);
        }
    }

    @Override
    protected HistoryEntry createEntity() {
        return new HistoryEntry();
    }

    @Override
    protected boolean confirmDeletion(HistoryEntry entity) {
        return notificationService.showConfirmationWindow(String.format(
                "Вы действительно хотите удалить запись о выдаче издания %d читателю %d?",
                entity.getPublicationNomenclatureNumber(),
                entity.getLibraryCardNumber()));
    }

    @Override
    protected void setCustomButtonActions() {
        if (selectedEntity.getReturnDate() == null || selectedEntity.getReturnDate().isEmpty()) {
            markPublicationAsReturnedButton.setVisible(true);
            markPublicationAsReturnedButton.setOnAction(e -> markPublicationAsReturned(selectedEntity));
        } else {
            markPublicationAsReturnedButton.setVisible(false);
        }

        getPublicationInfoButton.setOnAction(e -> getPublicationInfo(selectedEntity));
        getLibraryCardButton.setOnAction(e -> getLibraryCard(selectedEntity));
        getLibrarianInfoButton.setOnAction(e -> getLibrarianInfo(selectedEntity));
    }
}
