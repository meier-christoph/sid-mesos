#!/usr/bin/env groovy
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Host
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Grab(group = 'com.google.guava', module = 'guava', version = '19.0')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.5')
@Grab(group = 'org.eclipse.jetty.aggregate', module = 'jetty-all', version = '7.6.19.v20160209')
@Grab(group = 'com.datastax.cassandra', module = 'cassandra-driver-core', version = '3.0.0')
class C {
}

class WebServicesServlet extends HttpServlet {

    def cluster
    def session

    WebServicesServlet() {
        cluster = Cluster.builder().addContactPoint("192.168.99.100").withPort(9042).build()
        def metadata = cluster.getMetadata()
        printf("Connected to cluster: %s\n", metadata.getClusterName())
        for (final Host host : metadata.getAllHosts()) {
            printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(), host.getRack())
        }
        session = cluster.connect()
    }

    @Override
    protected void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {

        ResultSet rs = session.execute("SELECT * FROM walker.grid;")

        def list = rs.collect {
            """{"coord":"${it.getString("coord")}","ts":"${
                it.getTimestamp("ts").format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            }","nb":${it.getInt("nb") ?: 0}}"""
        }

        resp.addHeader("Access-Control-Allow-Origin", "*")
        //"Access-Control-Allow-Methods: POST, GET, OPTIONS\n" +
        //"Access-Control-Allow-Headers: X-PINGOTHER")
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        def writer = resp.getWriter()
        writer << "{\"results\":["
        writer << list.join(",")
        writer << "]}"
        writer.flush()

    }
}

def server = new Server(8080)
HandlerList list = new HandlerList()

def res = new ResourceHandler()
res.setResourceBase('.')
list.addHandler(res)

def servlet = new ServletContextHandler()
servlet.contextPath = '/'
servlet.addServlet(WebServicesServlet, '/zombies')
list.addHandler(servlet)

server.handler = list

println "Starting Jetty, press Ctrl+C to stop."
server.start()
