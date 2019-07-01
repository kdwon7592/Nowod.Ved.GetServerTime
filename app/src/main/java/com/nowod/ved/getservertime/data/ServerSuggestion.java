package com.nowod.ved.getservertime.data;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class ServerSuggestion implements SearchSuggestion {

    private String mServerName;
    private boolean mIsHistory = false;

    public ServerSuggestion(String suggestion) {
        this.mServerName = suggestion.toLowerCase();
    }

    public ServerSuggestion(Parcel source) {
        this.mServerName = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }

    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }

    @Override
    public String getBody() {
        return mServerName;
    }

    public static final Creator<ServerSuggestion> CREATOR = new Creator<ServerSuggestion>() {
        @Override
        public ServerSuggestion createFromParcel(Parcel in) {
            return new ServerSuggestion(in);
        }

        @Override
        public ServerSuggestion[] newArray(int size) {
            return new ServerSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mServerName);
        dest.writeInt(mIsHistory ? 1 : 0);
    }
}