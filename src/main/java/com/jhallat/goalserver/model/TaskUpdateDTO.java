package com.jhallat.goalserver.model;

public record TaskUpdateDTO(String description,
                            boolean isOngoing,
                            boolean isQuantifiable) {
}
