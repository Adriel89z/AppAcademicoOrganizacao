package com.example.appacademicoorganizacao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    public interface TaskActionListener {
        void onDelete(int position);
        void onCheckChanged();
    }

    private List<Task> taskList;
    private TaskActionListener listener;

    public TaskAdapter(List<Task> taskList, TaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkTask);
            textView = itemView.findViewById(R.id.textTask);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textView.setText(task.title);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone);

        holder.checkBox.setOnCheckedChangeListener((b, isChecked) -> {
            task.isDone = isChecked;
            listener.onCheckChanged();
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDelete(pos);
            }
        });
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
