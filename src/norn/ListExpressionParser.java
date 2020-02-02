package norn;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.mit.eecs.parserlib.ParseTree;
import edu.mit.eecs.parserlib.Parser;
import edu.mit.eecs.parserlib.UnableToParseException;

public class ListExpressionParser {
    /**
     * Main method. Parses and then reprints an example expression.
     * 
     * @param args command line arguments, not used
     * @throws UnableToParseException if example expression can't be parsed
     * @throws EvaluationException 
     */
    public static void main(final String[] args) throws UnableToParseException, EvaluationException {
        
        final String input = "c=a;c=d;(a=a@mit | b=c,b@mit);a,b";
        System.out.println(input);
//        ParseTree<ExpressionGrammar> tree = PARSER.parse(input);
//        System.out.println(tree.toString());
        final ListExpression expression = ListExpressionParser.parse(input);
        System.out.println(expression.toString());
        Context context = new Context();
        System.out.println(expression.evaluate(context).toString());
        System.out.println(context.toString());
        }
    
    
    // the nonterminals of the grammar
    private enum ExpressionGrammar {
        PARALLEL, SEQUENCE, UNION, DEFINITION, DIFFERENCE, INTERSECTION, BASE, LISTNAME, RECIPIENT, 
            WHITESPACE, EMPTY,
    }
    
    private static final Parser<ExpressionGrammar> PARSER = makeParser();

    /**
     * Compile the grammar into a parser.
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<ExpressionGrammar> makeParser() {
        try {
            // read the grammar as a file, relative to the project root.
            final File grammarFile = new File("src/norn/ListExpression.g");
            return Parser.compile(grammarFile, ExpressionGrammar.PARALLEL);

            // Parser.compile() throws two checked exceptions.
            // Translate these checked exceptions into unchecked RuntimeExceptions,
            // because these failures indicate internal bugs rather than client errors
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }

    /**
     * Parse a string into a list expression.
     *
     * @param string string to parse
     * @return ListExpression parsed from the string
     * @throws UnableToParseException if the string doesn't match the MailingList grammar
     */
    public static ListExpression parse(final String string) throws UnableToParseException {
        // parse the example into a parse tree
        final ParseTree<ExpressionGrammar> parseTree = PARSER.parse(string);

        // display the parse tree in various ways, for debugging only
        // System.out.println("parse tree " + parseTree);
        // Visualizer.showInBrowser(parseTree);

        // make an AST from the parse tree
        final ListExpression expression = makeAbstractSyntaxTree(parseTree);
        // System.out.println("AST " + expression);

        return expression;
    }

    /**
     * Convert a parse tree into an abstract syntax tree.
     *
     * @param parseTree constructed according to the grammar in Exression.g
     * @return abstract syntax tree corresponding to parseTree
     * @throws ParallelDefinitionException if error in parallel definition
     */
    private static ListExpression makeAbstractSyntaxTree(final ParseTree<ExpressionGrammar> parseTree) {
        switch (parseTree.name()) {
        case PARALLEL:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new Parallel(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case SEQUENCE:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new Sequence(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case UNION:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); i++) {
                    expression = new Union(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case DEFINITION:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                if (children.get(0).name() == ExpressionGrammar.DIFFERENCE) {
                    return makeAbstractSyntaxTree(children.get(0));
                } else {
                    return new Definition(new ListName(children.get(0).text()), makeAbstractSyntaxTree(children.get(1)));
                }
            }
        case DIFFERENCE:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); ++i) {
                    expression = new Difference(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case INTERSECTION:
            {
                final List<ParseTree<ExpressionGrammar>> children = parseTree.children();
                ListExpression expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); ++i) {
                    expression = new Intersection(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        case BASE:
            {
                final ParseTree<ExpressionGrammar> child = parseTree.children().get(0);
                return makeAbstractSyntaxTree(child);
            }
        case RECIPIENT:
            {
                return new Recipient(parseTree.text().toLowerCase());
            }
        case LISTNAME:
            {
                return new ListName(parseTree.text().toLowerCase());
            }
        case EMPTY:
            {
                return new Empty();
            }
        default:
            throw new AssertionError("should never get here");
        }
    }
}
