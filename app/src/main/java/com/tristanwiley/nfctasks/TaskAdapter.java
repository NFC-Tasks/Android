package com.tristanwiley.nfctasks;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Adapter to display tasks to the user.
 *
 * Created by adammcneilly on 2/20/16.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{
    private Context mContext;
    private List<Task> mTasks;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.mContext = context;
        this.mTasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bindTask(mTasks.get(position));
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public void remove(int position) {
        mTasks.remove(position);
        notifyItemRemoved(position);
    }

    public void swap(int firstPosition, int secondPosition){
        Collections.swap(mTasks, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView mTaskName;

        public TaskViewHolder(View view) {
            super(view);
            mTaskName = (TextView) view.findViewById(R.id.task_name);
        }

        public void bindTask(Task task) {
            mTaskName.setText(task.toString());
        }
    }
}
