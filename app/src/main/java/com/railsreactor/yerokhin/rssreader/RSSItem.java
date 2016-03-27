package com.railsreactor.yerokhin.rssreader;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Hlib on 22.03.2016.
 */
public class RSSItem implements Parcelable,Serializable{
    private String title;
    private String link;
    private String description;

    public RSSItem(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }

    private RSSItem(Parcel parcel){
        String[] strings = new String[3];
        parcel.readStringArray(strings);
        title = strings[0];
        link = strings[1];
        description = strings[2];
    }
    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{title,link,description});
    }

    public static final Parcelable.Creator<RSSItem> CREATOR = new Parcelable.Creator<RSSItem>(){

        @Override
        public RSSItem createFromParcel(Parcel source) {
            return new RSSItem(source);
        }

        @Override
        public RSSItem[] newArray(int size) {
            return new RSSItem[size];
        }
    };

    @Override
    public String toString() {
        return title;
    }
}
