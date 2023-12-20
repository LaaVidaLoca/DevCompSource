package generation.intermediate;


import semantic.SemanticAnalyzer;
import semantic.TypeParseOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import structure.*;
import library.*;


public class IntermediateCodeGenerator extends BinaryTreeHandler {
    private final List<Symbol> symbolList;

    private final List<CodeInstruction> instructionList;
    private int nameCounter = 0;

    public IntermediateCodeGenerator(BinaryTree binaryTree, List<Symbol> symbolList) {
        super(binaryTree);
        instructionList = new ArrayList<>();
        this.symbolList = symbolList;
    }

    public List<Symbol> getSymbols() {
        return symbolList;
    }


    public List<CodeInstruction> getInstructionList() {
        return instructionList;
    }

    private String getName() {
        nameCounter++;
        return "#T" + nameCounter;
    }

    private Symbol getSymbol(Type type) {
        Optional<Symbol> symbol = symbolList.stream().filter(sym ->
                sym.getSymbolReuse() == SymbolReuse.FREE && sym.getSymbolType() == type
        ).findFirst();
        return symbol.orElseGet(() -> {Symbol newSym = new Symbol(getName(), type, SymbolReuse.TAKEN);
            symbolList.add(newSym);
            return newSym;
        }).setSymbolReuse(SymbolReuse.TAKEN);

    }

    private void freeSymbol(Token token) {
        if (token.getLexeme() == Lexeme.ID) {
            Symbol symbol = symbolList.get((Integer) token.getAttribute());
            if (symbol.getSymbolReuse() == SymbolReuse.TAKEN) symbol.setSymbolReuse(SymbolReuse.FREE);
        }
    }

    private BinaryTreeNode swapToValue(BinaryTreeNode node, Type type) {
        Symbol symbol = getSymbol(type);
        Token token = new Token(Lexeme.ID, symbolList.indexOf(symbol));
        BinaryTreeNode newNode = new BinaryTreeNode(token);
        if (!node.isRoot()) node.getParent().replaceChild(node, newNode);
        return newNode;
    }

    public static Type getOperandType(BinaryTreeNode node, List<Symbol> symbolList) {
        Nodal left = node.getLeft().getValue();
        if (left.getClass() == TypeParseOperation.class) return Type.FLOAT;
        else return SemanticAnalyzer.isInt(node.getLeft(),symbolList) ? Type.INT : Type.FLOAT;
    }

    public static boolean isOperandToken(Nodal token) {
        return !((Token) token).getLexeme().getLexemeType().isOperator();
    }

    public static boolean isOperand(BinaryTreeNode node, Predicate<Nodal> isValueOperand) {
        Nodal value = node.getValue();
        if (value.getClass() == Token.class) {
            return isValueOperand.test(value);
        } else {
            return isOperand(node.getLeft(), isValueOperand);
        }
    }

    public static boolean isLeafParse(BinaryTreeNode node, Predicate<Nodal> isValueOperand) {
        return node.getValue().getClass() == TypeParseOperation.class && isOperand(node.getLeft(),isValueOperand);
    }

    public static boolean isLeafOperator(BinaryTreeNode node, Predicate<Nodal> isValueOperand) {
        return node.hasLeft() && node.hasRight() && isOperand(node.getLeft(),isValueOperand) && isOperand(node.getRight(),isValueOperand);
    }

    public static boolean isLeafParseImpl(BinaryTreeNode node) {
        return isLeafParse(node, IntermediateCodeGenerator::isOperandToken);
    }

    public static boolean isLeafOperatorImpl(BinaryTreeNode node)  {
        return isLeafOperator(node, IntermediateCodeGenerator::isOperandToken);
    }

    @Override
    protected void handleNode(BinaryTreeNode node) {
        if (isLeafParseImpl(node) || isLeafOperatorImpl(node)) {
            Token left = (Token) node.getLeft().getValue();
            CodeInstruction instruction = new CodeInstruction(Operation.get(node),left);
            if (isLeafParseImpl(node)) {
                instruction.setResult((Token) swapToValue(node,Type.FLOAT).getValue());
            } else if (isLeafOperatorImpl(node)) {
                Token right = (Token) node.getRight().getValue();
                instruction.setSecondArg(right);
                instruction.setResult((Token) swapToValue(node,getOperandType(node,symbolList)).getValue());
                freeSymbol(right);
            }
            freeSymbol(left);
            instructionList.add(instruction);
        }
    }
}
