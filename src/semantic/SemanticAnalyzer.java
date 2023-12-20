package semantic;


import library.BinaryTreeNode;
import library.*;
import structure.BinaryTree;
import structure.BinaryTreeHandler;

import java.util.List;

public class SemanticAnalyzer extends BinaryTreeHandler {

    private final List<Symbol> symbolList;


    public SemanticAnalyzer(List<Symbol> symbolList, BinaryTree binaryTree) {
        super(binaryTree);
        this.symbolList = symbolList;
    }

    public static boolean isInt(BinaryTreeNode node, List<Symbol> symbolList) {
        Token token = (Token) node.getValue();
        return (token.getLexeme() == Lexeme.INT_CONST)
                || (token.getLexeme() == Lexeme.ID && symbolList.get(
                        (Integer) token.getAttribute()).getSymbolType() == Type.INT);
    }

    private void wrapFloat(BinaryTreeNode node) {
        BinaryTreeNode newNode = new BinaryTreeNode(new TypeParseOperation(TypeParseOperators.INT_FLOAT));
        BinaryTreeNode parent = node.getParent();
        newNode.insertLeft(node);
        parent.replaceChild(node,newNode);
    }

    private boolean isIntDeep(BinaryTreeNode node) {
        if (node.getValue().getClass() == TypeParseOperation.class) return false;
        if (((Token)node.getValue()).getLexeme().getLexemeType().isOperator()) {
            return isIntDeep(node.getLeft()) && isIntDeep(node.getRight());
        } else {
            return isInt(node, symbolList);
        }
    }

    public static void checkZero(BinaryTreeNode node) {
        if (node.getValue().getClass() == Token.class
                && ((Token) node.getValue()).getLexeme() == Lexeme.DIVIDE) {
            Token rightToken = (Token) (node.getRight().getValue().getClass() == Token.class
                    ? node.getRight().getValue()
                    : node.getRight().getLeft().getValue());
            if (rightToken.getLexeme().getLexemeType() == LexemeType.CONST && rightToken.getAttribute().intValue() == 0) throw new IllegalArgumentException("Найдено деление на 0");
        }
    }

    @Override
    protected void handleNode(BinaryTreeNode node) {
        checkZero(node);
        if (!node.isRoot() && isIntDeep(node) && !isIntDeep(node.getSibling())) {
            wrapFloat(node);
        }
    }
}
