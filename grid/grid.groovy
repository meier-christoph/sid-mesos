#!/usr/bin/env groovy
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
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

class WebServicesServlet extends HttpServlet {

  public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  @Override
  protected void doGet(
      final HttpServletRequest req,
      final HttpServletResponse resp) throws ServletException, IOException {
    resp.contentType = "application/json"
    resp.characterEncoding = "UTF-8"

    def cluster = Cluster.builder()
        .addContactPoint(System.env.CASSANDRA_HOST ?: "192.168.99.100")
        .withPort(System.env.CASSANDRA_PORT ?: 9042)
        .build()

    def metadata = cluster.getMetadata()
    printf "Connected to cluster: $metadata.clusterName\n"
    metadata.allHosts.each {
      println "datacenter: $it.datacenter, host: $it.address, rack: $it.rack"
    }

    def session = cluster.connect()
    ResultSet rs = session.execute("SELECT * FROM walker.grid;")

    def list = rs.collect {
      def coord = it.getString("coord")
      def x = ((int) coord.charAt(0)) - 65 // A
      def y = Integer.parseInt(coord.substring(1)) - 1
      def nb = it.getInt("nb") ?: 0
      """{"x": $x,"y": $y,"nb": $nb}"""
    }

    def writer = resp.getWriter()
    writer << """{"results": [${list.join(',')}]}"""
    writer.flush()

    session.close()
    cluster.close()
  }
}

def server = new Server(8080)
HandlerList list = new HandlerList()

def res = new ResourceHandler()
res.setResourceBase('.')
list.addHandler(res)

def servlet = new ServletContextHandler()
servlet.contextPath = '/'
servlet.addServlet(WebServicesServlet, '/data')
list.addHandler(servlet)

server.handler = list

println "Starting Jetty, press Ctrl+C to stop."
server.start()
