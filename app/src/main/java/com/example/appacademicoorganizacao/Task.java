package com.example.appacademicoorganizacao;

public class Task {
    String title;
    boolean isDone;
    String date = "dd/MM/yyyy";

    public Task(String title, String date) {
        this.title = title;
        this.isDone = false;
        this.date = date;

    }
}
