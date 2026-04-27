package com.example.appacademicoorganizacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;

    private List<Task> allTasks;
    private List<Task> filteredTasks;

    private TaskAdapter adapter;

    private static final String PREF_NAME = "tasks";
    private static final String KEY_TASKS = "task_list";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerViewCalendar);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadTasks();
        setupDecorators();

        filteredTasks = new ArrayList<>();

        adapter = new TaskAdapter(filteredTasks, new TaskAdapter.TaskActionListener() {
            @Override
            public void onDelete(int position) {
                Task task = filteredTasks.get(position);
                allTasks.remove(task);
                filteredTasks.remove(position);
                adapter.notifyItemRemoved(position);
                saveTasks();
            }

            @Override
            public void onCheckChanged() {
                saveTasks();
            }
        });

        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {

            @SuppressLint("DefaultLocale") String selectedDate = String.format("%02d/%02d/%d",
                    date.getDay(),
                    date.getMonth() + 1,
                    date.getYear());

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
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TASKS, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            allTasks = gson.fromJson(json, type);
        } else {
            allTasks = new ArrayList<>();
        }
    }

    private void saveTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = gson.toJson(allTasks);

        prefs.edit().putString(KEY_TASKS, json).apply();
    }
    private void setupDecorators() {
        Set<CalendarDay> dates = new HashSet<>();

        for (Task task : allTasks) {
            try {
                String[] parts = task.date.split("/");

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);

                dates.add(CalendarDay.from(year, month, day));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        calendarView.removeDecorators(); // limpa antes (importante)
        calendarView.addDecorator(new EventDecorator(0xFFBB86FC, dates));
    }
}