package com.jhallat.goalserver.com.jhallat.controller;

import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCreationDTO;
import com.jhallat.goalserver.resource.TaskRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
}
