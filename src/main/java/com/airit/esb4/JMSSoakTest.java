package com.airit.esb4;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mwillingham on 5/9/2016.
 */
public class JMSSoakTest {

    public static void main(String[] args) throws Exception
    {

        //BasicConfigurator.configure();

        final SimpleRegistry registry = new SimpleRegistry();

        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        final JmsComponent mqJmsComponent = new JmsComponent();
        mqJmsComponent.setConnectionFactory(factory);

        registry.put("faajms", mqJmsComponent);

        final CamelContext context = new DefaultCamelContext(registry);

        final AtomicInteger total = new AtomicInteger(0);

        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                from("faajms:queue:test")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                total.incrementAndGet();
                            }
                        });


                from("timer://test?period=1")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                exchange.getIn().setBody("Test");
                            }
                        })
                        .to("faajms:queue:test");

            }
        });

        context.start();

        Thread.sleep(10000);

        context.stop();

        System.out.println(total.get());

    }

}
