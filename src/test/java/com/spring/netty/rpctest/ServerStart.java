package com.spring.netty.rpctest;

import com.spring.netty.RPC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/rpctest/spring-server.xml"})
public class ServerStart {
    @Test
    public void start() {
        RPC.serverStart();
    }
}
