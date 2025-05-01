package nsu.fit.utils.warning;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Warning {
    private WarningType type;
    private String message;
}
