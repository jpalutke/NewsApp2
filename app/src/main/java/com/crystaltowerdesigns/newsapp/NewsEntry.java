package com.crystaltowerdesigns.newsapp;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsEntry implements Parcelable {

    // method used by Parcelable creation
    public static final Creator<NewsEntry> CREATOR
            = new Creator<NewsEntry>() {
        public NewsEntry createFromParcel(Parcel in) {
            return new NewsEntry(in);
        }

        public NewsEntry[] newArray(int size) {
            return new NewsEntry[size];
        }
    };
    private final String id;
    private final String sectionName;
    private final String webURL;
    private final String webPublicationDate;
    private String webTitle;
    private String author = "";

    /**
     * {LINK NewsEntry}
     *
     * @param id                 String containing the news id
     * @param webTitle           String containing the news Title
     * @param sectionName        String containing the news Tag
     * @param webURL             String containing the news Tag
     * @param webPublicationDate String containing the news webPublicationDate
     */
    public NewsEntry(String id, String webTitle, String sectionName, String webURL, String webPublicationDate) {
        this.id = id;
        this.webTitle = webTitle;
        int pos = webTitle.lastIndexOf("|");
        if (pos > 0) {
            this.author = webTitle.substring(pos + 2);
            this.webTitle = webTitle.substring(0, pos - 1);
        }
        this.sectionName = sectionName;
        this.webURL = webURL;
        this.webPublicationDate = webPublicationDate;
    }

    // Parcelable READS, order must match WRITES
    private NewsEntry(Parcel in) {
        id = in.readString();
        webTitle = in.readString();
        sectionName = in.readString();
        webURL = in.readString();
        webPublicationDate = in.readString();
        author = in.readString();
    }

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    public String getWebTitle() {
        return webTitle;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getWebURL() {
        return webURL;
    }

    public String getWebPublicationDate() {
        return webPublicationDate;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable WRITES, order must match READS
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(webTitle);
        parcel.writeString(sectionName);
        parcel.writeString(webURL);
        parcel.writeString(webPublicationDate);
        parcel.writeString(author);
    }


}
