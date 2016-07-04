package edu.mit.compilers;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;
import edu.mit.compilers.astnode.ProgramASTNode;
import edu.mit.compilers.grammar.*;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.tools.CLI.Action;
import edu.mit.compilers.utils.StateAccumulator;

class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            CLI.parse(args, new String[]{"all", "cse", "register"});
            InputStream inputStream = args.length == 0 ? System.in : new java.io.FileInputStream(CLI.infile);
            PrintStream outputStream = CLI.outfile == null ? System.out
                    : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));

            DecafScanner scanner = new DecafScanner(new DataInputStream(inputStream));
//            LOGGER.log(Level.INFO, "DONE: Scanner");

            if (CLI.target == Action.SCAN) {
                scanner.setTrace(CLI.debug);
                boolean done = false;
                while (!done) {
                    try {
                        for (Token token = scanner.nextToken(); token
                                .getType() != DecafParserTokenTypes.EOF; token = scanner.nextToken()) {
                            String type = "";
                            String text = token.getText();
                            switch (token.getType()) {
                            case DecafScannerTokenTypes.ID:
                                type = " IDENTIFIER";
                                break;
                            case DecafScannerTokenTypes.INT_LITERAL:
                                type = " INTLITERAL";
                                break;
                            case DecafScannerTokenTypes.STR_LITERAL:
                                type = " STRINGLITERAL";
                                break;
                            case DecafScannerTokenTypes.CHAR_LITERAL:
                                type = " CHARLITERAL";
                                break;
                            case DecafScannerTokenTypes.TK_true:
                                type = " BOOLEANLITERAL";
                                break;
                            case DecafScannerTokenTypes.TK_false:
                                type = " BOOLEANLITERAL";
                                break;
                            }
                            outputStream.println(token.getLine() + type + " " + text);
                        }
                        done = true;
                    } catch (Exception e) {
                        // print the error:
                        System.err.println(CLI.infile + " " + e);
                        scanner.consume();
                    }
                }
                return;
            }
            
            DecafParser parser = new DecafParser(scanner);
            ASTFactory factory = new ASTFactory();
            factory.setASTNodeClass(CommonASTWithLines.class);
            parser.setASTFactory(factory);
            
            parser.program();

            if (parser.getError()) {
                System.exit(1);
            }

            AST ast = parser.getAST();
            
            OutputStreamWriter astFile = new OutputStreamWriter(new FileOutputStream("ast.xml"));
            ((CommonASTWithLines) ast).xmlSerialize(astFile);
            astFile.close();

//            LOGGER.log(Level.INFO, "DONE: Parser (no errors). AST in ast.xml");

            if (CLI.target == Action.PARSE) {
                return;
            }

            StateAccumulator accumulator = new StateAccumulator();
            
            OutputStreamWriter astGenFile = new OutputStreamWriter(new FileOutputStream("ast_gen.xml"));
            astGenFile.write(new ProgramASTNode(ast).toString());
            astGenFile.close();

            SemanticCheckVisitor semanticCheckVisitor = new SemanticCheckVisitor(outputStream, accumulator);
            ProgramASTNode astNode = new ProgramASTNode(ast);
            if (!semanticCheckVisitor.visit(astNode)) {
                System.exit(1);
            }
            
//            LOGGER.log(Level.INFO, "DONE: Inter (no errors)");

            if (CLI.target == Action.INTER) {
                return;
            }
            accumulator.globalSymbolTable = semanticCheckVisitor.getGlobalsSymbolTable();

            if (CLI.opts[0] || CLI.opts[1]) {
                LOGGER.log(Level.INFO, "CSE optimization");
                DataflowVisitor dataflowVisitor = new DataflowVisitor();
                dataflowVisitor.visit(astNode);
                accumulator.dataflowVisitor = dataflowVisitor;
                CSEOptimization.analyze(accumulator);
            }

            if (CLI.opts[0] || CLI.opts[2]) {
                LOGGER.log(Level.INFO, "Register allocation");
                DataflowVisitor dataflowVisitor = new DataflowVisitor();
                dataflowVisitor.visit(astNode);
                accumulator.dataflowVisitor = dataflowVisitor;
                VariableLiveness.analyze(accumulator);
                RegisterAllocation.allocate(accumulator);
            }

            // generate optimized astnode tree xml
            astGenFile = new OutputStreamWriter(new FileOutputStream("ast_gen_opt.xml"));
            astGenFile.write(astNode.toString());
            astGenFile.close();

            LocalDescriptor.resetVariableCounter();

            AssemblerVisitor assemblerVisitor = new AssemblerVisitor(outputStream, accumulator);
            assemblerVisitor.visit(astNode);

            if (CLI.outfile != null) {
//                LOGGER.log(Level.INFO, "DONE: Assembler (no errors)");
                PrintWriter writer = new PrintWriter(CLI.outfile);
                writer.print(assemblerVisitor);
                writer.close();
            } else {
//                LOGGER.log(Level.INFO, "DONE: Assembler (no errors)\n" + assemblerVisitor);
                System.out.print(assemblerVisitor);
            }
            
            if (CLI.target == Action.ASSEMBLY) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
