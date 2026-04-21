package com.example.appacademicoorganizacao;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    CalendarView calendarView;
    RecyclerView recyclerView;

    List<Task> allTasks;
    List<Task> filteredTasks;

    TaskAdapter adapter;

    private static final String PREF_NAME = "tasks";
    private static final String KEY_TASKS = "task_list";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerTasksByDate);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadTasks();

        filteredTasks = new ArrayList<>();
        adapter = new TaskAdapter(requireContext(), filteredTasks);
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
            filterTasks(selectedDate);
        });

        return view;
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
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, getContext().MODE_PRIVATE);
        String json = prefs.getString(KEY_TASKS, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            allTasks = gson.fromJson(json, type);
        } else {
            allTasks = new ArrayList<>();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }
}