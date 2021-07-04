package com.jhallat.goalserver.entity;

public record Task(long id,
                   long goalId,
                   String description,
                   Status status,
                   boolean isOngoing,
                   boolean isQuantifiable) {}
