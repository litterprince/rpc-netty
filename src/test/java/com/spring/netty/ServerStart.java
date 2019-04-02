package com.spring.netty;

import com.spring.netty.RPCServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/spring-context.xml", "/spring/spring-server.xml"})
public class ServerStart {
    @Test
    public void start() {
        RPCServer.start();
    }
}
