package lexis;

import library.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LexicalAnalyzer {

    private final String dataString;
    private final List<Token> tokens;
    private final List<Symbol> symbols;

    public LexicalAnalyzer(String dataString) throws IOException {
        tokens = new LinkedList<>();
        symbols = new LinkedList<>();
        this.dataString = dataString;
        analyse();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public boolean isInIdentifierList(String id) {
        return symbols.stream().map(Symbol::getName).toList().contains(id);
    }

    private void analyse() throws IOException {
        int posCounter = 0;
        List<String> dataList = splitData(dataString);
        dataList = dataList.stream().filter(item -> !item.equals(" ") && !item.equals("\n")).toList();
        for (String item : dataList) {
            if (tryParseOperator(item)) {
                tokens.add(new Token(parseOperator(item)));
            } else if (canBeConst(item)) {
                try {
                    constHandle(item);
                } catch (NumberFormatException e) {
                    throw new IOException("Лексическая ошибка! Неправильно задана константа "
                            + item + " на позиции " + (posCounter + 1));
                }
            } else {
                identifierHandle(item, posCounter);
            }
            posCounter += item.length();
        }
    }

    private boolean canBeConst(String string) {
        return string.matches("^[0-9.]+");
    }

    private void constHandle(String string) throws NumberFormatException {
        try {
            tokens.add(new Token(Lexeme.INT_CONST, Integer.parseInt(string)));
        } catch (NumberFormatException e) {
            tokens.add(new Token(Lexeme.FLOAT_CONST, Double.parseDouble(string)));
        }
    }

    private static boolean isLatinLetter(char ch) {
        return Character.toString(ch).matches("[a-zA-Z]");
    }


    private boolean isIdentifier( String string) { return string.matches("[a-zA-Z_][a-zA-Z0-9_]*+(\\[(f|F|i|I)])?$");}

    private void identifierHandle(String string, int posCounter) throws IOException {
        if (isIdentifier(string)) {
            Type currentType = string.matches(".*(\\[(f|F|)])$") ? Type.FLOAT : Type.INT;
            String symbolName = string.replaceAll("(\\[(f|F|i|I)])$","");
            Symbol symbol = new Symbol(symbolName, currentType, SymbolReuse.FORBIDDEN);
            if (!isInIdentifierList(symbolName)) {
                symbols.add(symbol);
            }
            tokens.add(new Token(Lexeme.ID, symbols.indexOf(symbol)));
        } else throw new IOException("Лексическая ошибка! Идентификатор " + string
                + " имеет неверный формат на позиции " + (posCounter + 1));
    }

    private Lexeme parseOperator(String operator) {
        return switch (operator) {
            case  "(" -> Lexeme.OPEN_BRACE;
            case ")" -> Lexeme.CLOSE_BRACE;
            case "*" -> Lexeme.MULTIPLY;
            case "/" -> Lexeme.DIVIDE;
            case "-" -> Lexeme.SUBTRACT;
            case "+" -> Lexeme.ADD;
            default -> null;
        };
    }

    private boolean tryParseOperator(String operator) {return parseOperator(operator) != null;}

    private List<String> splitData(String dataString) {
        return  List.of(dataString.split("(?=[()*/\\-+ \n])|(?<=[()*/\\-+ \n])"));
    }
}
