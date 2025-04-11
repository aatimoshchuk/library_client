package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StorageLocation extends AbstractEntity {
    private int libraryID;
    private int roomNumber;
    private int shelvingNumber;
    private int shelfNumber;

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
}
