package syntax;


import library.*;
import structure.BinaryTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SyntaxAnalyzer {

    private final String ERROR_MESSAGE = "Синтаксическая ошибка! ";
    private final List<BinaryTreeNode> nodes;
    private final List<Token> tokens;

    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        nodes = new ArrayList<>(tokens.stream().map(BinaryTreeNode::new).toList());
    }

    private void checkErrors() throws IOException {
        checkBraces();
        checkOperators();
    }

    private void checkBraces() throws IOException {
        int bracesCount = 0;
        List<Lexeme> lexemes = tokens.stream().map(Token::getLexeme).toList();
        for (int i = 0; i < tokens.size(); i++) {
            if (lexemes.get(i) == Lexeme.OPEN_BRACE) {
                bracesCount++;
            } else if (lexemes.get(i) == Lexeme.CLOSE_BRACE) {
                bracesCount--;
            }
            if (bracesCount<0) {
                throw new IOException(ERROR_MESSAGE + "<)> без <(> на позиции " + (i + 1));
            }
        }
        if (bracesCount > 0) {
            throw new IOException(ERROR_MESSAGE + "<(> не закрыта на позиции " + lexemes.lastIndexOf(Lexeme.OPEN_BRACE));
        }
    }

    private void checkOperators() throws IOException {
        int valuesCount = 0;
        List<Lexeme> lexemes = tokens.stream().map(Token::getLexeme).toList();
        for (int i = 0; i < tokens.size(); i++) {
            if (lexemes.get(i).getLexemeType().isOperator()) {
                valuesCount--;
            } else if (lexemes.get(i).getLexemeType()  != LexemeType.BRACE) {
                valuesCount++;
            }
            if (valuesCount>1) {
                throw new IOException(ERROR_MESSAGE + tokens.get(i) + " без оператора на позиции " + i);
            }
            if (valuesCount<0) {
                throw new IOException(ERROR_MESSAGE + tokens.get(i) + " без операнда на позиции " + i);
            }
        }
    }

    public BinaryTree analyze() throws IOException {
        checkErrors();
        analyzeBrace();
        unionHighPriorityNodes(nodes);
        unionLowPriorityNodes(nodes);
        return new BinaryTree(nodes.get(0));
    }

    public void analyzeBrace() {
      List<Lexeme> braceFinder = nodes.stream().map(item -> ((Token)item.getValue()).getLexeme()).toList();
      int closeBrace = braceFinder.indexOf(Lexeme.CLOSE_BRACE);
      if (closeBrace != -1) {
          int openBrace = braceFinder.subList(0,closeBrace).lastIndexOf(Lexeme.OPEN_BRACE);
          List<BinaryTreeNode> noBraceNode = new ArrayList<>(nodes.subList(openBrace + 1, closeBrace));
          unionHighPriorityNodes(noBraceNode);
          unionLowPriorityNodes(noBraceNode);
          for (int i = closeBrace; i >= openBrace; i--) {
              if (!nodes.get(i).isRoot()) {
                  nodes.remove(i);
              }
          }
          analyzeBrace();
      }
    }

    private void  unionHighPriorityNodes(List<BinaryTreeNode>  noBraceNode) {
        unionNodes(noBraceNode, LexemeType.HIGH_PRIORITY_OPERATOR);
    }

    private void  unionLowPriorityNodes(List<BinaryTreeNode>  noBraceNode) {
        unionNodes(noBraceNode, LexemeType.LOW_PRIORITY_OPERATOR);
    }

    private void unionNodes(List<BinaryTreeNode>  noBraceNode, LexemeType priority) {
        int rootIndex = IntStream.range(0, noBraceNode.size())
                .filter(i -> !noBraceNode.get(i).isRoot())
                .filter(i -> ((Token)noBraceNode.get(i).getValue()).getLexeme()
                        .getLexemeType() == priority)
                .findFirst().orElse(-1);
        if (noBraceNode.size() == 1) noBraceNode.get(0).root();
        if (rootIndex > 0) {
            BinaryTreeNode rootNode = noBraceNode.get(rootIndex);
            rootNode.root();
            rootNode.insertLeft(noBraceNode.get(rootIndex - 1));
            rootNode.insertRight(noBraceNode.get(rootIndex + 1));
            noBraceNode.get(rootIndex + 1).noRoot();
            noBraceNode.remove(rootIndex + 1);
            noBraceNode.get(rootIndex - 1).noRoot();
            noBraceNode.remove(rootIndex - 1);
            unionNodes(noBraceNode, priority);
        }
    }
}