package com.jhallat.goalserver.controller;

import com.jhallat.goalserver.entity.Goal;
import com.jhallat.goalserver.model.GoalCreationDTO;
import com.jhallat.goalserver.resource.GoalRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalController {

    @Inject
    GoalRepository repository;

    @GET
    public Multi<Goal> getGoals() {
        return repository.findAll();
    }

    @GET
    @Path("{id}")
    public Uni<Goal> getGoal(@PathParam("id") long id) {
        return repository.findById(id);
    }

    @POST
    public Uni<Goal> createGoal(GoalCreationDTO goalCreationDTO) {
        Goal goal = new Goal(0, goalCreationDTO.description());
        return repository.insert(goal);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteGoal(@PathParam("id") Long id) {
        return repository.delete(id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());

    }
}
