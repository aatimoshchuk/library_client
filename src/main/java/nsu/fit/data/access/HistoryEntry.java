package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class HistoryEntry extends AbstractEntity {
    private Integer publicationNomenclatureNumber;
    private Integer libraryCardNumber;
    private String issueDate;
    private String returnDate;
    private Integer librarianID;

    public HistoryEntry(int id, Integer publicationNomenclatureNumber, Integer libraryCardNumber, String issueDate,
                        String returnDate, Integer librarianID) {
        this.id = id;
        this.publicationNomenclatureNumber = publicationNomenclatureNumber;
        this.libraryCardNumber = libraryCardNumber;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
        this.librarianID = librarianID;
    }

    @Override
    public boolean checkEmptyFields() {
        return publicationNomenclatureNumber > 0 && libraryCardNumber > 0 && issueDate != null && !issueDate.isEmpty();
    }
}
