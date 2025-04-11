package nsu.fit.data.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ReaderCategory {
    private int readerCategoryID;
    private String readerCategoryName;
    private boolean isPermitted;
}
