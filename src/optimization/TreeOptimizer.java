package optimization;


import generation.intermediate.IntermediateCodeGenerator;
import library.*;
import semantic.SemanticAnalyzer;
import semantic.TypeParseOperation;
import structure.*;
import utils.*;

public class TreeOptimizer extends BinaryTreeHandler {

    public TreeOptimizer(BinaryTree binaryTree) {
        super(binaryTree);
    }

    private boolean needRepeat = false;

    @Override
    protected void handleNode(BinaryTreeNode node) {
        if (!node.isRoot()) {
            SemanticAnalyzer.checkZero(node);
            if (isLeafParseImpl(node)) {
                needRepeat = true;
                wrapValueTypeParse(node);
            }
            wrapUseless(node);
            if (isLeafOperatorImpl(node)) {
                needRepeat = true;
                wrapValueOperator(node);
            }
        }
        if (node.isRoot() && needRepeat) {
            needRepeat = false;
            handle();
        }
    }

    private static boolean isOperandConst(Nodal token) {
        return ((Token) token).getLexeme().getLexemeType() == LexemeType.CONST;
    }

    private boolean isLeafParseImpl(BinaryTreeNode node) {
        return IntermediateCodeGenerator.isLeafParse(node, TreeOptimizer::isOperandConst);
    }

    public static boolean isLeafOperatorImpl(BinaryTreeNode node) {
        return IntermediateCodeGenerator.isLeafOperator(node, TreeOptimizer::isOperandConst);
    }

    public static boolean isOperand(BinaryTreeNode node) {
        Nodal value = node.getValue();
        return value.getClass() != TypeParseOperation.class && IntermediateCodeGenerator.isOperandToken(value);
    }

    private void wrapUseless(BinaryTreeNode node) {
        if (node.hasParent()) {
            if (isOperand(node) && node.getParent().getValue().getClass() != TypeParseOperation.class) {
                Nodal nodal = node.getValue();
                Token token = (Token) nodal;
                Number value = token.getAttribute();
                LexemeType operation = ((Token) node.getParent().getValue()).getLexeme().getLexemeType();
                if (isOperandConst(nodal)) {
                    if (operation == LexemeType.HIGH_PRIORITY_OPERATOR) {
                        if (value.intValue() == 1) {
                            node.smartDisable();
                            needRepeat = true;
                        }
                        if (value.intValue() == 0) {
                            node.getSibling().smartDisable();
                            needRepeat = true;
                        }
                    } else {
                        if (value.intValue() == 0) {
                            node.smartDisable();
                            needRepeat = true;
                        }
                    }
                }
            }
        }
    }

    private void wrapValueTypeParse(BinaryTreeNode node) {
        BinaryTreeNode left = node.getLeft();
        Token leftToken = (Token) left.getValue();
        node.setValue(new Token(Lexeme.FLOAT_CONST, leftToken.getAttribute().doubleValue()));
        left.disable();
    }


    private void wrapValueOperator(BinaryTreeNode node) {
        Lexeme operation = ((Token) node.getValue()).getLexeme();
        BinaryTreeNode left = node.getLeft();
        BinaryTreeNode right = node.getRight();
        Number leftValue = ((Token) left.getValue()).getAttribute();
        Number rightValue = ((Token) right.getValue()).getAttribute();
        Lexeme lexemeType = leftValue instanceof Integer ? Lexeme.INT_CONST : Lexeme.FLOAT_CONST;
        Number value = switch (operation) {
            case MULTIPLY -> NumberMathUtils.multiply(leftValue, rightValue);
            case SUBTRACT -> NumberMathUtils.subtract(leftValue, rightValue);
            case ADD -> NumberMathUtils.add(leftValue, rightValue);
            case DIVIDE -> NumberMathUtils.divide(leftValue, rightValue);
            default -> 0;
        };
        node.setValue(new Token(lexemeType, value));
        left.disable();
        right.disable();
    }


}
