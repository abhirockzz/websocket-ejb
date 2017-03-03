package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.Session;

public class SendMessageToServerEndpoint implements Runnable {

    CountDownLatch startSignal = null;
    CountDownLatch closeConnSignal = null;
    Session session = null;

    public SendMessageToServerEndpoint(Session session , CountDownLatch startLatch, CountDownLatch closeConnLatch) {
        this.session = session;
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
            
            this.session.getBasicRemote().sendText("hello server");
            disconnectAfterClose();

        }  catch (IOException ex) {
            Logger.getLogger(WebSocketEJBTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SendMessageToServerEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
