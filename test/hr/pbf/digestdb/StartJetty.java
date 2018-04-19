package hr.pbf.digestdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hr.pbf.digestdb.web.WebListener;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartJetty {
    private static Server server;

    public static void main(String[] args) {

        server = new Server();

        HttpConfiguration http_config = new HttpConfiguration();
        // http_config.setSecureScheme("https");
        // http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(8080);
        http.setIdleTimeout(1000 * 60 * 60);


        server.addConnector(http);

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath("/");
        bb.setWar("C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\war");

        addJSP(bb);

        server.setHandler(bb);


        try {
            server.start();
            // server.dumpStdErr();
            new Thread() {
                public void run() {
                    try {
                        listenLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ;
            }.start();
            server.join();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                server.stop();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            System.exit(100);
        }
    }

    private static void addJSP(WebAppContext webapp) {
        webapp.setExtractWAR(true);

        // This webapp will use jsps and jstl. We need to enable the
        // AnnotationConfiguration in order to correctly
        // set up the jsp container
        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration");

        // Set the ContainerIncludeJarPattern so that jetty examines these
        // container-path jars for tlds, web-fragments etc.
        // If you omit the jar that contains the jstl .tlds, the jsp engine will
        // scan for them instead.
        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");

        // A WebAppContext is a ContextHandler as well so it needs to be set to
        // the server so it is aware of where to
        // send the appropriate requests.
        // server.setHandler( webapp );
    }

    private static final Logger log = LoggerFactory.getLogger(StartJetty.class);

    private static void listenLine() throws IOException {
        log.debug("listen from console: s i r");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("reading...");
        String line = r.readLine();
        while (line != null) {
            if ("s".equals(line.trim())) {
                System.out.println("STOP");
                try {
                    server.stop();
                    server.join();
                    WebListener.getFinder().close();
                    log.debug("Dela**+");
                    System.out.println("Stoped je...");
                    // System.exit(1);
                    r.close();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if ("r".equals(line.trim())) {
                System.out.println("RESTART");
                try {
                    server.stop();
                    server.join();
                    server.start();
                    // server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            line = r.readLine();
        }
    }
}
