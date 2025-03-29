package controller;

import controller.command.Command;

/**
 * Interface for command factory that creates and manages commands.
 */
public interface ICommandFactory {
    /**
     * Checks if a command exists.
     *
     * @param commandName the name of the command
     * @return true if the command exists, false otherwise
     */
    boolean hasCommand(String commandName);

    /**
     * Gets a command by name.
     *
     * @param commandName the name of the command
     * @return the command, or null if not found
     */
    Command getCommand(String commandName);
}