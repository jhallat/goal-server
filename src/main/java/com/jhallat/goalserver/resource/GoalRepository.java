package com.jhallat.goalserver.resource;

import com.jhallat.goalserver.entity.Goal;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GoalRepository {

    @Inject
    PgPool client;


    public Multi<Goal> findAll() {
        return client.query("SELECT id, description FROM goals ORDER BY description ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(row -> new Goal(row.getLong("id"), row.getString("description")));
    }

    public Uni<Goal> insert(Goal goal) {
        return client.preparedQuery("INSERT INTO goals (description) VALUES ($1) RETURNING id, description")
                .execute(Tuple.of(goal.description()))
                .onItem().transform(rowSet -> {
                    var row = rowSet.iterator().next();
                    return new Goal(row.getLong("id"), row.getString("description"));
                });
    }

    public void update(Goal goal) {
        client.preparedQuery("UPDATE goals SET description = $1 WHERE id = $2")
                .execute(Tuple.of(goal.description(), goal.id()));
    }

    public Uni<Boolean> delete(long id) {
        return client.preparedQuery("DELETE FROM goals WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
