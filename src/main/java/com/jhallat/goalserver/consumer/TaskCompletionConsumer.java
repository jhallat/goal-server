package com.jhallat.goalserver.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhallat.goalserver.model.TaskCompletionDTO;
import com.jhallat.goalserver.resource.TaskRepository;
import com.rabbitmq.client.*;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class TaskCompletionConsumer {

    @Inject
    TaskRepository taskRepository;

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

    @ConfigProperty(name = "queue.task.completed")
    String taskCompletedQueue;

    private Connection connection;
    private Channel channel;

    private static final Logger LOG = Logger.getLogger(TaskCompletionConsumer.class);

    void onStart(@Observes StartupEvent event) {
        try {
            connection = createFactory().newConnection();
            channel = connection.createChannel();

            channel.basicConsume(taskCompletedQueue, true, "task-completed-tag",
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                            super.handleShutdownSignal(consumerTag, sig);
                            LOG.info("Task consumer shutdown");
                        }

                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body) throws IOException {
                            var task = new String(body);
                            LOG.debug("Message received: " + task);
                            ObjectMapper mapper = new ObjectMapper();
                            TaskCompletionDTO taskCompletionDTO = mapper.readValue(task, TaskCompletionDTO.class);
                            taskRepository.findById(taskCompletionDTO.taskId())
                                    .subscribe()
                                    .with(currentTask -> {
                                        if (currentTask == null) {
                                            LOG.warn("Task with id " + taskCompletionDTO.taskId() + " not found");
                                        }
                                        int newStatus = taskCompletionDTO.completed() ? 3 : 2; //TODO replace with constants
                                        if (currentTask.isOngoing() && taskCompletionDTO.completed()) {
                                            newStatus = 1;
                                        }
                                        Uni<Boolean> result = taskRepository.updateStatus(taskCompletionDTO.taskId(), newStatus);
                                        result.subscribe()
                                                .with(updated ->
                                                        LOG.debug("Update task: " + taskCompletionDTO.taskId() + ":" + updated));
                                    });

                        }


                    });

        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
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
