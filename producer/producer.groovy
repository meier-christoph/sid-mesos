#!/usr/bin/env groovy
import com.google.common.collect.EvictingQueue
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Grab(group = 'com.google.guava', module = 'guava', version = '19.0')
@Grab(group = 'org.apache.kafka', module = 'kafka-clients', version = '0.8.2.2')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.5')
class C {
  static def r = new Random()
  static def xMax = 100
  static def yMax = 100
}

enum Direction {
  N(0, 1), NE(1, 1), E(1, 0), SE(1, -1), S(0, -1), SW(-1, -1), W(-1, 0), NW(-1, 1);

  def x
  def y

  Direction(def x, def y) {
    this.x = x
    this.y = y
  }

  static def random() {
    return values()[C.r.nextInt(values().size())]
  }
}

class Position {
  def x
  def y

  Position(def x, def y) {
    this.x = x
    this.y = y
  }

  def move(Direction d) {
    new Position(x + d.x, y + d.y)
  }

  def isCloseTo(Position p) {
    (x - p.x).abs() + (y - p.y).abs() < 2
  }

  def isValid() {
    x >= 0 && y >= 0 && x <= C.xMax && y <= C.yMax
  }

  @Override
  public String toString() {
    "$x:$y"
  }
}

class Walker {
  private final def lock = new ReentrantLock()
  def uid = UUID.randomUUID()
  def current = new Position(C.r.nextInt(C.xMax), C.r.nextInt(C.yMax))
  def previous = EvictingQueue.create(10)

  def move() {
    def p = current.move(Direction.random())
    while (!p.isValid()) { // must be a valid position at least
      p = current.move(Direction.random())
    }
    try {
      if (lock.tryLock(5, TimeUnit.SECONDS)) {
        def count = 0
        while (count < 10 && (!p.isValid() || previous.find { it.isCloseTo(p) })) {
          count++ // security in case we get cornered
          p = current.move(Direction.random())
        }
        previous.add(current)
      }
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock()
      }
    }
    current = p
  }
}

def walkers = []
(1..10).collect(walkers) { new Walker() }

println "Starting walkers, press Ctrl+C to stop."

def props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.env.PRODUCER_HOSTS ?: "192.168.99.100:9092");
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.StringSerializer');
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.StringSerializer');
def producer = new KafkaProducer<String, String>(props);
try {
  while (true) {
    println "move walker(s) ..."
    walkers.each { Walker w ->
      w.move()
      def record = new ProducerRecord<String, String>("walker", w.uid.toString(),
          """{"uid":"$w.uid", "x":$w.current.x, "y":$w.current.y}""" as String);
      producer.send(record).get()
    }
    Thread.sleep(1000)
  }
} finally {
  producer.close()
}
