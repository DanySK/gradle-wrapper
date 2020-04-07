package it.unibo.iot;

import it.unibo.iot.domain.impl.prodcons.v2.ConsumerServerWithAck;
import it.unibo.iot.domain.impl.prodcons.v2.ProducerClientWithAck;
import it.unibo.iot.domain.impl.support.EventEmitterFactory;
import it.unibo.iot.domain.impl.support.EventService;
import it.unibo.iot.domain.impl.support.EventServiceServerPort;
import it.unibo.iot.domain.impl.support.GlobalConfig;
import it.unibo.iot.domain.interfaces.EmitterFactory;
import it.unibo.iot.interaction.impl.TCPConnectionFactory;
import it.unibo.iot.interaction.interfaces.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConfiguratorOneToOneClientServerAckTest {
    private EventService evs;

    @Before public void setup() throws Exception {
        final ExecutorService es = Executors.newCachedThreadPool();
        ConnectionFactory cf = new TCPConnectionFactory();
        //ConnectionFactory cf = ZMQConnectionFactories.ReqRep;
        EmitterFactory ef = new EventEmitterFactory();
        int esport = TestUtils.getEphemeralPort();
        evs = new EventService(new EventServiceServerPort(esport));
        es.execute(evs);
        int port = TestUtils.getEphemeralPort();
        es.execute(new ConsumerServerWithAck(ef.createEmitter("", "127.0.0.1", esport), cf.connection(), port));
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        es.execute(new ProducerClientWithAck(ef.createEmitter("", "127.0.0.1", esport), cf.connection(), "127.0.0.1", port));
        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test public void basicTest() throws IOException {
        List<String> lines = evs.getAllEvents();
        ProducerConsumerAssertions.assertOrderedConsumptionOnLog(lines);
    }

}