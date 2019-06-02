package com.helospark.tactview.ui.javafx.commands.impl;

import javax.annotation.Generated;

import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class GenericLambdaExecutorCommand implements UiCommand {
    private Runnable executeCommand;
    private Runnable revertCommand;

    @Generated("SparkTools")
    private GenericLambdaExecutorCommand(Builder builder) {
        this.executeCommand = builder.executeCommand;
        this.revertCommand = builder.revertCommand;
    }

    @Override
    public void execute() {
        executeCommand.run();
    }

    @Override
    public void revert() {
        revertCommand.run();
    }

    @Override
    public String toString() {
        return "GenericLambdaExecutorCommand [executeCommand=" + executeCommand + ", revertCommand=" + revertCommand + "]";
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private Runnable executeCommand;
        private Runnable revertCommand;

        private Builder() {
        }

        public Builder withExecuteCommand(Runnable executeCommand) {
            this.executeCommand = executeCommand;
            return this;
        }

        public Builder withRevertCommand(Runnable revertCommand) {
            this.revertCommand = revertCommand;
            return this;
        }

        public GenericLambdaExecutorCommand build() {
            return new GenericLambdaExecutorCommand(this);
        }
    }

}
