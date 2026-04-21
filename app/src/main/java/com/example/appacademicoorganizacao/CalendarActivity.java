package com.example.appacademicoorganizacao;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    CalendarView calendarView;
    RecyclerView recyclerView;

    List<Task> allTasks;
    List<Task> filteredTasks;
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerTasksByDate);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTasks();

        filteredTasks = new ArrayList<>();
        adapter = new TaskAdapter(this, filteredTasks);
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%0d2/%d", dayOfMonth, month + 1, year);
            filterTasks(selectedDate);
        });
    }

    private void filterTasks(String date) {
        filteredTasks.clear();

        for (Task task : allTasks) {
            if (task.date.equals(date)) {
                filteredTasks.add(task);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        String json = prefs.getString("task_list", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        } else {
            allTasks = new ArrayList<>();
        }
    }
}
