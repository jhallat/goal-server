package com.jhallat.goalserver.model;

public record TaskCreationDTO(long goalId, String description, boolean isOngoing) {
}
