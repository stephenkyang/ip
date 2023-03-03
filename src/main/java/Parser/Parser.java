package Parser;

import Exceptions.DukeException;
import TaskList.TaskList;
import Tasks.Deadline;
import Tasks.Event;
import Tasks.Task;
import UI.UserInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    public Parser() {

    }
    public static void readCommand(UserInterface ui, TaskList tasks) throws DukeException {
        ArrayList<Task> list = tasks.getList();
        Scanner inputScanner = ui.getScanner();
        while (true) {
            String nextLine = inputScanner.nextLine();
            if (nextLine.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                dukeCommandList(list);
            } else if (nextLine.split(" ", 0)[0].equals("mark")) {
                dukeCommandMark(nextLine, list);
            } else if (nextLine.split(" ", 0)[0].equals("find")) {
                dukeCommandFind(nextLine, list);
            } else if (nextLine.split(" ", 0)[0].equals("deadline")) {
                dukeCommandDeadline(nextLine, list);
            } else if (nextLine.split(" ", 0)[0].equals("todo")) {
                dukeCommandToDo(nextLine, list);
            } else if (nextLine.split(" ", 0)[0].equals("delete")) {
                dukeCommandDelete(nextLine, list);
            } else if (nextLine.split(" ", 0)[0].equals("event")) {
                dukeCommandEvent(nextLine, list);
            } else if (nextLine.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                dukeSaveList(list);
                return;
            } else {
                System.out.println("Command does not exist");
                continue;
            }
        }
    }
    private static void dukeCommandFind(String nextLine, ArrayList<Task> list) throws DukeException {
        String[] inputArray = nextLine.split(" ", 0);
        if (inputArray.length != 2) {
            throw new DukeException("Try to remove all spaces in your keyword.");
        }
        String key = inputArray[1];
        ArrayList<Task> containsSubstring = new ArrayList<Task>();
        for (Task task: list) {
            String taskName = task.getTaskName();
            if (taskName.contains(key)) {
                containsSubstring.add(task);
            }
        }
        if (containsSubstring.isEmpty()) {
            System.out.println("No task names found with " + key);
            return;
        }
        System.out.println("Here are the following Tasks that have names containing your keyword:");
        dukeCommandList(containsSubstring);
    }
    private static void dukeSaveList(ArrayList<Task> list) throws DukeException {
        String home = System.getProperty("user.dir");
        java.nio.file.Path path = java.nio.file.Paths.get(home, "src", "main", "savefile");
        boolean directoryExists = java.nio.file.Files.exists(path);
        if (!directoryExists) {
            // from https://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new DukeException("Unable to create filepath");
            }
        }
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            throw new DukeException("Unable to delete and then recreate file with given filepath");
        }
        for (Task task: list) {
            String taskType = String.valueOf(task.getClass());
            ArrayList<String> attributesToStore = new ArrayList<String>();
            String taskInLineForm = "";

            switch (taskType) {
                case ("class Tasks.Task"):
                    attributesToStore.add(taskType);
                    attributesToStore.add(task.getTaskName());
                    attributesToStore.add(String.valueOf(task.isDone()));
                    taskInLineForm = deliminterAdder(attributesToStore);
                    break;
                case ("class Tasks.Deadline"):
                    attributesToStore.add(taskType);
                    attributesToStore.add(task.getTaskName());
                    attributesToStore.add(String.valueOf(task.isDone()));
                    attributesToStore.add(((Deadline) task).getDeadline());
                    taskInLineForm = deliminterAdder(attributesToStore);
                    break;
                case ("class Tasks.Event"):
                    attributesToStore.add(taskType);
                    attributesToStore.add(task.getTaskName());
                    attributesToStore.add(String.valueOf(task.isDone()));
                    attributesToStore.add(((Event) task).getDeadline());
                    attributesToStore.add(((Event) task).getStart());
                    taskInLineForm = deliminterAdder(attributesToStore);
                default:
                    break;
            }
            taskInLineForm = taskInLineForm.concat("\n");
//            System.out.println(taskInLineForm);
            try {
                Files.write(path, taskInLineForm.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new DukeException("Unable to write the task to file, given the filepath");
            }
        }
    }

    private static String deliminterAdder(ArrayList<String> items) {
        String taskInStringForm = "";
        for (String item : items) {
            taskInStringForm = taskInStringForm.concat(item + "|");
        }
//        System.out.println(taskInStringForm);
        return taskInStringForm;
    }


    private static void dukeCommandDelete(String nextLine, List<Task> list) throws DukeException {
        String[] inputArray = nextLine.split(" ", 0);
        if (inputArray.length != 2) {
            throw new DukeException("Please just input a number after delete.");
        }
        String indexString = inputArray[1];
        int index;
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException e) {
            throw new DukeException("The index " + indexString +  " cannot be interpreted as an int.");
        }
        try {
            list.remove(index);
        } catch (IndexOutOfBoundsException e) {
            throw new DukeException("Index out of bounds, try a smaller number than " + index);
        }
        System.out.println("Removed task at " + index);
    }

    private static void dukeCommandList (List<Task> list) {
        for (int i = 0; i < list.size(); i++ ) {
            Task task = list.get(i);
            if (task instanceof Event) {
                printEvent((Event) task, i);
            } else if (task instanceof Deadline) {
                printDeadline((Deadline) task, i);
            } else {
                printTodo(task, i);
            }
        }
    }

    // Helper functions for dukeCommandList
    private static void printEvent(Event event, int index) {
        if (event.isDone()) {
            System.out.println(index + ". [E][X] " + event.getTaskName() +
                    " (from: " + event.getStart() + " to: " + event.getDeadline() + ")");
        } else {
            System.out.println(index + ". [E][ ] " + event.getTaskName() +
                    " (from: " + event.getStart() + " to: " + event.getDeadline() + ")");
        }
    }
    private static void printDeadline(Deadline deadline, int index) {
        if (deadline.isDone()) {
            System.out.println(index + ". [D][X] " + deadline.getTaskName() +
                    " (by: " + deadline.getDeadline() + ")");
        } else {
            System.out.println(index + ". [D][ ] " + deadline.getTaskName() +
                    " (by: " + deadline.getDeadline() + ")");
        }
    }

    private static void printTodo(Task task, int index) {
        if (task.isDone()) {
            System.out.println(index + ". [T][X] " + task.getTaskName());
        } else {
            System.out.println(index + ". [T][ ] " + task.getTaskName());
        }
    }

    private static void dukeCommandMark(String nextLine, List<Task> list) throws DukeException {
        String[] inputArray = nextLine.split(" ", 0);
        if (inputArray.length != 2) {
            throw new DukeException("Try to only put a number after 'mark'");
        }
        int index;
        try {
            index = Integer.parseInt(inputArray[1]);
        } catch (NumberFormatException e) {
            throw new DukeException("The index " + inputArray[1] +  " cannot be interpreted as an int.");
        }
        if (index > list.size() - 1) {
            return;
        }
        Task task = list.get(index);
        if (!task.isDone()) {
            task.doTask();
            System.out.println("Task marked as complete!");
            return;
        }
        System.out.println("Task already marked as complete!");
    }

    private static void dukeCommandEvent(String nextLine, List<Task> list) throws DukeException {
        String lineWithoutCommand;
        try {
            lineWithoutCommand = nextLine.split(" ", 2)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DukeException("Incorrect number of arguments. Please do not use '/' or '|' in your arguments");
        }
        String[] regexOutput = lineWithoutCommand.split("/", 0);
        if (regexOutput.length != 3) {
            throw new DukeException("Incorrect number of arguments. Please write the name of the task and then '/[start] /[deadline]'. Please do not use '/' or '|' in your arguments");
        }
        String eventName = regexOutput[0];
        String startDate = regexOutput[1];
        String endDate = regexOutput[2];
        Deadline deadline = new Event(eventName, false, list.size(), startDate, endDate);
        list.add(deadline);
        System.out.println("event added");
    }
    private static void dukeCommandDeadline(String nextLine, List<Task> list) throws DukeException {
//        System.out.println(nextLine);
        String lineWithoutCommand;
        try {
            lineWithoutCommand = nextLine.split(" ", 2)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DukeException("Incorrect number of arguments. Please do not use '/' or '|' in your arguments");
        }
        String[] regexOutput = lineWithoutCommand.split("/", 0);
        if (regexOutput.length != 2) {
            throw new DukeException("Incorrect number of arguments. Please write the name of the task and then '/[deadline]' Please do not use '/' or '|' in your arguments");
        }
        String deadlineName = regexOutput[0];
        String deadlineDate = regexOutput[1];
        Deadline deadline = new Deadline(deadlineName, false, list.size(), deadlineDate);
        list.add(deadline);
        System.out.println("deadline added");
    }
    private static void dukeCommandToDo(String nextLine, List<Task> list) throws DukeException {
        String lineWithoutCommand;
        try {
            lineWithoutCommand = nextLine.split(" ", 2)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DukeException("Incorrect number of arguments. Please just wrte the task name.list Please do not use '/' or '|' in your arguments");
        }
        Task task = new Task(lineWithoutCommand, list.size());
        list.add(task);
        System.out.println("task/todo added");
    }
}
