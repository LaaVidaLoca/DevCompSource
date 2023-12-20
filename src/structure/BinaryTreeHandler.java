package structure;

import library.BinaryTreeNode;

public abstract class BinaryTreeHandler {
    protected final BinaryTree binaryTree;

    public BinaryTreeHandler(BinaryTree binaryTree) {
        this.binaryTree = binaryTree;
    }

    public void handle() {
        handleDeep(binaryTree.getRoot());
    }

    private void handleDeep(BinaryTreeNode node) {
        if (node.hasLeft()) handleDeep(node.getLeft());
        if (node.hasRight()) handleDeep(node.getRight());
        handleNode(node);
    }

    protected abstract void handleNode(BinaryTreeNode node);
}
