package com.zybooks.weightlogger;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class WeightEntryAdapter extends BaseAdapter {

    private Context context;
    private List<DatabaseHelper.WeightEntry> weightEntries;
    private DatabaseHelper dbHelper;
    private WeightDataFragment fragment;

    public WeightEntryAdapter(Context context, List<DatabaseHelper.WeightEntry> weightEntries, DatabaseHelper dbHelper, WeightDataFragment fragment) {
        this.context = context;
        this.weightEntries = weightEntries;
        this.dbHelper = dbHelper;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return weightEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return weightEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.weight_grid_item, parent, false);

            holder = new ViewHolder();
            holder.dateTextView = convertView.findViewById(R.id.dateTextView);
            holder.weightTextView = convertView.findViewById(R.id.weightTextView);
            holder.editButton = convertView.findViewById(R.id.editButton);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DatabaseHelper.WeightEntry entry = weightEntries.get(position);

        // Set the data to the views
        holder.dateTextView.setText(entry.getDate());
        holder.weightTextView.setText(String.format(Locale.getDefault(), "%.1f lbs", entry.getWeight()));

        // Set edit button click listener
        holder.editButton.setOnClickListener(v -> {
            showEditDialog(entry, position);
        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            // Delete the entry from database
            boolean success = dbHelper.deleteWeightEntry(entry.getId());

            if (success) {
                // Remove from the list
                weightEntries.remove(position);
                // Notify the adapter
                notifyDataSetChanged();

                Toast.makeText(context, "Weight entry deleted", Toast.LENGTH_SHORT).show();

                // Update the profile fragment if it's visible
                MainActivity.updateProfileIfVisible();
            } else {
                Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    private void showEditDialog(DatabaseHelper.WeightEntry entry, int position) {
        // Create dialog view
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_weight, null);
        EditText dateEditText = dialogView.findViewById(R.id.editDateEditText);
        EditText weightEditText = dialogView.findViewById(R.id.editWeightEditText);

        // Pre-fill with current values
        dateEditText.setText(entry.getDate());
        weightEditText.setText(String.format(Locale.getDefault(), "%.1f", entry.getWeight()));

        // Create and show dialog
        new AlertDialog.Builder(context)
                .setTitle("Edit Weight Entry")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDate = dateEditText.getText().toString().trim();
                    String newWeightStr = weightEditText.getText().toString().trim();

                    if (newDate.isEmpty() || newWeightStr.isEmpty()) {
                        Toast.makeText(context, "Please enter both date and weight", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double newWeight = Double.parseDouble(newWeightStr);

                        // Update the entry in the database
                        boolean success = dbHelper.updateWeightEntry(entry.getId(), newDate, newWeight);

                        if (success) {
                            Toast.makeText(context, "Weight entry updated", Toast.LENGTH_SHORT).show();
                            // Refresh the data
                            fragment.loadWeightData();

                            // Update the profile fragment if it's visible
                            MainActivity.updateProfileIfVisible();
                        } else {
                            Toast.makeText(context, "Failed to update entry", Toast.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class ViewHolder {
        TextView dateTextView;
        TextView weightTextView;
        Button editButton;
        Button deleteButton;
    }
}