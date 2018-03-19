package hr.pbf.digestdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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
		bb.setWar("war");

		addJSP(bb);

		// uncomment the next two lines if you want to start Jetty with WebSocket
		// (JSR-356) support
		// you need org.apache.wicket:wicket-native-websocket-javax in the classpath!
		// ServerContainer serverContainer =
		// WebSocketServerContainerInitializer.configureContext(bb);
		// serverContainer.addEndpoint(new WicketServerEndpointConfig());

		// uncomment next line if you want to test with JSESSIONID encoded in the urls
		// ((AbstractSessionManager)
		// bb.getSessionHandler().getSessionManager()).setUsingCookies(false);

		server.setHandler(bb);

		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.addEventListener(mBeanContainer);
		// server.addBean(mBeanContainer);

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
				};
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
