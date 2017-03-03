package io.gitbooks.abhirockzz.jwah.websocketejb;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@Stateful
@ServerEndpoint("/stateful/")
public class StatefulEndpoint {
 
    
    @OnOpen
    public void onOpen(Session s) throws IOException{
        s.getBasicRemote().sendText(String.valueOf(hashCode()));
    }

    @PreDestroy
    public void onDestroy(){
        System.out.println("Stateful bean "+ hashCode() + " will be destroyed");
    }

    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Closed " + session.getId() + " due to " + closeReason.getCloseCode());
        clear();
    }
    
    @Remove
    public void clear(){
        System.out.println("Removing Stateful bean "+ hashCode());
    }
}
