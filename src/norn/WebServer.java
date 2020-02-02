/* Copyright (c) 2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import norn.web.ExceptionsFilter;
import norn.web.LogFilter;
import norn.web.StaticFileHandler;

public class WebServer {
    private final HttpServer server;
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 404;
    
    // AF(server) = a WebServer server that hosts a connection where a client can input a grammar
    //              defined by the project and will receive a mailing list corresponding to said
    //              grammar
    //
    // RI:
    //  - true
    //
    // SRE:
    //  - server is private and final, never returned
    //
    // TSA:
    //  - While this class isn't necessarily explicitly threadsafe, its calls handling the mailing list and
    //      context operate in a threadsafe manner, so overall the operations are threadsafe
    
    /**
     * Public constructor for WebServer
     * @param port the port that the server will be hosted on
     * @param context Object that contains previously defined items
     * @throws IOException when visualization fails
     */
    public WebServer(int port, Context context) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());
        
        List<Filter> logging = List.of(new ExceptionsFilter(), new LogFilter());

        StaticFileHandler.create(server, "/static/", "static/", "invalid");
        HttpContext eval = server.createContext("/eval/", exchange -> handleEval(exchange, context));
        eval.getFilters().addAll(logging);
    }
    
    /**
     * Evaluates and Visualizes a mailing list for a given grammar input
     * - For the evaluation, the code functions along the spec of evaluate defined in Context/ListExpression
     * - For the visualization, the code will output a tree representing the recursive input generated by the parser,
     *   along with a list of all defined listnames.
     * @param exchange the exchange containing the mailing list input
     * @param context stores the previously defined ListNames
     */
    private void handleEval(HttpExchange exchange, Context context) throws IOException {
        // page response is HTML text in UTF-8
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();

        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);

        final String data = path.substring(base.length());

        final String response;
        final ListExpression expr;
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        try {
            expr = ListExpression.parse(data);
            response = Util.setToOrderedString(context.evaluate(expr));
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
        } catch (EvaluationException e) {
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            out.print(e.getMessage());
            out.flush();
            exchange.close();
            return;
        }
        final VisualizerTree tree = new VisualizerTree("Expression structure");
        tree.addChild(expr.getVisualizerTree());
        StringBuilder sb = new StringBuilder(tree.asHTML());
        if (!expr.getAllListNames().isEmpty()) {
            final VisualizerTree definedNames = new VisualizerTree("Used listnames");
            for (ListName listName : expr.getAllListNames()) {
                try {
                    final Set<String> set = listName.evaluate(context);
                    final VisualizerTree nameTree = new VisualizerTree(listName.toString());
                    nameTree.addChild(new VisualizerTree(Util.setToOrderedString(set)));
                    definedNames.addChild(nameTree);
                } catch (EvaluationException e) {
                    throw new AssertionError("ListName should never throw exception");
                }
            }
            sb.append(definedNames.asHTML());
        }
        String templated = String.format(getTemplate(), data, response, sb.toString());
        out.print(templated);
        out.flush();
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }

    private static String getTemplate() {
        try {
            return Files.readString(Paths.get("static/template.html"));
        } catch (IOException e) {
            throw new AssertionError("template not in static folder/static folder doesn't exist");
        }
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
    }
}