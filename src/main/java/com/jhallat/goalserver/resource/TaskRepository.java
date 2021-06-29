package com.jhallat.goalserver.resource;

import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCompletionDTO;
import com.jhallat.goalserver.model.TaskCreationDTO;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TaskRepository {

    private static final String SQL_FIND_BY_GOAL = """
                    SELECT id, 
                           goal_Id, 
                           description, 
                           completed,
                           ongoing 
                    FROM tasks
                    WHERE goal_id = $1 
                    ORDER BY completed ASC, description ASC
                """;

    private static final String SQL_INSERT = """
            INSERT INTO tasks(goal_id, description, completed, ongoing)
            VALUES ($1, $2, $3, $4) RETURNING id
            """;

    private static final String SQL_UPDATE_COMPLETED = """
            UPDATE tasks
            SET completed = $2
            WHERE goal_id = $1
            """;

    @Inject
    PgPool client;

    public Multi<Task> findAllByGoal(long goalId) {
        return client.preparedQuery(SQL_FIND_BY_GOAL)
                .execute(Tuple.of(goalId))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> new Task(row.getLong("id"),
                                                    row.getLong("goal_id"),
                                                    row.getString("description"),
                                                    row.getBoolean("completed"),
                                                    row.getBoolean("ongoing")));
    }

    public Uni<Task> insert(TaskCreationDTO task) {
        return client.preparedQuery(SQL_INSERT)
                .execute(Tuple.of(task.goalId(),
                         task.description(),
                         false,
                         task.isOngoing()))
                .onItem().transform(rowSet -> {
                    var row = rowSet.iterator().next();
                    return new Task(row.getLong("id"),
                                    task.goalId(),
                                    task.description(),
                                    false,
                                    task.isOngoing());
                });
    }

    public Uni<Boolean> updateCompleted(TaskCompletionDTO taskCompletion) {
        return client.preparedQuery(SQL_UPDATE_COMPLETED)
                .execute(Tuple.of(taskCompletion.taskId(), taskCompletion.completed()))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() > 0);
    }
}
