package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.ejb.Stateless;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@Stateless
@ServerEndpoint("/stateless/")
public class StatelessEndpoint {

    @OnOpen
    public void onopen(Session s) throws IOException, InterruptedException {
        /**
         * simulate minor delay. helps confirm if additional instance of this bean will be created in
         * case concurrent requests come through
         */
        Thread.currentThread().sleep(1000); 
        s.getBasicRemote().sendText(String.valueOf(hashCode()));
    }

    @OnMessage
    public void onmsg(Session s, String msg) throws IOException, InterruptedException {
        //Thread.currentThread().sleep(1000);
        s.getBasicRemote().sendText("hello client.. my hasCode() is "+String.valueOf(hashCode()));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("closed " + session.getId() + " due to " + closeReason.getCloseCode());
    }

    @PreDestroy
    public void onDestroy() {
        System.out.println("going to be destroyed...");
    }

}
