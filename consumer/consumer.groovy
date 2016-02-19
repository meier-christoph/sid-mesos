#!/usr/bin/env groovy
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import kafka.consumer.KafkaStream
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Grab(group = 'com.google.guava', module = 'guava', version = '19.0')
@Grab(group = 'org.eclipse.jetty.aggregate', module = 'jetty-all', version = '7.6.19.v20160209')
@Grab(group = 'org.apache.kafka', module = 'kafka_2.11', version = '0.8.2.2')
class C {
  static def eventBus = new EventBus()
}

class EventPoller implements Runnable {

  @Override
  void run() {
    def connector
    try {
      def props = new Properties();
      props.put("zookeeper.connect", System.env.CONSUMER_HOSTS ?: '192.168.99.100:2181');
      props.put("group.id", 'walker');
      props.put("zookeeper.session.timeout.ms", "400");
      props.put("zookeeper.sync.time.ms", "200");
      props.put("auto.commit.interval.ms", "1000");
      connector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props))
      def streams = connector.createMessageStreams([walker: 1])
      streams.each { topic ->
        topic.value.each { KafkaStream<byte[], byte[]> stream ->
          println "polling ..."
          def iter = stream.iterator()
          while (iter.hasNext()) {
            def msg = new String(iter.next().message())
            C.eventBus.post(msg)
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
      System.exit(-1)
    } finally {
      if (connector) connector.shutdown()
    }
  }
}

class EventStreamer {

  Writer writer
  def count = 0

  @Subscribe
  void onMessage(String e) {
    writer.println("""event: data\ndata: $e\n""");
    writer.flush();
    count++
    if (count > 10) {
      count = 0
      writer.println("""event: render\ndata: render\n""");
      writer.flush();
      println "render ..."
    }
  }
}

class EventStreamServlet extends HttpServlet {

  @Override
  protected void doGet(
      final HttpServletRequest req,
      final HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/event-stream");
    resp.setCharacterEncoding("UTF-8");

    def writer = resp.getWriter()
    def streamer
    try {
      streamer = new EventStreamer()
      streamer.writer = writer
      C.eventBus.register(streamer)
      while (true) {
        println "alive ..."
        Thread.sleep(1000)
      }
    } catch (Exception e) {
      e.printStackTrace()
      System.exit(-1)
    } finally {
      if (streamer) C.eventBus.unregister(streamer)
    }
  }
}

def server = new Server(8080)
HandlerList list = new HandlerList()

def res = new ResourceHandler()
res.setResourceBase('.')
list.addHandler(res)

def servlet = new ServletContextHandler()
servlet.contextPath = '/'
servlet.addServlet(EventStreamServlet, '/event-stream')
list.addHandler(servlet)

server.handler = list

println "Starting Jetty, press Ctrl+C to stop."
new Thread(new EventPoller()).start()
server.start()
