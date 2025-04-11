package nsu.fit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import nsu.fit.data.access.Publication;
import nsu.fit.data.access.ReaderCategory;
import nsu.fit.repository.PublicationPermissionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView("permission-dialog.fxml")
@RequiredArgsConstructor
public class PublicationPermissionDialogController {
    private final PublicationPermissionRepository publicationPermissionRepository;
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    @Setter
    private Publication publication;

    @FXML
    private VBox checkboxContainer;

    public void loadData() {
        List<ReaderCategory> categories = publicationPermissionRepository.findAllForPublication(publication);

        checkboxContainer.getChildren().clear();
        checkBoxes.clear();

        for (ReaderCategory category : categories) {
            CheckBox checkBox = new CheckBox(category.getReaderCategoryName());
            checkBox.setUserData(category.getReaderCategoryID());
            checkBox.setSelected(category.isPermitted());
            checkBox.setStyle("-fx-font-size: 14px; -fx-font-family: 'Hlebozavod Medium'; -fx-text-fill: #7b6f6f" );
            checkBoxes.add(checkBox);
            checkboxContainer.getChildren().add(checkBox);
        }
    }

    @FXML
    private void onSaveClick() {
        List<Integer> permittedIDs = checkBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (Integer) cb.getUserData())
                .collect(Collectors.toList());

        publicationPermissionRepository.savePermissions(publication, permittedIDs);

        ((Stage) checkboxContainer.getScene().getWindow()).close();
    }

    @FXML
    private void onCancelClick() {
        ((Stage) checkboxContainer.getScene().getWindow()).close();
    }
}