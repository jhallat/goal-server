package com.jhallat.goalserver.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhallat.goalserver.model.TaskCompletionDTO;
import com.jhallat.goalserver.resource.TaskRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;

@ApplicationScoped
public class TaskConsumer implements Runnable {

    //@Inject
    //ConnectionFactory connectionFactory;

    @Inject
    TaskRepository taskRepository;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void onStart(@Observes StartupEvent event) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent event) {
        scheduler.shutdown();
    }

    @Override
    public void run() {

        //TODO replace with the push API
        try (Connection connection = createFactory().newConnection();
             Channel channel = connection.createChannel()) {
            //JMSConsumer consumer = context.createConsumer(context.createQueue("task.completed"));
            while (true) {
                //Message message = consumer.receive();
                GetResponse response =channel.basicGet("task.completed", true);
                if (response == null) continue;
                var taskMessage = response.getBody();
                var task = new String(taskMessage);
                //TODO replace with logger
                System.out.println(task);
                ObjectMapper mapper = new ObjectMapper();
                TaskCompletionDTO taskCompletionDTO = mapper.readValue(task, TaskCompletionDTO.class);
                taskRepository.updateCompleted(taskCompletionDTO);
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException | IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    //TODO Replace hard coded values with config
    private ConnectionFactory createFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        return factory;
    }
}
