package com.blazingpirates.sailsheets;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class HomeworkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION = 0;
    private static final int VIEW_TYPE_HOMEWORK = 1;

    private final List<Object> items;  // String for section title, Homework for task
    private OnHomeworkClickListener listener;

    private SharedPreferences prefs;

    public HomeworkAdapter(Context context, List<Homework> homeworkList) {
        this.prefs = context.getSharedPreferences("homework_prefs", Context.MODE_PRIVATE);
        items = new ArrayList<>();

        // Split works and noWorks
        List<Homework> works = new ArrayList<>();
        List<Homework> noWorks = new ArrayList<>();
        for (Homework hw : homeworkList) {
            String key = hw.getSubject() + "_" + hw.getTask();
            hw.setCompleted(prefs.getBoolean(key, false)); // 🧠 Load saved state
            if (hw.getTask() != null && !hw.getTask().equalsIgnoreCase("No work today!")) {
                works.add(hw);
            } else {
                noWorks.add(hw);
            }
        }

        if (!works.isEmpty()) {
            items.add("Works");
            items.addAll(works);
        }

        if (!noWorks.isEmpty()) {
            items.add("No Works");
            items.addAll(noWorks);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Homework ? VIEW_TYPE_HOMEWORK : VIEW_TYPE_SECTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SECTION) {
            TextView header = new TextView(parent.getContext());
            header.setPadding(32, 48, 32, 24);
            header.setTextSize(22);
            header.setTextColor(Color.BLACK);
            return new SectionViewHolder(header);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homework_card, parent, false);
            return new HomeworkViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_SECTION) {
            ((SectionViewHolder) holder).sectionTitle.setText((String) items.get(position));
        } else {
            Homework hw = (Homework) items.get(position);
            HomeworkViewHolder hwHolder = (HomeworkViewHolder) holder;

            hwHolder.subjectName.setText(hw.getSubject());
            hwHolder.task.setText(hw.getTask());

            boolean hasWork = hw.getTask() != null && !hw.getTask().equalsIgnoreCase("No work today!");

            hwHolder.itemView.setBackgroundColor(hasWork ? Color.parseColor("#AF4B2214") : Color.WHITE);
            hwHolder.subjectName.setTextColor(hasWork ? Color.WHITE : Color.BLACK);
            hwHolder.task.setTextColor(hasWork ? Color.LTGRAY : Color.DKGRAY);

            hwHolder.checkBox.setOnCheckedChangeListener(null); // Detach first
            hwHolder.checkBox.setChecked(hw.isCompleted());
            hwHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                hw.setCompleted(isChecked);
                String key = hw.getSubject() + "_" + hw.getTask();
                prefs.edit().putBoolean(key, isChecked).apply();
            });

            hwHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onHomeworkClick(position, hw);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnHomeworkClickListener(OnHomeworkClickListener listener) {
        this.listener = listener;
    }

    public static class HomeworkViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName, task;
        CheckBox checkBox;

        public HomeworkViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.subjectName);
            task = itemView.findViewById(R.id.task);
            checkBox = itemView.findViewById(R.id.checkBox); // ✅ Make sure your item_homework_card layout has this ID
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;

        public SectionViewHolder(@NonNull TextView itemView) {
            super(itemView);
            this.sectionTitle = itemView;
        }
    }

    public interface OnHomeworkClickListener {
        void onHomeworkClick(int position, Homework hw);
    }

    public void updateData(List<Homework> newHomeworkList) {
        items.clear();
        List<Homework> works = new ArrayList<>();
        List<Homework> noWorks = new ArrayList<>();

        for (Homework hw : newHomeworkList) {
            String key = hw.getSubject() + "_" + hw.getTask();
            hw.setCompleted(prefs.getBoolean(key, false));
            if (hw.getTask() != null && !hw.getTask().equalsIgnoreCase("No work today!")) {
                works.add(hw);
            } else {
                noWorks.add(hw);
            }
        }

        if (!works.isEmpty()) {
            items.add("Works");
            items.addAll(works);
        }

        if (!noWorks.isEmpty()) {
            items.add("No Works");
            items.addAll(noWorks);
        }

        notifyDataSetChanged();
    }

}