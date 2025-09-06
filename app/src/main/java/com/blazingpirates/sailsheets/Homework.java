package com.blazingpirates.sailsheets;

public class Homework {

    private String subject;
    private String task;
    private String editedBy;
    private String editedTime;

    private boolean isCompleted = false;

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Homework(String subject, String task, String editedBy, String editedTime) {
        this.subject = subject;
        this.task = task;
        this.editedBy = editedBy;
        this.editedTime = editedTime;
    }

    public String getSubject() {
        return subject;
    }

    public String getTask() {
        return task;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public String getEditedTime() {
        return editedTime;
    }
}