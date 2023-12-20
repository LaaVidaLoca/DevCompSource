package semantic;
import library.*;

public class TypeParseOperation implements Nodal {
    private final TypeParseOperators value;

    public TypeParseOperation(TypeParseOperators value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.getValue();
    }
}
