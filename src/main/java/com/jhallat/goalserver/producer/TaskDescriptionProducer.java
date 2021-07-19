package com.jhallat.goalserver.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhallat.goalserver.consumer.TaskCompletionConsumer;
import com.jhallat.goalserver.model.TaskDescriptionDTO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class TaskDescriptionProducer {

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "queue.username")
    String queueUsername;

    @ConfigProperty(name = "queue.password")
    String queuePassword;

    @ConfigProperty(name = "queue.host")
    String queueHost;

    @ConfigProperty(name = "queue.virtual-host")
    String queueVirtualHost;

    @ConfigProperty(name = "queue.port")
    int queuePort;

    private Connection connection;
    private Channel channel;


    private static final Logger LOG = Logger.getLogger(TaskDescriptionProducer.class);

    void onStart(@Observes StartupEvent event) {

        try {
            connection = createFactory().newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException exception) {
            LOG.error("Error creating RabbitMQ connection", exception);
        }

    }

    public void sendDescription(TaskDescriptionDTO task) {
        try {
            String message = objectMapper.writeValueAsString(task);
            channel.basicPublish("", "task.description", null, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            LOG.error("Error mapping task object", exception);
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        try {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception exception) {
            LOG.error("Error closing connection to task consumer queue", exception);
        }
    }


    //TODO have a common connection factory
    private ConnectionFactory createFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(queueUsername);
        factory.setPassword(queuePassword);
        factory.setVirtualHost(queueVirtualHost);
        factory.setHost(queueHost);
        factory.setPort(queuePort);
        return factory;
    }
}
