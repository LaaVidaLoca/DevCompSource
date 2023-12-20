package optimization;

import library.*;
import structure.BinaryTree;
import structure.BinaryTreeHandler;

import java.util.ArrayList;
import java.util.List;

public class SymbolOptimizer extends BinaryTreeHandler {

    private final List<Symbol> symbolList;

    private final List<Symbol> origSymbols;


    public List<Symbol> getSymbolList() {
        return symbolList;
    }

    public SymbolOptimizer(BinaryTree binaryTree, List<Symbol> origSymbols) {
        super(binaryTree);
        this.origSymbols = origSymbols;
        symbolList = new ArrayList<>();
    }

    private Symbol executeSymbol(Token token) {
        return origSymbols.get(token.getAttribute().intValue());
    }

    @Override
    protected void handleNode(BinaryTreeNode node) {
        Nodal value = node.getValue();
        if (value.getClass() == Token.class) {
            Token token = (Token) value;
            if (token.getLexeme() == Lexeme.ID) {
                Symbol symbol = executeSymbol(token);
                if (!symbolList.contains(symbol)) symbolList.add(executeSymbol(token));
            }
        }
    }
}
