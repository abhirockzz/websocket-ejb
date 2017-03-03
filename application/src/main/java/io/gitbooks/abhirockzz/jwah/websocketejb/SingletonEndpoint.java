package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;

import javax.ejb.Singleton;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@Singleton
@ServerEndpoint("/singleton/")
public class SingletonEndpoint {

    @OnOpen
    public void onopen(Session s) throws IOException, InterruptedException {
        //Thread.currentThread().sleep(1000);
        s.getBasicRemote().sendText(String.valueOf(hashCode()));        
    }
    
    @OnMessage
    @Lock(LockType.READ)
    public void onmsg(Session s, String msg) throws IOException, InterruptedException {
        //Thread.currentThread().sleep(1000);
        s.getBasicRemote().sendText("hello client from "+String.valueOf(hashCode()));
    }

    @PreDestroy
    public void onDestroy() {
        System.out.println("Singleton bean " + hashCode() + " will be destroyed");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Closed " + session.getId() + " due to " + closeReason.getCloseCode());
    }
}
