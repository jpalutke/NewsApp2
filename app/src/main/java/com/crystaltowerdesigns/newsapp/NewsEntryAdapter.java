package com.crystaltowerdesigns.newsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class NewsEntryAdapter extends RecyclerView.Adapter<NewsEntryAdapter.MyViewHolder> {

    private final OnItemClickListener listener;
    private final List<NewsEntry> newsEntryList;

    public NewsEntryAdapter(@SuppressWarnings("unused") Context context, List<NewsEntry> newsEntryList, OnItemClickListener listener) {
        this.newsEntryList = newsEntryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_entry_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(newsEntryList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return newsEntryList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(NewsEntry newsEntry);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView webTitle;
        final TextView sectionName;
        final TextView webPublicationDate;
        final TextView author;

        MyViewHolder(View view) {
            super(view);
            webTitle = view.findViewById(R.id.webTitle_textView);
            sectionName = view.findViewById(R.id.section_textView);
            webPublicationDate = view.findViewById(R.id.news_webPublicationDate_textView);
            author = view.findViewById(R.id.author_textView);
        }

        @SuppressLint("SetTextI18n")
        void bind(final NewsEntry newsEntry, final OnItemClickListener listener) {
            webTitle.setText(newsEntry.getWebTitle());
            sectionName.setText(newsEntry.getSectionName());
            webPublicationDate.setText(newsEntry.getWebPublicationDate().replace("T"," ").replace("Z",""));
            author.setText(newsEntry.getContributor());
            if (newsEntry.getContributor().equals(""))
                author.setVisibility(View.GONE);
            else
                author.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(newsEntry);
                }
            });
        }
    }
}



