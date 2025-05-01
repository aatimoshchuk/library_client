package nsu.fit.data.access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LiteraryWork extends AbstractEntity {

    private String title;
    private String author;
    private Integer writingYear;
    private String category;

    public LiteraryWork(int id, String title, String author, int writingYear, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.writingYear = writingYear;
        this.category = category;
    }


    @Override
    public boolean checkEmptyFields() {
        return title != null && !title.isEmpty() && author != null && !author.isEmpty();
    }
}
