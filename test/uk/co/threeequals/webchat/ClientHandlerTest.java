/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.threeequals.webchat;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michaelwaterworth
 */
public class ClientHandlerTest {
    Group gr;
    
    public ClientHandlerTest() {
        gr = new Group();
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class ClientHandler.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String expResult2 = "My Name";
        gr = new Group();
        ClientHandler cl = new ClientHandler(gr);
        cl.processSendMessage(expResult2);
        String result2 = cl.getName();
        assertEquals(expResult2, result2);
    }

//    /**
//     * Test of bulkReceiver method, of class ClientHandler.
//     */
//    @Test
//    public void testBulkReceiver() {
//        System.out.println("bulkReceiver");
//        List<Message> m = null;
//        ClientHandler instance = new ClientHandler(gr);
//        instance.bulkReceiver(m);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of sendMessage method, of class ClientHandler.
     */
    @Test
    public void testSendMessage() {
        ClientHandler cl1 = new ClientHandler(gr);
        ClientHandler cl2 = new ClientHandler(gr);
        ClientHandler cl3 = new ClientHandler(gr);
        Message[] messages;
        
        //Set names
        cl1.processSendMessage("Cl1_sendp");
        cl2.processSendMessage("Cl2_sendp");
        cl3.processSendMessage("Cl3_sendp");
        
        System.out.println("sendPrivateMessage");
        String testStr = "Mess(ge)";
        Message m = new Message(testStr, null, cl1.getName());
        
        //Clear message buffers
        cl1.getMessages();
        cl2.getMessages();
        cl3.getMessages();
        
        //Send message
        cl1.sendMessage(m);

        messages = cl1.getMessages();
        
        assertEquals(0, messages.length);
        
        messages = cl2.getMessages();
        
        assertEquals(1, messages.length);
        
        messages = cl3.getMessages();

        assertEquals(1, messages.length);
    }
    
    /**
     * Test of sendMessage method, of class ClientHandler.
     */
    @Test
    public void testSendMessageBufferSize() {
        ClientHandler cl1 = new ClientHandler(gr);
        ClientHandler cl2 = new ClientHandler(gr);
        Message[] messages;
        
        //Set names
        cl1.processSendMessage("Cl1_sendp");
        cl2.processSendMessage("Cl2_sendp");
        
        String testStr = "Mess(ge)";

        //Clear message buffers
        cl1.getMessages();
        cl2.getMessages();
        
        //Add 30 messages to the queue
        int i = 0;
        
        //Send messages to fill queue
        while(i < 30){
            Message m = new Message(testStr + i, null, cl1.getName());
            cl1.sendMessage(m);
            i++;
        }

        messages = cl1.getMessages();
        assertEquals(0, messages.length);
        
        messages = cl2.getMessages();
        assertEquals(30, messages.length);
        
        ClientHandler cl3 = new ClientHandler(gr);
        cl3.getMessages();
        cl3.processSendMessage("Cl3_sendp");
        messages = cl3.getMessages();
        System.out.println(messages[0].body);
        assertEquals(Group.MESSAGEQUEUELENGTH + 1, messages.length);
    }


    /**
     * Test of getMessages method, of class ClientHandler.
     */
    @Test
    public void testGetMessages() {
        ClientHandler cl1 = new ClientHandler(gr);
        cl1.processSendMessage("Cl1");

        System.out.println("getMessages");
        Message[] expResult = null;
        Message[] result = cl1.getMessages();
        assertEquals(2, result.length);
    }
    
    /**
     * Test of sendMessage method, of class ClientHandler.
     */
    @Test
    public void testSendPrivateMessage() {
        ClientHandler cl1 = new ClientHandler(gr);
        ClientHandler cl2 = new ClientHandler(gr);
        ClientHandler cl3 = new ClientHandler(gr);
        Message[] messages;
        
        //Set names
        cl1.processSendMessage("Cl1_sendp");
        cl2.processSendMessage("Cl2_sendp");
        cl3.processSendMessage("Cl3_sendp");
        
        System.out.println("sendPrivateMessage");
        String testStr = "Mess(ge)";
        Message m = new Message(testStr, "Cl2_sendp", "Cl1_sendp");
        
        //Clear message buffers
        cl1.getMessages();
        cl2.getMessages();
        cl3.getMessages();
        
        //Send message
        cl1.sendMessage(m);

        messages = cl1.getMessages();
        assertEquals(0, messages.length);
        
        messages = cl2.getMessages();
        assertEquals(1, messages.length);
        
        messages = cl3.getMessages();
        assertEquals(0, messages.length);
    }
    
}