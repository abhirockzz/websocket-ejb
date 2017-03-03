package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WebSocketEJBTest {

    public WebSocketEJBTest() {
    }

    static final int NUM_OF_THREADS = 5;

    ExecutorService es = null;
    CountDownLatch msgReceivedLatch = null;
    CountDownLatch startLatch = null;
    CountDownLatch closeConnLatch = null;

    @Before
    public void setUp() {
        es = Executors.newFixedThreadPool(NUM_OF_THREADS);
        msgReceivedLatch = new CountDownLatch(NUM_OF_THREADS);
        startLatch = new CountDownLatch(1);
        closeConnLatch = new CountDownLatch(1);
    }

    @After
    public void tearDown() {
        msgReceivedLatch = null;
        startLatch = null;
        closeConnLatch = null;
        es.shutdown();
        es = null;
    }

    final static String BASE_URL = "ws://localhost:8080/websocket-ejb/";

    /**
     * Used to assert that a SLSB instance is obtained from the EJB pool (if
     * needed) for a WebSocket client. The server endpoint returns the hashCode
     * of the EJB as a response message and the same is stored (in a
     * Set<String>) by the client. For a successful scenario, it's sufficient to
     * confirm that the Set contains more than 1 entries (at least 2 SLSBs were
     * created)
     *
     * @throws DeploymentException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void aSLSBPickedFromPoolForEachClient() throws DeploymentException, IOException, InterruptedException {
        final ProgrammaticWebsocketClient client = new ProgrammaticWebsocketClient(msgReceivedLatch);

        ConnectToServer connectToServer = new ConnectToServer(client, BASE_URL + "stateless/", startLatch, closeConnLatch);

        for (int i = 1; i <= NUM_OF_THREADS; i++) {
            es.execute(connectToServer);
        }

        startLatch.countDown();

        System.out.println("All threads started");

        assertTrue("Please try test with more than ONE thread", NUM_OF_THREADS > 1);
        assertTrue(msgReceivedLatch.await(NUM_OF_THREADS, TimeUnit.SECONDS)); //1 sec per thread

        System.out.println(client.getResponses().size() + " SLSB instances were created");
        assertTrue("at least two different SLSB should have been created by the container", client.getResponses().size() > 1);

        closeConnLatch.countDown(); // initiate session close
    }

    /**
     * Used to assert the same SLSB instance is used throughout the lifecycle of
     * a WebSocket session i.e. connect, send/receive messages, disconnect (The
     * instance is obtained from the EJB pool)
     *
     * The server endpoint returns the hashCode of the EJB as a response message
     * via the onOpen callback and a hello (appended by the hashCode) in the
     * onMessage callback, and the same is stored (in a Set<String>) by the
     * client. For a successful scenario, it's sufficient to confirm that the
     * Set contains 2 entries - 1 response each the onOpen callback and one
     * response (only unique entries in a Set) from onMessage callback
     *
     * @throws DeploymentException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void theSameSLSBInstanceIsUsedForMultipleInteractionsWithinASession() throws DeploymentException, IOException, InterruptedException {

        ProgrammaticWebsocketClient client = new ProgrammaticWebsocketClient(msgReceivedLatch);
        Session session = ContainerProvider.getWebSocketContainer()
                .connectToServer(client,
                        URI.create(BASE_URL + "stateless/"));

        SendMessageToServerEndpoint sendMsg = new SendMessageToServerEndpoint(session, startLatch, closeConnLatch);
        for (int n = 1; n <= NUM_OF_THREADS; n++) {
            es.execute(sendMsg);
        }

        startLatch.countDown();

        System.out.println("All threads started");

        msgReceivedLatch.await(); //wait for acitivity to finish
        assertEquals(2, client.getResponses().size()); //

        closeConnLatch.countDown(); // initiate session close
        

    }

    /**
     * Used to assert that the SAME singleton EJB instance is invoked for
     * establishing a connection with different clients (threads). The server
     * endpoint returns the hashCode of the EJB as a response message and the
     * same is stored (in a Set<String>) by the client. For a successful
     * scenario, it's sufficient to confirm that the Set contains ONLY 1 entry
     * (only unique entries in a Set)
     *
     * @throws DeploymentException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void theSameSingletonInstanceIsUsedForAllSessions() throws DeploymentException, IOException, InterruptedException {
        final ProgrammaticWebsocketClient client = new ProgrammaticWebsocketClient(msgReceivedLatch);

        ConnectToServer connectToServer = new ConnectToServer(client, BASE_URL + "singleton/", startLatch, closeConnLatch);

        for (int i = 1; i <= NUM_OF_THREADS; i++) {
            //new Thread(connectToServer).start();
            es.execute(connectToServer);
        }

        startLatch.countDown();

        System.out.println("All threads started");

        assertTrue(msgReceivedLatch.await(NUM_OF_THREADS, TimeUnit.SECONDS)); //1 sec per thread
        assertEquals(1, client.getResponses().size());

        closeConnLatch.countDown(); // initiate session close

    }

   
    /**
     * Used to assert that a unique Stateful bean is obtained from the EJB pool (if
     * needed) for each WebSocket client. The server endpoint returns the hashCode
     * of the EJB as a response message and the same is stored (in a
     * Set<String>) by the client. For a successful scenario, it's sufficient to
     * confirm that the Set contains as many entries as the number of clients
     *
     * @throws DeploymentException
     * @throws IOException
     * @throws InterruptedException
     */
    
    @Test
    public void uniqueStatefulBeanIsAssignedToEachClient() throws DeploymentException, IOException, InterruptedException {
        final ProgrammaticWebsocketClient client1 = new ProgrammaticWebsocketClient(msgReceivedLatch);

        ConnectToServer connectToServer = new ConnectToServer(client1, BASE_URL + "stateful/", startLatch, closeConnLatch);

        for (int i = 1; i <= NUM_OF_THREADS; i++) {
            es.execute(connectToServer);
        }

        startLatch.countDown();

        System.out.println("All threads started");

        int timeout = NUM_OF_THREADS == 1 ? 5 : NUM_OF_THREADS; //if no. of threads is just one, then need to be slightly more liberal about the timout

        assertTrue(msgReceivedLatch.await(timeout, TimeUnit.SECONDS)); //1 sec per thread
        assertEquals(NUM_OF_THREADS, client1.getResponses().size());

        closeConnLatch.countDown(); // initiate session close

    }

    public static class ProgrammaticWebsocketClient extends Endpoint {

        CountDownLatch msgReceivedLatch;

        public ProgrammaticWebsocketClient(CountDownLatch msgReceivedLatch) {
            this.msgReceivedLatch = msgReceivedLatch;
        }

        public Set<String> getResponses() {
            return Collections.unmodifiableSet(responses);
        }

        Set<String> responses = new HashSet<>();

        @Override
        public void onOpen(Session sn, EndpointConfig ec) {
            sn.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String t) {
                    System.out.println("Got msg from server -- " + t);
                    responses.add(t);
                    msgReceivedLatch.countDown();
                }
            });

        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("closed " + session.getId() + " due to " + closeReason.getCloseCode());
        }

    }
 
}
