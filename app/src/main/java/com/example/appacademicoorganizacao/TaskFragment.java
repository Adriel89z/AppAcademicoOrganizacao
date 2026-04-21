package com.example.appacademicoorganizacao;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskFragment extends Fragment {

    private EditText inputTask;
    private Button btnAdd;
    private RecyclerView recyclerView;

    private List<Task> taskList;
    private TaskAdapter adapter;

    private static final String PREF_NAME = "tasks";
    private static final String KEY_TASKS = "task_list";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task, container, false);

        inputTask = view.findViewById(R.id.inputTask);
        btnAdd = view.findViewById(R.id.btnAdd);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Enter = adicionar tarefa
        inputTask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnAdd.performClick();
                return true;
            }
            return false;
        });

        loadTasks();

        adapter = new TaskAdapter(requireContext(), taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Botão adicionar tarefa
        btnAdd.setOnClickListener(v -> {
            String text = inputTask.getText().toString().trim();

            if (text.isEmpty()) return;

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {

                        String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

                        taskList.add(new Task(text, selectedDate));
                        updateTasks();
                        recyclerView.scrollToPosition(taskList.size() - 1);

                        inputTask.setText("");
                        inputTask.clearFocus();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });

        return view;
    }

    // Atualiza lista + salva
    private void updateTasks() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        saveTasks();
    }

    // Salvar
    private void saveTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);

        editor.putString(KEY_TASKS, json);
        editor.apply();
    }

    // Carregar
    private void loadTasks() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = prefs.getString(KEY_TASKS, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            taskList = gson.fromJson(json, type);
        } else {
            taskList = new ArrayList<>();
        }
    }

    // Atualiza ao voltar para tela
    @Override
    public void onResume() {
        super.onResume();
        loadTasks();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}