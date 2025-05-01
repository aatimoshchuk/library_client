package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;

@Setter
@Getter
@NoArgsConstructor
public class Reader extends AbstractEntity {

    @DisplayName("Номер читательского билета")
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

    @DisplayName("Категория")
    @FieldOrder(6)
    private String category;

    public Reader(int id, String surname, String name, String patronymic, String birthDay, String category) {
        this.id = id;
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.birthDay = birthDay;
        this.category = category;
    }

    @Override
    public boolean checkEmptyFields() {
        return name != null && !name.isEmpty() && surname != null && !surname.isEmpty() && patronymic != null &&
                !patronymic.isEmpty() && birthDay != null && !birthDay.isEmpty();
    }
}
