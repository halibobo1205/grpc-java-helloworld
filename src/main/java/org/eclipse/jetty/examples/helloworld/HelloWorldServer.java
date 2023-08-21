package org.eclipse.jetty.examples.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
@Slf4j(topic = "server")
public class HelloWorldServer {

  private Server server;

  private void start() throws Exception {
    /* The port on which the server should run */
    int port = 8080;
    server = new Server(port);
    ServletHandler servletHandler = new ServletHandler();
    server.setHandler(servletHandler);

    servletHandler.addBean(new ConnectionLimit(Runtime.getRuntime().availableProcessors(), server));
    servletHandler.addServletWithMapping(HelloWorldServlet.class, "/hello");
    servletHandler.addFilterWithMapping(JettyDosFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

    server.setStopAtShutdown(true);
    server.setStopTimeout(30_000);
    server.dumpStdErr();
    server.start();
    server.join();
    logger.info("Server started, listening on " + port);
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws Exception {
    final HelloWorldServer server = new HelloWorldServer();
    server.start();
  }

}