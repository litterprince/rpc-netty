package com.spring.netty;

import com.spring.common.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/spring-context.xml", "/spring/spring-client.xml"})
public class ClientStart {
    @Test
    public void start() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        final HelloService service = (HelloService) RPC.call(HelloService.class);
        int i = 3;
        while (i -- > 0) {
            int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    service.sayHello("jeff" + finalI);
                }
            });
            Thread.sleep(100);
        }
        System.in.read();
    }
}
