package generation.postfix;


import library.BinaryTreeNode;
import structure.BinaryTree;

import java.util.ArrayList;

public class PostfixGenerator {
    private final BinaryTree binaryTree;
    ArrayList<BinaryTreeNode> postfixNodesList;

    public PostfixGenerator(BinaryTree binaryTree) {
        this.binaryTree = binaryTree;
        postfixNodesList = new ArrayList<>();
    }

    public ArrayList<BinaryTreeNode> getPostfixView() {
        addNode(binaryTree.getRoot());
        return postfixNodesList;
    }

    private void addNode(BinaryTreeNode node) {
        if (node.hasLeft()) addNode(node.getLeft());
        if (node.hasRight()) addNode(node.getRight());
        postfixNodesList.add(node);
    }
}
