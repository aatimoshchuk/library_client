package nsu.fit.controllers.category;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Schoolchild;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category.SchoolchildRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("schoolchild_category.fxml")
public class SchoolchildrenController extends AbstractCategoryController<Schoolchild, SchoolchildRepository> {

    @FXML
    private TableColumn<Schoolchild, String> idColumn;
    @FXML
    private TableColumn<Schoolchild, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Schoolchild, String> educationalInstitutionNameColumn;
    @FXML
    private TableColumn<Schoolchild, String> gradeColumn;
    @FXML
    private TableColumn<Schoolchild, String> extensionDateColumn;

    @FXML
    private TextField educationalInstitutionNameField;
    @FXML
    private TextField gradeField;

    public SchoolchildrenController(FxWeaver fxWeaver, SchoolchildRepository entityRepository, UserService userService,
                                    NotificationService notificationService, ReaderRepository readerRepository,
                                    TableColumnConfigurator tableColumnConfigurator, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator,
                objectToMapConverter);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                Schoolchild.class);
        tableColumnConfigurator.configureEditableTextColumn(educationalInstitutionNameColumn,
                "educationalInstitutionName", Schoolchild.class);
        tableColumnConfigurator.configureEditableNumberColumn(gradeColumn, "grade", Schoolchild.class);
        tableColumnConfigurator.configureEditableTextColumn(extensionDateColumn, "extensionDate", Schoolchild.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Schoolchild> getEntities() {
        return entityRepository.findEntities(educationalInstitutionNameField.getText().trim(),
                gradeField.getText().trim());
    }

    @Override
    protected Schoolchild createEntity() {
        return new Schoolchild();
    }

    @Override
    protected boolean confirmDeletion(Schoolchild entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow(String.format("Вы действительно хотите удалить %s %s %s из " +
                "числа школьников?", reader.getSurname(), reader.getName(), reader.getPatronymic()));
    }
}
