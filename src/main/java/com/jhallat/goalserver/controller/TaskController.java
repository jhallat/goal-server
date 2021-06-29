package com.jhallat.goalserver.controller;

import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCompletionDTO;
import com.jhallat.goalserver.model.TaskCreationDTO;
import com.jhallat.goalserver.resource.TaskRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskController {

    @Inject
    TaskRepository repository;

    @GET
    @Path("{goalId}")
    public Multi<Task> getTasks(@PathParam("goalId") long goalId) {
        return repository.findAllByGoal(goalId);
    }

    @POST
    public Uni<Task> createTask(TaskCreationDTO taskCreationDTO) {
        return repository.insert(taskCreationDTO);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> completeTask(@PathParam("id") Long id, TaskCompletionDTO taskCompletion) {
        return repository.updateCompleted(taskCompletion)
                .onItem().transform(completed -> completed ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
