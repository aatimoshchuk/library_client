package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Publication extends AbstractEntity {
    private String title;
    private String publisher;
    private String receiptDate;
    private int yearOfPrinting;
    private String category;
    private int ageRestriction;
    private int storageLocationID;
    private String state;
    private boolean permissionToIssue;
    private int daysForReturn;

    public Publication(int id, String title, String publisher, String receiptDate, int yearOfPrinting, String category,
                       int ageRestriction, int storageLocationID, String state, boolean permissionToIssue,
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
        this.permissionToIssue = permissionToIssue;
        this.daysForReturn = daysForReturn;
    }
    @Override
    public boolean checkEmptyFields() {
        return title != null && !title.isEmpty() && publisher != null && !publisher.isEmpty() && receiptDate != null &&
                !receiptDate.isEmpty() && storageLocationID > 0 && state != null && !state.isEmpty() && daysForReturn > 0;
    }
}
