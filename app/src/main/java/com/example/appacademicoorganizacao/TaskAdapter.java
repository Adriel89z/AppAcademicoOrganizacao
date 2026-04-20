package com.example.appacademicoorganizacao;

import com.example.appacademicoorganizacao.MainActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkTask);
            textView = itemView.findViewById(R.id.textTask);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textView.setText(task.title);
        holder.checkBox.setChecked(task.isDone);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isDone = isChecked;

        });
    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
