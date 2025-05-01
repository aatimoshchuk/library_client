package nsu.fit.controllers.category;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Student;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.repository.category.StudentRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("student_category.fxml")
public class StudentsController extends AbstractCategoryController<Student, StudentRepository> {

    @FXML
    private TableColumn<Student, String> idColumn;
    @FXML
    private TableColumn<Student, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Student, String> educationalInstitutionNameColumn;
    @FXML
    private TableColumn<Student, String> facultyColumn;
    @FXML
    private TableColumn<Student, String> courseColumn;
    @FXML
    private TableColumn<Student, String> groupNumberColumn;
    @FXML
    private TableColumn<Student, String> studentCardNumberColumn;
    @FXML
    private TableColumn<Student, String> extensionDateColumn;

    @FXML
    private TextField educationalInstitutionNameField;
    @FXML
    private TextField facultyField;
    @FXML
    private TextField courseField;

    public StudentsController(FxWeaver fxWeaver, StudentRepository entityRepository, UserService userService,
                              NotificationService notificationService, ReaderRepository readerRepository,
                              TableColumnConfigurator tableColumnConfigurator, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator,
                objectToMapConverter);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn, "libraryCardNumber",
                Student.class);
        tableColumnConfigurator.configureEditableTextColumn(educationalInstitutionNameColumn,
                "educationalInstitutionName", Student.class);
        tableColumnConfigurator.configureEditableTextColumn(facultyColumn, "faculty", Student.class);
        tableColumnConfigurator.configureEditableNumberColumn(courseColumn, "course", Student.class);
        tableColumnConfigurator.configureEditableNumberColumn(groupNumberColumn, "groupNumber", Student.class);
        tableColumnConfigurator.configureEditableNumberColumn(studentCardNumberColumn, "studentCardNumber",
                Student.class);
        tableColumnConfigurator.configureEditableTextColumn(extensionDateColumn, "extensionDate",
                Student.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Student> getEntities() {
        return entityRepository.findEntities(educationalInstitutionNameField.getText().trim(),
                facultyField.getText().trim(), courseField.getText().trim());
    }

    @Override
    protected Student createEntity() {
        return new Student();
    }

    @Override
    protected boolean confirmDeletion(Student entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow(String.format("Вы действительно хотите удалить %s %s %s из " +
                "числа студентов?", reader.getSurname(), reader.getName(), reader.getPatronymic()));
    }
}
