package com.shortestpath.ui;

import java.util.Objects;

public final class AlgorithmState {
    public enum PlaybackStatus { STOPPED, PLAYING, PAUSED }

    private final int currentStepIndex;
    private final int totalSteps;
    private final PlaybackStatus playbackStatus;
    private final double playbackSpeed;

    public AlgorithmState(int currentStepIndex, int totalSteps, PlaybackStatus status, double speed) {
        if (totalSteps < 0) {
            throw new IllegalArgumentException("totalSteps must be >= 0");
        }
        Objects.requireNonNull(status, "status");
        if (speed <= 0.0) {
            throw new IllegalArgumentException("speed must be > 0.0");
        }
        if (totalSteps == 0) {
            if (currentStepIndex != -1) {
                throw new IllegalArgumentException("currentStepIndex must be -1 when totalSteps is 0");
            }
        } else {
            if (currentStepIndex < 0 || currentStepIndex >= totalSteps) {
                throw new IllegalArgumentException("currentStepIndex out of range");
            }
        }
        this.currentStepIndex = currentStepIndex;
        this.totalSteps = totalSteps;
        this.playbackStatus = status;
        this.playbackSpeed = speed;
    }

    public static AlgorithmState initial() {
        return new AlgorithmState(-1, 0, PlaybackStatus.STOPPED, 1.0);
    }

    public AlgorithmState withStepIndex(int index) {
        return new AlgorithmState(index, this.totalSteps, this.playbackStatus, this.playbackSpeed);
    }

    public AlgorithmState withStatus(PlaybackStatus status) {
        return new AlgorithmState(this.currentStepIndex, this.totalSteps, status, this.playbackSpeed);
    }

    public AlgorithmState withSpeed(double speed) {
        return new AlgorithmState(this.currentStepIndex, this.totalSteps, this.playbackStatus, speed);
    }

    public int getCurrentStepIndex() { return currentStepIndex; }
    public int getTotalSteps() { return totalSteps; }
    public PlaybackStatus getPlaybackStatus() { return playbackStatus; }
    public double getPlaybackSpeed() { return playbackSpeed; }

    public boolean hasAlgorithm() { return totalSteps > 0; }
    public boolean isAtStart() { return hasAlgorithm() && currentStepIndex == 0; }
    public boolean isAtEnd() { return hasAlgorithm() && currentStepIndex == totalSteps - 1; }
    public boolean canStepForward() { return hasAlgorithm() && !isAtEnd(); }
    public boolean canStepBackward() { return hasAlgorithm() && currentStepIndex > 0; }
    public boolean isPlaying() { return playbackStatus == PlaybackStatus.PLAYING; }
    public boolean isPaused() { return playbackStatus == PlaybackStatus.PAUSED; }
    public boolean isStopped() { return playbackStatus == PlaybackStatus.STOPPED; }

    @Override
    public String toString() {
        return "AlgorithmState[step=" + (hasAlgorithm() ? (currentStepIndex + 1) : 0) + "/" + totalSteps +
                ", status=" + playbackStatus + ", speed=" + playbackSpeed + "]";
    }
}
