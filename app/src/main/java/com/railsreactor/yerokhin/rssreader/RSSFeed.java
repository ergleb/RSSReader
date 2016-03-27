package com.railsreactor.yerokhin.rssreader;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hlib on 22.03.2016.
 */
public class RSSFeed implements Parcelable{
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    private String url;
    private String title;
    private String link;
    private String description;
    private LinkedList<RSSItem> rssItems;

    public RSSFeed(String url, String title, String link, String description, LinkedList<RSSItem> rssItems) {
        this.url = url;
        this.title = title;
        this.link = link;
        this.description = description;
        this.rssItems = rssItems;
    }

    private RSSFeed(Parcel parcel){
        String[] strings = new String[4];
        parcel.readStringArray(strings);
        url = strings[0];
        title = strings[1];
        link = strings[2];
        description = strings[3];
        rssItems = (LinkedList<RSSItem>)parcel.readSerializable();
    }

    public LinkedList<RSSItem> getRssItems() {

        return rssItems;
    }

    public void setRssItems(LinkedList<RSSItem> rssItems) {
        this.rssItems = rssItems;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{url,title,link,description});
        dest.writeSerializable(rssItems);
    }

    public final static Parcelable.Creator<RSSFeed> CREATOR = new Parcelable.Creator<RSSFeed>(){

        @Override
        public RSSFeed createFromParcel(Parcel source) {
            return new RSSFeed(source);
        }

        @Override
        public RSSFeed[] newArray(int size) {
            return new RSSFeed[size];
        }
    };
}
