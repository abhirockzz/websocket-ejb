package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.Session;

public class ConnectToServer implements Runnable {

    Endpoint client;
    String serverURL;
    CountDownLatch startSignal = null;
    CountDownLatch closeConnSignal = null;
    Session session = null;

    public ConnectToServer(Endpoint client, String serverURL, CountDownLatch startLatch, CountDownLatch closeConnLatch) {
        this.client = client;
        this.serverURL = serverURL;
        this.startSignal = startLatch;
        this.closeConnSignal = closeConnLatch;
    }

    private void waitForStartSignal() throws InterruptedException {
        startSignal.await();
    }

    private void disconnectAfterClose() throws InterruptedException, IOException {
        closeConnSignal.await();
        session.close();
    }

    @Override
    public void run() {

        System.out.println(Thread.currentThread() + " is Q'd");
        try {
            waitForStartSignal();
        } catch (InterruptedException ex) {
            Logger.getLogger(WebSocketEJBTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.session = ContainerProvider.getWebSocketContainer()
                    .connectToServer(client,
                            URI.create(serverURL));

            System.out.println("Session created " + session.getId());
            disconnectAfterClose();

        } catch (DeploymentException ex) {
            Logger.getLogger(WebSocketEJBTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebSocketEJBTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ConnectToServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
