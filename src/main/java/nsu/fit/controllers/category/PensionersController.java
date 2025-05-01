package nsu.fit.controllers.category;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Reader;
import nsu.fit.data.access.category.Pensioner;
import nsu.fit.repository.category.PensionerRepository;
import nsu.fit.repository.ReaderRepository;
import nsu.fit.service.UserService;
import nsu.fit.utils.ObjectToMapConverter;
import nsu.fit.utils.TableColumnConfigurator;
import nsu.fit.view.NotificationService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@FxmlView("pensioner_category.fxml")
public class PensionersController extends AbstractCategoryController<Pensioner, PensionerRepository> {

    @FXML
    private TableColumn<Pensioner, String> idColumn;
    @FXML
    private TableColumn<Pensioner, String> libraryCardNumberColumn;
    @FXML
    private TableColumn<Pensioner, String> pensionCertificateNumberColumn;

    public PensionersController(FxWeaver fxWeaver, PensionerRepository entityRepository, UserService userService,
                                NotificationService notificationService, ReaderRepository readerRepository,
                                TableColumnConfigurator tableColumnConfigurator, ObjectToMapConverter objectToMapConverter) {
        super(fxWeaver, entityRepository, userService, notificationService, readerRepository, tableColumnConfigurator, objectToMapConverter);
    }

    @FXML
    public void initialize() {
        tableColumnConfigurator.configureNotEditableTextColumn(idColumn, "id");
        tableColumnConfigurator.configureEditableNumberColumn(libraryCardNumberColumn,
                "libraryCardNumber", Pensioner.class);
        tableColumnConfigurator.configureEditableTextColumn(pensionCertificateNumberColumn,
                "pensionCertificateNumber", Pensioner.class);

        initializeBase();
        initializeNavigateButtons();
    }

    @Override
    protected List<Pensioner> getEntities() {
        return Collections.emptyList();
    }

    @Override
    protected Pensioner createEntity() {
        return new Pensioner();
    }

    @Override
    protected boolean confirmDeletion(Pensioner entity) {
        Reader reader = readerRepository.findOne(entity.getLibraryCardNumber());

        return notificationService.showConfirmationWindow(String.format("Вы действительно хотите удалить %s %s %s из " +
                "числа пенсионеров?", reader.getSurname(), reader.getName(), reader.getPatronymic()));
    }
}
