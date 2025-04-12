package nsu.fit.data.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Librarian extends AbstractEntity {

    private String surname;
    private String name;
    private String patronymic;
    private String birthDay;
    private String phoneNumber;
    private Integer library;
    private Integer roomNumber;

    public Librarian(int id, String surname, String name, String patronymic, String birthDay, String phoneNumber,
                     int library, int roomNumber) {
        this.id = id;
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.birthDay = birthDay;
        this.phoneNumber = phoneNumber;
        this.library = library;
        this.roomNumber = roomNumber;
    }

    @Override
    public boolean checkEmptyFields() {
        return name != null && !name.isEmpty() && surname != null && !surname.isEmpty() && patronymic != null &&
                !patronymic.isEmpty() && birthDay != null && !birthDay.isEmpty() && library > 0 && phoneNumber != null &&
                !phoneNumber.isEmpty() && roomNumber > 0;
    }
}
