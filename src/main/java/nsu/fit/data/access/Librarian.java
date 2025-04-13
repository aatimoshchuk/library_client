package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;

@Setter
@Getter
@NoArgsConstructor
public class Librarian extends AbstractEntity {
    @DisplayName("ID")
    @FieldOrder(1)
    private int id;
    @DisplayName("Фамилия")
    @FieldOrder(2)
    private String surname;
    @DisplayName("Имя")
    @FieldOrder(3)
    private String name;
    @DisplayName("Отчество")
    @FieldOrder(4)
    private String patronymic;
    @DisplayName("Дата рождения")
    @FieldOrder(5)
    private String birthDay;
    @DisplayName("Номер телефона")
    @FieldOrder(6)
    private String phoneNumber;
    @DisplayName("ID библиотеки")
    @FieldOrder(7)
    private Integer library;
    @DisplayName("Номер зала")
    @FieldOrder(8)
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
