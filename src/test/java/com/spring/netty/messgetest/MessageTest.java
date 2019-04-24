package com.spring.netty.messgetest;

import com.spring.netty.message.Request;
import com.spring.netty.protocol.Protocol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-context.xml"})
public class MessageTest {

    @Test
    public void test() throws IOException, ClassNotFoundException {
        Request request = new Request();
        request.setRequestId("1");
        //返回表示声明由此 Method 对象表示的方法的类或接口的Class对象
        request.setClassName("className");
        request.setMethodName("methodName");
        request.setParameters(new Object[]{"1","2"});//输入的实参

        Protocol protocol = new Protocol();
        protocol.buildRequestProtocol(request);

        Request request1 = protocol.buildRequestByProtocol();
        System.out.println(request.equals(request1));
    }
}
