package com.zybooks.weightlogger.Utilities;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zybooks.weightlogger.Data.WeightDatabaseHelper;
import com.zybooks.weightlogger.Data.WeightRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for calculating and maintaining user statistics.
 * Separates statistics logic from the ViewModel to reduce complexity.
 */
public class UserStatisticsHelper {
    private final WeightRepository weightRepository;

    // LiveData objects for statistics
    private final MutableLiveData<String> totalEntriesLiveData = new MutableLiveData<>("--");
    private final MutableLiveData<String> weightLostLiveData = new MutableLiveData<>("--");
    private final MutableLiveData<String> daysTrackingLiveData = new MutableLiveData<>("--");
    private final MutableLiveData<String> weeklyAvgLiveData = new MutableLiveData<>("--");

    /**
     * Creates a new UserStatisticsHelper instance.
     *
     * @param context The context used to access repositories
     */
    public UserStatisticsHelper(Context context) {
        weightRepository = new WeightRepository(context);
    }

    /**
     * Gets the LiveData for total entries count.
     * @return LiveData containing the total entries count
     */
    public LiveData<String> getTotalEntriesLiveData() {
        return totalEntriesLiveData;
    }

    /**
     * Gets the LiveData for weight lost/gained.
     * @return LiveData containing the weight change
     */
    public LiveData<String> getWeightLostLiveData() {
        return weightLostLiveData;
    }

    /**
     * Gets the LiveData for days tracking.
     * @return LiveData containing the tracking duration
     */
    public LiveData<String> getDaysTrackingLiveData() {
        return daysTrackingLiveData;
    }

    /**
     * Gets the LiveData for weekly average.
     * @return LiveData containing the weekly average change
     */
    public LiveData<String> getWeeklyAvgLiveData() {
        return weeklyAvgLiveData;
    }

    /**
     * Calculates all user statistics and updates LiveData.
     *
     * @param userId The ID of the user
     */
    public void calculateStatistics(int userId) {
        if (userId == -1) {
            resetStatistics();
            return;
        }

        List<WeightDatabaseHelper.WeightEntry> entries = weightRepository.getWeightEntries(userId);

        if (entries.isEmpty()) {
            resetStatistics();
            return;
        }

        // Total entries
        int totalEntries = entries.size();
        totalEntriesLiveData.setValue(String.valueOf(totalEntries));

        // Calculate weight lost/gained (first entry vs. most recent)
        if (entries.size() >= 2) {
            // First entry is most recent (they're sorted in reverse chronological order)
            double currentWeight = entries.get(0).getWeight();
            double initialWeight = entries.get(entries.size() - 1).getWeight();
            double weightDiff = initialWeight - currentWeight;

            String weightChangeText = String.format(Locale.getDefault(),
                    "%.1f lbs", Math.abs(weightDiff));
            weightLostLiveData.setValue(weightChangeText);
        }

        // Days tracking
        if (entries.size() >= 2) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date firstDate = dateFormat.parse(entries.get(entries.size() - 1).getDate());
                Date lastDate = dateFormat.parse(entries.get(0).getDate());

                if (firstDate != null && lastDate != null) {
                    long diffInMillis = Math.abs(lastDate.getTime() - firstDate.getTime());
                    long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                    daysTrackingLiveData.setValue(diffInDays + " days");
                }
            } catch (ParseException e) {
                daysTrackingLiveData.setValue("--");
            }
        }

        // Weekly average
        if (entries.size() >= 2) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date firstDate = dateFormat.parse(entries.get(entries.size() - 1).getDate());
                Date lastDate = dateFormat.parse(entries.get(0).getDate());

                if (firstDate != null && lastDate != null) {
                    double firstWeight = entries.get(entries.size() - 1).getWeight();
                    double lastWeight = entries.get(0).getWeight();
                    double weightDiff = lastWeight - firstWeight;

                    long diffInMillis = Math.abs(lastDate.getTime() - firstDate.getTime());
                    double diffInWeeks = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) / 7.0;

                    if (diffInWeeks > 0) {
                        double weeklyAvg = weightDiff / diffInWeeks;
                        String avgText = String.format(Locale.getDefault(), "%.1f lbs/week", weeklyAvg);
                        weeklyAvgLiveData.setValue(avgText);
                    }
                }
            } catch (ParseException e) {
                weeklyAvgLiveData.setValue("--");
            }
        }
    }

    /**
     * Resets all statistics to default values.
     */
    private void resetStatistics() {
        totalEntriesLiveData.setValue("--");
        weightLostLiveData.setValue("--");
        daysTrackingLiveData.setValue("--");
        weeklyAvgLiveData.setValue("--");
    }
}