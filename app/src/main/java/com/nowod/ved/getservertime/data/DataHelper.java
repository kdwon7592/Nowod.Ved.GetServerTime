package com.nowod.ved.getservertime.data;

import android.content.Context;
import android.widget.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataHelper {

    private static final String COLORS_FILE_NAME = "colors.json";

    public static List<ServerSuggestion> sServerSuggestions = new ArrayList<>();

    public interface OnFindSuggestionsListener {
        void onResults(List<ServerSuggestion> results);
    }

    public static List<ServerSuggestion> getHistory(Context context, int count) {

        List<ServerSuggestion> suggestionList = new ArrayList<>();
        ServerSuggestion serverSuggestion;
        for (int i = 0; i < sServerSuggestions.size(); i++) {
            serverSuggestion = sServerSuggestions.get(i);
            serverSuggestion.setIsHistory(true);
            suggestionList.add(serverSuggestion);
            if (suggestionList.size() == count) {
                break;
            }
        }
        return suggestionList;
    }

    public static void resetSuggestionsHistory() {
        for (ServerSuggestion colorSuggestion : sServerSuggestions) {
            colorSuggestion.setIsHistory(false);
        }
    }

    public static void findSuggestions(Context context, String query, final int limit, final long simulatedDelay,
                                       final OnFindSuggestionsListener listener) {
        new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                try {
                    Thread.sleep(simulatedDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DataHelper.resetSuggestionsHistory();
                List<ServerSuggestion> suggestionList = new ArrayList<>();
                if (!(constraint == null || constraint.length() == 0)) {

                    for (ServerSuggestion suggestion : sServerSuggestions) {
                        if (suggestion.getBody().toUpperCase()
                                .startsWith(constraint.toString().toUpperCase())) {

                            suggestionList.add(suggestion);
                            if (limit != -1 && suggestionList.size() == limit) {
                                break;
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();
                Collections.sort(suggestionList, new Comparator<ServerSuggestion>() {
                    @Override
                    public int compare(ServerSuggestion lhs, ServerSuggestion rhs) {
                        return lhs.getIsHistory() ? -1 : 0;
                    }
                });
                results.values = suggestionList;
                results.count = suggestionList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (listener != null) {
                    listener.onResults((List<ServerSuggestion>) results.values);
                }
            }
        }.filter(query);

    }





    private static String loadJson(Context context) {

        String jsonString;

        try {
            InputStream is = context.getAssets().open(COLORS_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return jsonString;
    }

}