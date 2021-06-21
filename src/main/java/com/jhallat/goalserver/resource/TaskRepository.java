package com.jhallat.goalserver.resource;

import com.jhallat.goalserver.entity.Goal;
import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCreationDTO;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TaskRepository {

    @Inject
    PgPool client;

    public Multi<Task> findAllByGoal(long goalId) {
        return client.preparedQuery(
                """
                    SELECT id, 
                           goal_Id, 
                           description, 
                           completed 
                    FROM tasks
                    WHERE goal_id = $1 
                    ORDER BY completed ASC, description ASC
                """)
                .execute(Tuple.of(goalId))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> new Task(row.getLong("id"),
                        row.getLong("goal_id"), row.getString("description"),
                        row.getBoolean("completed")));
    }

    public Uni<Task> insert(TaskCreationDTO task) {
        return client.preparedQuery("INSERT INTO tasks(goal_id, description, completed) VALUES ($1, $2, $3) RETURNING id")
                .execute(Tuple.of(task.goalId(), task.description(), false))
                .onItem().transform(rowSet -> {
                    var row = rowSet.iterator().next();
                    return new Task(row.getLong("id"), task.goalId(), task.description(), false);
                });
    }
}
