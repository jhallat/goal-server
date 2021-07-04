package com.jhallat.goalserver.controller;

import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCreationDTO;
import com.jhallat.goalserver.model.TaskUpdateDTO;
import com.jhallat.goalserver.model.TaskUpdateStatusDTO;
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
    @Path("{id}/status")
    public Uni<Response> updateStatus(@PathParam("id") Long id, TaskUpdateStatusDTO statusDTO) {
        return repository.updateStatus(id, statusDTO.statusId())
                .onItem().transform(completed -> completed ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateTask(@PathParam("id") Long id, TaskUpdateDTO updateDTO) {
        return repository.updateTask(id, updateDTO)
                .onItem().transform(completed -> completed ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteTask(@PathParam("id") Long id) {
        return repository.deleteTask(id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
