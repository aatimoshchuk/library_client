package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;

@Setter
@Getter
@NoArgsConstructor
public class StorageLocation extends AbstractEntity {

    @DisplayName("ID места хранения")
    @FieldOrder(1)
    private int id;

    @DisplayName("ID библиотеки")
    @FieldOrder(2)
    private Integer libraryID;

    @DisplayName("Номер зала")
    @FieldOrder(3)
    private Integer roomNumber;

    @DisplayName("Номер стеллажа")
    @FieldOrder(4)
    private Integer shelvingNumber;

    @DisplayName("Номер полки")
    @FieldOrder(5)
    private Integer shelfNumber;

    public StorageLocation(int id, int libraryID, int roomNumber, int shelvingNumber, int shelfNumber) {
        this.id = id;
        this.libraryID = libraryID;
        this.roomNumber = roomNumber;
        this.shelvingNumber = shelvingNumber;
        this.shelfNumber = shelfNumber;
    }

    @Override
    public boolean checkEmptyFields() {
        return libraryID > 0 && roomNumber > 0 && shelvingNumber > 0 && shelfNumber > 0;
    }

    @Override
    public boolean validateNumericFields() {
        return libraryID != null && roomNumber != null && shelvingNumber != null && shelfNumber != null;
    }
}
