package com.railsreactor.yerokhin.rssreader;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;

public class ItemsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final RSSFeed feed = getIntent().getParcelableExtra("RSSFeed");//getting parcelable from intent

        getSupportActionBar().setTitle(feed.getTitle());

        ListView listView = (ListView) findViewById(R.id.items_listview);

        final LinkedList<RSSItem> rssItems = feed.getRssItems();

        ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(this,R.layout.rss_feed_view,rssItems){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {//overiding for displaying two strings
                if(convertView == null){
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.rss_feed_view, null);
                }
                TextView titleTextView = (TextView) convertView.findViewById(R.id.title_textview);
                titleTextView.setText(rssItems.get(position).getTitle());
                TextView descriptionTextView = (TextView) convertView.findViewById(R.id.description_textview);
                descriptionTextView.setText(rssItems.get(position).getDescription());
                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//OnClickListener to go to ItemActivity
                Intent intent = new Intent(getBaseContext(), ItemActivity.class);
                intent.putExtra("Item", (Parcelable) rssItems.get(position));
                startActivity(intent);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);//FloatingActionButton to delete feed
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RSSDatabaseHelper helper = new RSSDatabaseHelper(getBaseContext());
                SQLiteDatabase database = helper.getWritableDatabase();
                database.delete(RSSDatabaseHelper.getTableName(),
                        RSSDatabaseHelper.getColumnName() +" = ?", new String[]{feed.getUrl()});
                database.close();
                Intent intent = new Intent(getBaseContext(),FeedsListActivity.class);
                startActivity(intent);
            }
        });

    }

}
