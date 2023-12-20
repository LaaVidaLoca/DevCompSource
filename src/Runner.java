
import generation.intermediate.IntermediateCodeGenerator;
import generation.postfix.PostfixGenerator;
import lexis.LexicalAnalyzer;
import library.PostCodeDTO;
import optimization.TreeOptimizer;
import optimization.SymbolOptimizer;
import semantic.SemanticAnalyzer;
import structure.BinaryTree;
import syntax.SyntaxAnalyzer;
import utils.FileHandler;
import java.io.IOException;
import java.util.List;

public class Runner {

    public static void main(String[] args) throws IOException {
        boolean argsFlag = true;
        for (String arg : args) {
            argsFlag &= !(arg.contains("*") || arg.contains("?"));
        }
        if (args.length > 10) {
            System.out.println("Wrong number of arguments");
        } else if (!argsFlag) {
            System.out.println("Illegal symbols in files' names");
        } else {
            try {
                FileHandler fileHandler = new FileHandler(System.getProperty("user.dir") + "/src");
                LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(fileHandler.readData(args[0]));
                SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexicalAnalyzer.getTokens());
                BinaryTree tree = syntaxAnalyzer.analyze();
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexicalAnalyzer.getSymbols(), tree);
                SymbolOptimizer symbolOptimizer = new SymbolOptimizer(tree, lexicalAnalyzer.getSymbols());
                TreeOptimizer treeOptimizer = new TreeOptimizer(tree);
                String mod = args[8];
                if (mod.equalsIgnoreCase("lex")) {
                    fileHandler.writeData(lexicalAnalyzer.getTokens(), args[1], lexicalAnalyzer.getSymbols());
                    fileHandler.writeData(lexicalAnalyzer.getSymbols(), args[2]);
                }
                if (mod.equalsIgnoreCase("syn")) {
                    fileHandler.writeData(tree.toString(), args[3]);
                }
                if (List.of("sem", "gen1", "gen2").contains(mod.toLowerCase())) {
                    semanticAnalyzer.handle();
                }
                if (mod.equalsIgnoreCase("sem")) {
                    fileHandler.writeData(tree.toString(), args[4]);
                }
                if (args.length == 10 && args[9].equalsIgnoreCase("opt")) {
                    treeOptimizer.handle();
                }
                if (mod.equalsIgnoreCase("gen2")) {
                    PostfixGenerator postfixGenerator = new PostfixGenerator(tree);
                    fileHandler.writeData(args[6], postfixGenerator.getPostfixView());
                }
                if (List.of("gen1", "gen3").contains(mod.toLowerCase())) {
                    symbolOptimizer.handle();
                    IntermediateCodeGenerator intermediateCodeGenerator = new IntermediateCodeGenerator(tree, symbolOptimizer.getSymbolList());
                    intermediateCodeGenerator.handle();
                    if (mod.equalsIgnoreCase("gen1")) {
                        fileHandler.writeData(intermediateCodeGenerator.getSymbols(), args[2]);
                        fileHandler.writeData(args[5], intermediateCodeGenerator.getInstructionList());
                    }
                    if (mod.equalsIgnoreCase("gen3")) {
                        fileHandler.writeDataSerial(new PostCodeDTO(
                                intermediateCodeGenerator.getSymbols(),
                                intermediateCodeGenerator.getInstructionList()), args[7]);
                        ;
                    }
                }
            } catch (IOException | RuntimeException e) {
                throw new IllegalStateException(e);
                //System.out.println(e.getMessage());
            }
        }
    }
}