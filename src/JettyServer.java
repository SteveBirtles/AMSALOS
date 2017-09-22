import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyServer extends AbstractHandler {

    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {

        if (request.getRequestURI().equals("/favicon.ico")) return;

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>It works, Steve!</h1>");
        System.out.println("Request received from " + request.getRemoteAddr());
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8081);
        server.setHandler(new JettyServer());
        server.start();
        server.join();
    }

}