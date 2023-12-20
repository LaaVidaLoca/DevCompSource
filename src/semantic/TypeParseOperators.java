package semantic;

public enum TypeParseOperators {
    INT_FLOAT("i2f");
    final String value;

    TypeParseOperators(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
