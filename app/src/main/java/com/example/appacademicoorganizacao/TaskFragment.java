package com.example.appacademicoorganizacao;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {

    EditText inputTask;
    Button btnAdd;
    RecyclerView recyclerView;

    List<Task> taskList;
    TaskAdapter adapter;

    private static final String PREF_NAME = "tasks";
    private static final String KEY_TASKS = "task_list";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task, container, false);

        inputTask = view.findViewById(R.id.inputTask);
        btnAdd = view.findViewById(R.id.btnAdd);
        recyclerView = view.findViewById(R.id.recyclerView);

        loadTasks();

        adapter = new TaskAdapter(getContext(), taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        //Adicionar tarefa
        btnAdd.setOnClickListener(v -> {
            String text = inputTask.getText().toString();

            if (!text.isEmpty()) {
                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                taskList.add(new Task(text, currentDate));
                adapter.notifyItemInserted(taskList.size() - 1);
                saveTasks();
                inputTask.setText("");
            }
        });

        //Swipe para deletar
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        taskList.remove(position);
                        adapter.notifyItemRemoved(position);
                        saveTasks();
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        return view;
    }

    //Salvar tarefas
    private void saveTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);

        editor.putString(KEY_TASKS, json);
        editor.apply();
    }

    //Carregar tarefas
    private void loadTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, getContext().MODE_PRIVATE);
        String json = prefs.getString(KEY_TASKS, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            taskList = gson.fromJson(json, type);
        } else {
            taskList = new ArrayList<>();
        }
    }
}