package com.example.jadwalin.ui.kalender;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jadwalin.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<String> days;
    private final Calendar calendarInstance;
    private final String selectedDate;
    private final HashSet<String> taskDates;
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(String date);
    }

    public CalendarAdapter(ArrayList<String> days, Calendar calendarInstance, String selectedDate, HashSet<String> taskDates, OnDateClickListener listener) {
        this.days = days;
        this.calendarInstance = calendarInstance;
        this.selectedDate = selectedDate;
        this.taskDates = taskDates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = days.get(position);
        holder.tvDayNumber.setText(dayText);
        if (dayText.isEmpty()) {
            holder.viewTaskDot.setVisibility(View.INVISIBLE);
            holder.frameDayBackground.setBackgroundResource(android.R.color.transparent);
            return;
        }

        // Susun string tanggal item saat ini
        Calendar tempCal = (Calendar) calendarInstance.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayText));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String itemDateStr = sdf.format(tempCal.getTime());
        String todayStr = sdf.format(Calendar.getInstance().getTime());

        // 1. Kondisi Logika Visual Hari Ini - menggunakan Color.parseColor()
        if (itemDateStr.equals(todayStr)) {
            holder.frameDayBackground.setBackgroundResource(R.drawable.bg_today_border);
            holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#401F00"));
        } else {
            holder.frameDayBackground.setBackgroundResource(android.R.color.transparent);
            holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#401F00"));
        }

        // 2. Kondisi Logika Visual Tanggal Diklik
        if (itemDateStr.equals(selectedDate)) {
            holder.frameDayBackground.setBackgroundResource(R.drawable.bg_selected_day);
            holder.tvDayNumber.setTextColor(android.graphics.Color.WHITE);
        }

        // 3. Kondisi Tambahan Titik Penanda Tugas
        if (taskDates.contains(itemDateStr)) {
            holder.viewTaskDot.setVisibility(View.VISIBLE);
        } else {
            holder.viewTaskDot.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onDateClick(itemDateStr));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        FrameLayout frameDayBackground;
        View viewTaskDot;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            frameDayBackground = itemView.findViewById(R.id.frameDayBackground);
            viewTaskDot = itemView.findViewById(R.id.viewTaskDot);
        }
    }
}
