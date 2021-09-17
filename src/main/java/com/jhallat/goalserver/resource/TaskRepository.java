package com.jhallat.goalserver.resource;

import com.jhallat.goalserver.entity.Status;
import com.jhallat.goalserver.entity.Task;
import com.jhallat.goalserver.model.TaskCompletionDTO;
import com.jhallat.goalserver.model.TaskCreationDTO;
import com.jhallat.goalserver.model.TaskUpdateDTO;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TaskRepository {

    private static int STATUS_PENDING = 1;
    private static int STATUS_IN_PROGRESS = 2;
    private static int STATUS_COMPLETED = 3;

    private static final String SQL_FIND_BY_GOAL = """
                    SELECT tasks.id as task_id, 
                           goal_Id, 
                           tasks.description as task_description, 
                           status.id as status_id,
                           status.key as status_key,
                           status.description as status_description,
                           ongoing,
                           quantifiable,
                           notes 
                    FROM tasks
                    INNER JOIN status
                        ON tasks.status_id = status.id
                    WHERE goal_id = $1 
                    ORDER BY tasks.description ASC
                """;

    private static final String SQL_FIND_BY_ID = """
                    SELECT tasks.id as task_id, 
                           goal_Id, 
                           tasks.description as task_description, 
                           status.id as status_id,
                           status.key as status_key,
                           status.description as status_description,
                           ongoing,
                           quantifiable,
                           notes 
                    FROM tasks
                    INNER JOIN status
                        ON tasks.status_id = status.id
                    WHERE tasks.id = $1 
            """;

    private static final String SQL_INSERT = """
            INSERT INTO tasks(goal_id, description, status_id, ongoing, quantifiable, notes)
            VALUES ($1, $2, $3, $4, $5, $6) RETURNING id
            """;

    private static final String SQL_UPDATE_STATUS = """
            UPDATE tasks
            SET status_id = $2
            WHERE id = $1
            """;

    private static final String SQL_DELETE_TASK = """
            DELETE FROM tasks
            WHERE id = $1
            """;

    private static final String SQL_UPDATE_TASK = """
            UPDATE tasks
            SET description = $2,
                ongoing = $3,
                quantifiable = $4,
                notes = $5
            WHERE id = $1    
            """;

    @Inject
    PgPool client;

    public Multi<Task> findAllByGoal(long goalId) {
        return client.preparedQuery(SQL_FIND_BY_GOAL)
                .execute(Tuple.of(goalId))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> new Task(row.getLong("task_id"),
                                                    row.getLong("goal_id"),
                                                    row.getString("task_description"),
                                                    new Status(row.getInteger("status_id"),
                                                               row.getString("status_key"),
                                                               row.getString("status_description")),
                                                    row.getBoolean("ongoing"),
                                                    row.getBoolean("quantifiable"),
                                                    row.getString("notes")));
    }

    public Uni<Task> findById(long taskId) {
        return client.preparedQuery(SQL_FIND_BY_ID)
                .execute(Tuple.of(taskId))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? iterator.next() : null)
                .onItem().transform(row -> row == null ? null : new Task(row.getLong("task_id"),
                        row.getLong("goal_id"),
                        row.getString("task_description"),
                        new Status(row.getInteger("status_id"),
                                row.getString("status_key"),
                                row.getString("status_description")),
                        row.getBoolean("ongoing"),
                        row.getBoolean("quantifiable"),
                        row.getString("notes")));
    }

    public Uni<Task> insert(TaskCreationDTO task) {
        return client.preparedQuery(SQL_INSERT)
                .execute(Tuple.of(task.goalId(),
                         task.description(),
                         1,
                         task.isOngoing(),
                         task.isQuantifiable(),
                         task.notes()))
                .onItem().transform(rowSet -> {
                    var row = rowSet.iterator().next();
                    return new Task(row.getLong("id"),
                                    task.goalId(),
                                    task.description(),
                                    new Status(1, "PENDING", "Pending"),
                                    task.isOngoing(),
                                    task.isQuantifiable(),
                                    task.notes());
                });
    }

    public Uni<Boolean> updateStatus(long taskId, int status) {
        return client.preparedQuery(SQL_UPDATE_STATUS)
                .execute(Tuple.of(taskId, status))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() > 0);
    }

    public Uni<Boolean> updateTask(long taskId, TaskUpdateDTO update) {
        return client.preparedQuery(SQL_UPDATE_TASK)
                .execute(Tuple.of(taskId,
                        update.description(),
                        update.isOngoing(),
                        update.isQuantifiable(),
                        update.notes()))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() > 0);
    }

    public Uni<Boolean> deleteTask(long taskId) {
        return client.preparedQuery(SQL_DELETE_TASK)
                .execute(Tuple.of(taskId))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
