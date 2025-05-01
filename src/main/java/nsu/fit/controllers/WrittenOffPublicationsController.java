package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.WrittenOffPublication;
import nsu.fit.repository.PublicationRepository;
import nsu.fit.repository.WrittenOffPublicationRepository;
import nsu.fit.service.UserRole;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.utils.warning.Warning;
import nsu.fit.utils.warning.WarningType;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

@Component
@FxmlView("written_off_publications.fxml")
public class WrittenOffPublicationsController extends AbstractEntityController<WrittenOffPublication,
        WrittenOffPublicationRepository> {

    private final PublicationRepository publicationRepository;
    private final ObjectToMapConverter objectToMapConverter;

    @FXML
    private TableColumn<WrittenOffPublication, String> idColumn;
    @FXML
    private TableColumn<WrittenOffPublication, String> publicationNomenclatureNumberColumn;
    @FXML
    private TableColumn<WrittenOffPublication, String> writeOffDateColumn;

    @FXML
    private Button getPublicationInfoButton;

    public WrittenOffPublicationsController(FxWeaver fxWeaver, WrittenOffPublicationRepository entityRepository, UserService userService, NotificationService notificationService, TableColumnConfigurator tableColumnConfigurator, PublicationRepository publicationRepository, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, tableColumnConfigurator);
        this.publicationRepository = publicationRepository;
        this.objectToMapConverter = objectToMapConverter;
    }

    public void getPublicationInfo(WrittenOffPublication writtenOffPublication) {
        Publication publication = publicationRepository.findOne(writtenOffPublication.getPublicationNomenclatureNumber());
        if (publication != null) {
            notificationService.showResultInStringView(objectToMapConverter.convert(publication));
        } else {
            notificationService.showNotification("Издание не найдено.");
        }
    }

    @Override
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(publicationNomenclatureNumberColumn,
                "publicationNomenclatureNumber", WrittenOffPublication.class);
        tableColumnConfigurator.configureEditableTextColumn(writeOffDateColumn, "writeOffDate",
                WrittenOffPublication.class);

        initializeBase();
    }

    @Override
    protected void applyUserPermissions() {
        entitiesTable.setEditable(true);
        addButton.setVisible(true);
        saveButton.setVisible(true);

        if (!userService.getUserRole().equals(UserRole.LIBRARIAN)) {
            deleteButton.setVisible(true);
        }
    }

    @Override
    protected WrittenOffPublication createEntity() {
        return new WrittenOffPublication();
    }

    @Override
    protected boolean confirmDeletion(WrittenOffPublication entity) {
        return notificationService.showConfirmationWindow(String.format("Вы действительно хотите удалить издание с " +
                "номенклатурным номером %d из числа списанных?", entity.getPublicationNomenclatureNumber()));
    }

    @Override
    public void saveEntity(WrittenOffPublication entity) {
        if (userService.getUserRole().equals(UserRole.LIBRARIAN) && entity.getId() != 0) {
            notificationService.showWarning(new Warning(WarningType.SAVING_ERROR,
                    "Недостаточно прав для редактирования записи"));
        } else {
            Warning warning = entityRepository.saveEntity(entity);

            if (warning != null) {
                notificationService.showWarning(warning);
                return;
            }
        }

        loadData();
        actionPanel.setVisible(false);
    }

    @Override
    public void deleteEntity(WrittenOffPublication entity) {
        if (confirmDeletion(entity)) {
            entityRepository.deleteEntity(entity);
            notificationService.showNotification("Издание удалено из числа списанных. Разметите его в одном из мест " +
                    "хранения и внесите информацию в базу данных.");
            loadData();
            actionPanel.setVisible(false);
        }
    }

    @Override
    protected void setCustomButtonActions() {
        getPublicationInfoButton.setOnAction(e -> getPublicationInfo(selectedEntity));
    }
}
