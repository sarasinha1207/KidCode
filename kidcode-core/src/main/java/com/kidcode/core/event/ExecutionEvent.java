package com.kidcode.core.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// A sealed interface restricts which other classes may implement it.
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExecutionEvent.MoveEvent.class, name = "MoveEvent"),
    @JsonSubTypes.Type(value = ExecutionEvent.SayEvent.class, name = "SayEvent"),
    @JsonSubTypes.Type(value = ExecutionEvent.ErrorEvent.class, name = "ErrorEvent"),
    @JsonSubTypes.Type(value = ExecutionEvent.ClearEvent.class, name = "ClearEvent")
})
public sealed interface ExecutionEvent {
    // We define specific event types as records that implement this interface.
    // This is a modern, clean way to handle different event types.

    record MoveEvent(
        int fromX, int fromY, int toX, int toY,
        double newDirection, boolean isPenDown, String color
    ) implements ExecutionEvent {}

    record SayEvent(String message) implements ExecutionEvent {}

    record ErrorEvent(String errorMessage) implements ExecutionEvent {}

    // A simple event to clear the screen at the start of execution.
    record ClearEvent() implements ExecutionEvent {}
} 