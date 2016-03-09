#!/usr/bin/env groovy
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.ServletContextHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Grab(group = 'com.google.guava', module = 'guava', version = '19.0')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.5')
@Grab(group = 'org.eclipse.jetty.aggregate', module = 'jetty-all', version = '7.6.19.v20160209')
@Grab(group = 'com.datastax.cassandra', module = 'cassandra-driver-core', version = '3.0.0')

class GridServlet extends HttpServlet {

  @Override
  protected void doGet(
      final HttpServletRequest req,
      final HttpServletResponse resp) throws ServletException, IOException {
    resp.contentType = "text/html"
    resp.characterEncoding = "UTF-8"

    def warning = req.getParameter('warning') ?: '10'
    def danger = req.getParameter('danger') ?: '100'

    def writer = resp.getWriter()
    writer << """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <script src="//d3js.org/d3.v3.min.js" charset="utf-8"></script>
  <script src="//code.jquery.com/jquery-2.2.1.min.js" charset="utf-8"></script>
</head>
<body>
<div id="graph"></div>
<script>
  var width = 500;
  var height = 500;
  var vx = width / 10;
  var vy = height / 10;
  var margin = {top: 10, right: 10, bottom: 10, left: 10};

  var svg = d3.select("#graph")
      .append("svg").attr("width", width).attr("height", height);

  function renderRect(d) {
    svg.append("rect")
        .attr("x", d.x * vx)
        .attr("y", vy * d.y)
        .attr("height", vy)
        .attr("width", vx)
        .attr("fill", d.color)
        .attr("stroke", "black")
        .attr("stroke-width", 1);
  }

  function renderGrid() {
    \$.getJSON("data?warning=$warning&danger=$danger", function (data) {
      svg.selectAll("*").remove();

      for (var i = 0; i < 10; i++) {
        for (var j = 0; j < 10; j++) {
          renderRect({x: i, y: j, nb: 0, color: 'white'});
        }
      }

      data.results.map(function (d) {
        renderRect(d);
      });
    });
  }

  setInterval(function () {
    renderGrid();
  }, 1000);
</script>
</body>
</html>
"""
    writer.flush()
  }
}

class DataServlet extends HttpServlet {

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

    def warning = req.getParameter('warning') ?: '10'
    def danger = req.getParameter('danger') ?: '100'

    def latest = [:]
    rs.each {
      def coord = it.getString("coord")
      def ts = it.getTimestamp("ts").time
      def nb = it.getInt("nb") ?: 0
      def previous = latest[coord]
      if (previous) {
        if (previous.ts < ts) {
          latest[coord] = [nb: nb, ts: ts]
        }
      } else {
        latest[coord] = [nb: nb, ts: ts]
      }
    }

    def list = latest.collect { String k, v ->
      def x = ((int) k.charAt(0)) - 65 // A
      def y = Integer.parseInt(k.substring(1)) - 1
      def nb = v.nb
      def color = nb > Integer.parseInt(danger) ? 'red' : (nb > Integer.parseInt(warning) ? 'yellow' : 'green')
      """{"x": $x,"y": $y,"nb": $nb, "color": "$color"}"""
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

def servlet = new ServletContextHandler()
servlet.contextPath = '/'
servlet.addServlet(GridServlet, '/')
servlet.addServlet(DataServlet, '/data')
list.addHandler(servlet)

server.handler = list

println "Starting Jetty, press Ctrl+C to stop."
server.start()
