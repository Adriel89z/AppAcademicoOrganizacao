package com.example.appacademicoorganizacao;

import com.example.appacademicoorganizacao.MainActivity;
import com.google.gson.Gson;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    List<Task> taskList;
    Context context;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkTask);
            textView = itemView.findViewById(R.id.textTask);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textView.setText(task.title);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isDone = isChecked;
        });

        // 🔥 BOTÃO DE DELETE
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                taskList.remove(pos);
                notifyDataSetChanged();

                // salvar
                SharedPreferences prefs = context.getSharedPreferences("tasks", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Gson gson = new Gson();
                String json = gson.toJson(taskList);

                editor.putString("task_list", json);
                editor.apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
