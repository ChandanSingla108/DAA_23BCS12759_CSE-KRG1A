package com.shortestpath.ui;

import com.shortestpath.algorithm.AlgorithmResult;
import com.shortestpath.algorithm.AlgorithmStep;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class AnimationEngine {
    private AlgorithmResult algorithmResult;
    private AlgorithmState currentState;
    private final Timeline timeline;
    private final List<Consumer<AlgorithmStep>> stepListeners;
    private final List<Consumer<AlgorithmState>> stateListeners;
    private static final Duration BASE_STEP_DURATION = Duration.millis(800);

    public AnimationEngine() {
        this.algorithmResult = null;
        this.currentState = AlgorithmState.initial();
        this.stepListeners = new ArrayList<>();
        this.stateListeners = new ArrayList<>();
        this.timeline = new Timeline();
        this.timeline.setCycleCount(1);
        this.timeline.getKeyFrames().add(new KeyFrame(BASE_STEP_DURATION, e -> advanceToNextStep()));
        this.timeline.setRate(this.currentState.getPlaybackSpeed());
    }

    public void loadAlgorithm(AlgorithmResult result) {
        if (result == null || result.getStepCount() == 0) {
            throw new IllegalArgumentException("AlgorithmResult must be non-null with at least one step");
        }
        stop();
        this.algorithmResult = result;
        this.currentState = new AlgorithmState(0, result.getStepCount(), AlgorithmState.PlaybackStatus.STOPPED, currentState.getPlaybackSpeed());
        notifyStateListeners();
        notifyStepListeners(result.getSteps().get(0));
    }

    public boolean hasAlgorithm() {
        return algorithmResult != null;
    }

    public void play() {
        if (!hasAlgorithm()) return;
        if (currentState.isPlaying()) return;
        if (currentState.isAtEnd()) {
            currentState = new AlgorithmState(0, currentState.getTotalSteps(), AlgorithmState.PlaybackStatus.STOPPED, currentState.getPlaybackSpeed());
            notifyStateListeners();
            notifyStepListeners(algorithmResult.getSteps().get(0));
        }
        currentState = currentState.withStatus(AlgorithmState.PlaybackStatus.PLAYING);
        notifyStateListeners();
        timeline.playFromStart();
    }

    public void pause() {
        if (!currentState.isPlaying()) return;
        timeline.stop();
        currentState = currentState.withStatus(AlgorithmState.PlaybackStatus.PAUSED);
        notifyStateListeners();
    }

    public void stop() {
        timeline.stop();
        if (!hasAlgorithm()) {
            currentState = AlgorithmState.initial();
            notifyStateListeners();
            return;
        }
        currentState = new AlgorithmState(0, currentState.getTotalSteps(), AlgorithmState.PlaybackStatus.STOPPED, currentState.getPlaybackSpeed());
        notifyStateListeners();
        notifyStepListeners(algorithmResult.getSteps().get(0));
    }

    public void stepForward() {
        if (!hasAlgorithm() || currentState.isAtEnd()) return;
        if (currentState.isPlaying()) pause();
        int total = currentState.getTotalSteps();
        int newIndex = Math.min(currentState.getCurrentStepIndex() + 1, total - 1);
        currentState = new AlgorithmState(newIndex, total, AlgorithmState.PlaybackStatus.PAUSED, currentState.getPlaybackSpeed());
        notifyStateListeners();
        AlgorithmStep step = algorithmResult.getSteps().get(newIndex);
        notifyStepListeners(step);
    }

    public void stepBackward() {
        if (!hasAlgorithm() || currentState.isAtStart()) return;
        if (currentState.isPlaying()) pause();
        int newIndex = Math.max(currentState.getCurrentStepIndex() - 1, 0);
        currentState = new AlgorithmState(newIndex, currentState.getTotalSteps(), AlgorithmState.PlaybackStatus.PAUSED, currentState.getPlaybackSpeed());
        notifyStateListeners();
        AlgorithmStep step = algorithmResult.getSteps().get(newIndex);
        notifyStepListeners(step);
    }

    public void reset() {
        stop();
    }

    public void setSpeed(double speed) {
        if (speed <= 0.0 || speed > 5.0) {
            throw new IllegalArgumentException("speed must be in (0, 5.0]");
        }
        currentState = currentState.withSpeed(speed);
        timeline.setRate(speed);
        notifyStateListeners();
    }

    private void advanceToNextStep() {
        if (!currentState.isPlaying() || !hasAlgorithm()) return;
        int nextIndex = currentState.getCurrentStepIndex() + 1;
        int total = currentState.getTotalSteps();
        if (nextIndex >= total) {
            timeline.stop();
            currentState = new AlgorithmState(total - 1, total, AlgorithmState.PlaybackStatus.PAUSED, currentState.getPlaybackSpeed());
            notifyStateListeners();
            return;
        }
        currentState = new AlgorithmState(nextIndex, total, AlgorithmState.PlaybackStatus.PLAYING, currentState.getPlaybackSpeed());
        notifyStateListeners();
        AlgorithmStep step = algorithmResult.getSteps().get(nextIndex);
        notifyStepListeners(step);
        timeline.playFromStart();
    }

    private void notifyStepListeners(AlgorithmStep step) {
        for (Consumer<AlgorithmStep> l : new ArrayList<>(stepListeners)) {
            try { l.accept(step); } catch (Exception ignored) { }
        }
    }

    private void notifyStateListeners() {
        for (Consumer<AlgorithmState> l : new ArrayList<>(stateListeners)) {
            try { l.accept(currentState); } catch (Exception ignored) { }
        }
    }

    public AutoCloseable addStepListener(Consumer<AlgorithmStep> listener) {
        Objects.requireNonNull(listener, "listener");
        stepListeners.add(listener);
        return () -> stepListeners.remove(listener);
    }

    public AutoCloseable addStateListener(Consumer<AlgorithmState> listener) {
        Objects.requireNonNull(listener, "listener");
        stateListeners.add(listener);
        return () -> stateListeners.remove(listener);
    }

    public void removeStepListener(Consumer<AlgorithmStep> listener) {
        stepListeners.remove(listener);
    }

    public void removeStateListener(Consumer<AlgorithmState> listener) {
        stateListeners.remove(listener);
    }

    public AlgorithmState getCurrentState() { return currentState; }

    public AlgorithmStep getCurrentStep() {
        if (!hasAlgorithm()) return null;
        int idx = currentState.getCurrentStepIndex();
        if (idx < 0 || idx >= algorithmResult.getStepCount()) return null;
        return algorithmResult.getSteps().get(idx);
    }

    public void dispose() {
        timeline.stop();
        stepListeners.clear();
        stateListeners.clear();
        algorithmResult = null;
        currentState = AlgorithmState.initial();
    }
}
