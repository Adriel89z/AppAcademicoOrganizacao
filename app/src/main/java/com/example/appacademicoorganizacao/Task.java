package com.example.appacademicoorganizacao;
public class Task {
    public String title;
    public String date;
    public boolean isDone;

    public Task(String title, String date) {
        this.title = title;
        this.date = date;
        this.isDone = false;
    }
}