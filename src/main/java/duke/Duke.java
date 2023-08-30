package duke;

import java.util.Scanner;

import duke.parser.Parser;

import duke.storage.Storage;

import duke.tasks.Deadline;
import duke.tasks.Event;
import duke.tasks.Task;
import duke.tasks.ToDo;

import duke.ui.Ui;

import duke.util.TaskList;

import duke.exceptions.DukeException;

/*
 * Duke is a personal assistant chatbot that helps a person to keep track of various things.
 */

public class Duke {
    
    public enum CommandType {
        LIST, MARK, DELETE, TODO, DEADLINE, EVENT, UNKNOWN
    }

    private Storage storage;
    private TaskList tasks;
    private Ui ui;
    private Parser parser;

    private Duke() {
        try {
            this.ui = new Ui();
            this.storage = new Storage();
            this.tasks = new TaskList(storage.readTasks());
            this.parser = new Parser();
        } catch (DukeException e) {
            ui.printErrorMessage(e);
        }
    }

    private void run() {
        ui.printWelcomeMessage();
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                String input = sc.nextLine();
                if (input.equals("bye")) {
                    break;
                }
                CommandType commandType = parser.parseCommandType(input);
                handleCommand(commandType, input);
            }
        } catch (DukeException e) {
            ui.printErrorMessage(e);
        } catch (Exception e) {
            ui.printErrorMessage(new DukeException("An unexpected error occurred: " + e.getMessage()));
        }
        ui.printFarewellMessage();
    }

    /*
     * Main entry point of the Duke application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Duke ekud = new Duke();
        ekud.run();
    }

    

    private void handleCommand(CommandType commandType, String command) throws DukeException {
        switch (commandType) {
        case LIST:
            ui.printList(tasks.getTasks());
            break;
        case MARK:
            markTask(command);
            break;
        case DELETE:
            deleteTask(command);
            break;
        case TODO:
        case DEADLINE:
        case EVENT:
            addTask(command);
            break;
        case UNKNOWN:
            ui.printErrorMessage(new DukeException("I'm sorry, but I don't know what that means :-("));
            break;
        }
    }

    private void addTask(String task) {
        try {
            Task newTask = null;
            if (task.startsWith("todo")) {
                newTask = ToDo.createToDoFromCommand(task);
            } else if (task.startsWith("deadline")) {
                newTask = Deadline.createDeadlineFromCommand(task);
            } else if (task.startsWith("event")) {
                newTask = Event.createEventFromCommand(task);
            }

            if (newTask != null) {
                tasks.add(newTask);
                storage.write(newTask);
                ui.printAddedTaskConfirmation(newTask, tasks);
            } 
        } catch (DukeException e) {
            ui.printErrorMessage(e);
        }
    }


    private void markTask(String command) {
        try {
            int index = Integer.parseInt(command.split(" ")[1]) - 1;
            Task task = tasks.get(index);
            task.markAsDone();
            storage.write(tasks.getTasks());
            ui.printMarkedTaskConfirmation(task);            
        } catch (NumberFormatException e) {
            ui.printErrorMessage(new DukeException("Invalid command format"));
        } catch (DukeException e) {
            ui.printErrorMessage(e);
        }
    }

    private void deleteTask(String command) {
        try {
            int index = Integer.parseInt(command.split(" ")[1]) - 1;
            Task task = tasks.remove(index);
            storage.write(tasks.getTasks());
            ui.printDeletedTaskConfirmation(task, tasks);    
        } catch (NumberFormatException e) {
            ui.printErrorMessage(new DukeException("Invalid command format"));
        } catch (DukeException e) {
            ui.printErrorMessage(e);
        } 
    }


}
