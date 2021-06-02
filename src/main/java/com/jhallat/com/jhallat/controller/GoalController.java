package com.jhallat.com.jhallat.controller;

import com.jhallat.model.Goal;
import com.jhallat.resource.GoalRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("goals")
public class GoalController {

    @Inject
    GoalRepository repository;

    @GET
    public Multi<Goal> getGoals() {
        return repository.findAll();
    }

    @POST
    public Uni<Goal> create(Goal goal) {
        return repository.insert(goal);
    }
}
