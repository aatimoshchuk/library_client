package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Reader extends AbstractEntity {

    private String surname;
    private String name;
    private String patronymic;
    private String birthDay;
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
