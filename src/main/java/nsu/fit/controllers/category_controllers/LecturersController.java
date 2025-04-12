package nsu.fit.controllers.category_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Lecturer;
import nsu.fit.repository.category_repository.LecturerRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("lecturer_category.fxml")
public class LecturersController extends AbstractCategoryController<Lecturer, LecturerRepository> {

    @FXML
    private TableColumn<Lecturer, String> idColumn;
    @FXML
    private TableColumn<Lecturer, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Lecturer, String> educationalInstitutionNameColumn;
    @FXML
    private TableColumn<Lecturer, String> jobTitleColumn;

    @FXML
    private TextField educationalInstitutionNameField;
    @FXML
    private TextField jobTitleField;

    public LecturersController(FxWeaver fxWeaver, LecturerRepository entityRepository, UserService userService,
                               NotificationService notificationService, ReaderRepository readerRepository,
                               TableColumnConfigurator tableColumnConfigurator, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator,
                objectToMapConverter);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn,
                "libraryCardNumber", Lecturer.class);
        tableColumnConfigurator.configureEditableTextColumn(educationalInstitutionNameColumn,
                "educationalInstitutionName", Lecturer.class);
        tableColumnConfigurator.configureEditableTextColumn(jobTitleColumn, "jobTitle", Lecturer.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Lecturer> getEntities() {
        return entityRepository.findEntities(educationalInstitutionNameField.getText().trim(),
                jobTitleField.getText().trim());
    }

    @Override
    protected Lecturer createEntity() {
        return new Lecturer();
    }

    @Override
    protected boolean confirmDeletion(Lecturer entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow("Вы действительно хотите удалить " + reader.getSurname() +
                " " + reader.getName() + " " + reader.getPatronymic() + " из числа преподавателей?");
    }
}
