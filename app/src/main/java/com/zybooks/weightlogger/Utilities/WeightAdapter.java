package com.zybooks.weightlogger.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zybooks.weightlogger.Data.WeightDatabaseHelper;
import com.zybooks.weightlogger.R;

import java.util.List;
import java.util.Locale;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {
    private final Context context;
    private List<WeightDatabaseHelper.WeightEntry> weightEntries;

    public interface OnWeightEntryActionListener {
        void onEditClick(WeightDatabaseHelper.WeightEntry entry, int position);
        void onDeleteClick(WeightDatabaseHelper.WeightEntry entry, int position);
    }

    private OnWeightEntryActionListener listener;

    public WeightAdapter(Context context, List<WeightDatabaseHelper.WeightEntry> weightEntries) {
        this.context = context;
        this.weightEntries = weightEntries;
    }

    public void setOnWeightEntryActionListener(OnWeightEntryActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weight_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightDatabaseHelper.WeightEntry entry = weightEntries.get(position);

        // Set the date and weight (basic information)
        holder.dateTextView.setText(entry.getDate());
        holder.weightTextView.setText(String.format(Locale.getDefault(), "%.1f lbs", entry.getWeight()));

        // Calculate and format weight change only when needed (not for every list item update)
        if (position < weightEntries.size() - 1) {
            WeightDatabaseHelper.WeightEntry nextEntry = weightEntries.get(position + 1);
            double change = entry.getWeight() - nextEntry.getWeight();
            String changeText = String.format(Locale.getDefault(), "%+.1f lbs from last entry", change);

            // Cache text colors (these should be moved to class-level constants)
            final int successColor = ContextCompat.getColor(context, R.color.success);
            final int errorColor = ContextCompat.getColor(context, R.color.error);
            final int neutralColor = ContextCompat.getColor(context, R.color.text_secondary_light);

            // Set text color based on change direction
            int textColor;
            if (change < 0) {
                textColor = successColor; // Weight loss
            } else if (change > 0) {
                textColor = errorColor; // Weight gain
            } else {
                textColor = neutralColor; // No change
            }

            holder.changeTextView.setText(changeText);
            holder.changeTextView.setTextColor(textColor);
            holder.changeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.changeTextView.setVisibility(View.GONE);
        }

        // Use a single OnClickListener instance for all items instead of creating new ones per item
        if (holder.editButton.getTag() == null) {
            holder.editButton.setTag(true); // Mark as initialized
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onEditClick(weightEntries.get(adapterPosition), adapterPosition);
                    }
                }
            });
        }

        if (holder.deleteButton.getTag() == null) {
            holder.deleteButton.setTag(true); // Mark as initialized
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(weightEntries.get(adapterPosition), adapterPosition);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return weightEntries == null ? 0 : weightEntries.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<WeightDatabaseHelper.WeightEntry> newEntries) {
        this.weightEntries = newEntries;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView weightTextView;
        public TextView changeTextView;
        public Button editButton;
        public Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            changeTextView = itemView.findViewById(R.id.changeTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}