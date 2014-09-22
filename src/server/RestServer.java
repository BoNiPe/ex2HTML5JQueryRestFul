package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Player;

/**
 * @author plaul1
 */
public class RestServer {

    static int port = 8089;
    static String ip = "127.0.0.1";
    static String publicFolder = "src/htmlFiles/";

    public void run() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        //REST Routes
        server.createContext("/Date", new HandlerDate());
        //HTTP Server Routes
        server.createContext("/pages", new HandlerFileServer());
        //ex1
        server.createContext("/AllPlayerNames", new HandlerQuestion1());
        //ex2
        server.createContext("/BitchPlayer", new HandlerQuestion2());
        //ex3
        server.createContext("/FindPlayer", new HandlerQuestion3());
        server.start();
        System.out.println("Server started, listening on port: " + port);
    }

    public static void main(String[] args) throws Exception {
        if (args.length >= 3) {
            port = Integer.parseInt(args[0]);
            ip = args[1];
            publicFolder = args[2];
            System.out.println("MainRestServer args[2] :" + args[2]);
        }
        new RestServer().run();
    }

    class HandlerDate implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = new Date().toString();
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            //he.getResponseHeaders().add("Content-Type", "appliaction/json");
            he.getResponseHeaders().add("Content-Type", "text/plain");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }
        }
    }

    class HandlerQuestion1 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = new Gson().toJson(getPlayers());
            //String response; //sadsada
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            //he.getResponseHeaders().add("Content-Type", "application/json");
            he.getResponseHeaders().add("Content-Type", "text/plain");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response); //What happens if we use a println instead of print --> Explain
            }
        }
    }

    class HandlerQuestion2 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

//OutputStreams are meant for binary data. Writers (including PrintWriter) are meant for text data.
//which means- OutputStream prints bytes, whereas Writer prints actualy jSon shit. (gSon ..wtf was that?)
            String requestedFile = he.getRequestURI().toString();
            System.out.println("getRequestURI to String: " + requestedFile);
            String f = requestedFile.substring(requestedFile.lastIndexOf("/") + 1);
            System.out.println("substring ^ lastIndexOf / +1 :" + f);

// ***Response type stays text/html here.
//            String extension = f.substring(f.lastIndexOf("."));
//            System.out.println("substring ^ last Index Of . :" + extension);
//            String mime = ""; if (extension.equals(".html")) { mime = "text/html"; } else { mime = "krisko"; }
// ***
            Integer fint = null;
            String response = "";
            int mySize = getPlayers().size();

//Error handling
            if (isInteger(f) && Integer.parseInt(f) <= mySize && Integer.parseInt(f) >= 0) {

                fint = Integer.parseInt(f);
                response = new Gson().toJson(getPlayers().get(fint));
                //String response; //Checking response via sout (shows jSon string)
                he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                //he.getResponseHeaders().add("Content-Type", "appliaction/json"); //LOL?
                he.getResponseHeaders().add("Content-Type", "text/html");
                he.sendResponseHeaders(200, response.length());
                try (PrintWriter pw = new PrintWriter(he.getResponseBody())) { //PrintWriter
                    pw.print(response);
                }

            } else {
                String error = "<h1>404 Not Found <3,</h1>No context found for request";
                byte[] bytesToSend = error.getBytes();
                response = error;
                int responseCode = 404;
                he.sendResponseHeaders(responseCode, bytesToSend.length);
                try (OutputStream os = he.getResponseBody()) {
                    os.write(bytesToSend, 0, bytesToSend.length);
                }
            }

        }
    }

    //used in ex2.

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    //used in ex1. ex2.

    private List<Player> getPlayers() {
        List<Player> p = new ArrayList<>();
        p.add(new Player(1, "James Rodriguez", "country"));
        p.add(new Player(2, "Thomas Mueller", "German"));
        p.add(new Player(3, "Messi", "GROW UP! ;//"));
        return p;
    }

    class HandlerQuestion3 implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

        }

    }

    class HandlerFileServer implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String requestedFile = he.getRequestURI().toString();
            System.out.println("getRequestURI to String: " + requestedFile);
            String f = requestedFile.substring(requestedFile.lastIndexOf("/") + 1);
            System.out.println("substring ^ lastIndexOf / +1 :" + f);
            String extension = f.substring(f.lastIndexOf("."));
            System.out.println("substring ^ last Index Of . :" + extension);
            String mime = "";
            switch (extension) {
                case ".pdf":
                    mime = "application/pdf";
                    break;
                case ".png":
                    mime = "image/png";
                    break;
                case ".js":
                    mime = "text/javascript";
                    break;
                case ".html":
                    mime = "text/html";
                    break;
                case ".jar":
                    mime = "application/java-archive";
                    break;
            }
            File file = new File(publicFolder + f);
            System.out.println("File is : " + publicFolder + f);
            byte[] bytesToSend = new byte[(int) file.length()];
            String errorMsg = null;
            int responseCode = 200;
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytesToSend, 0, bytesToSend.length);
            } catch (IOException ie) {
                errorMsg = "<h1>404 Not Found <3</h1>No context found for request";
            }
            if (errorMsg == null) {
                Headers h = he.getResponseHeaders();
                h.set("Content-Type", mime);
            } else {
                responseCode = 404;
                bytesToSend = errorMsg.getBytes();

            }
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); //<SWAG
            //^^Used if not on the same port, not same origine/mashine etc.
            he.sendResponseHeaders(responseCode, bytesToSend.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(bytesToSend, 0, bytesToSend.length);
            }
        }
    }

}
