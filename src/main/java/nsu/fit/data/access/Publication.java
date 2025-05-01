package nsu.fit.data.access;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;
import nsu.fit.annotations.HiddenField;

@Setter
@Getter
public class Publication extends AbstractEntity {

    @DisplayName("Номенклатурный номер")
    @FieldOrder(1)
    private int id;

    @DisplayName("Название")
    @FieldOrder(2)
    private String title;

    @DisplayName("Издательство")
    @FieldOrder(3)
    private String publisher;

    @DisplayName("Дата поступления")
    @FieldOrder(4)
    private String receiptDate;

    @DisplayName("Год печати")
    @FieldOrder(5)
    private Integer yearOfPrinting;

    @DisplayName("Категория")
    @FieldOrder(6)
    private String category;

    @DisplayName("Возрастное ограничение")
    @FieldOrder(7)
    private Integer ageRestriction;

    @DisplayName("ID места хранения")
    @HiddenField
    private Integer storageLocationID;

    @DisplayName("Состояние")
    @FieldOrder(8)
    private PublicationState state;

    @DisplayName("Разрешение на выдачу")
    @HiddenField
    private BooleanProperty permissionToIssue = new SimpleBooleanProperty();

    @DisplayName("Срок возврата")
    @HiddenField
    private Integer daysForReturn;

    public Publication(int id, String title, String publisher, String receiptDate, int yearOfPrinting, String category,
                       int ageRestriction, int storageLocationID, PublicationState state, boolean permissionToIssue,
                       int daysForReturn) {
        this.id = id;
        this.title = title;
        this.publisher = publisher;
        this.receiptDate = receiptDate;
        this.yearOfPrinting = yearOfPrinting;
        this.category = category;
        this.ageRestriction = ageRestriction;
        this.storageLocationID = storageLocationID;
        this.state = state;
        this.permissionToIssue.set(permissionToIssue);
        this.daysForReturn = daysForReturn;
    }

    public Publication() {
        this.state = PublicationState.AVAILABLE;
    }

    @Override
    public boolean checkEmptyFields() {
        return title != null && !title.isEmpty() && publisher != null && !publisher.isEmpty() && receiptDate != null &&
                !receiptDate.isEmpty() && state != null && daysForReturn > 0;
    }

    @Override
    public boolean validateNumericFields() {
        return storageLocationID != null && daysForReturn != null;
    }
 }
