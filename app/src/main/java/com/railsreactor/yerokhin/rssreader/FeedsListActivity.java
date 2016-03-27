package com.railsreactor.yerokhin.rssreader;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class FeedsListActivity extends AppCompatActivity {

    private final static String LOG_TAG = FeedsListActivity.class.getName();

    private final static String DATABASE_NAME = "RSSDatabase";

    private final static int DATABASE_VERSION = 1;

    private final static String TABLE_NAME = "RSSLink";

    private final static String COLUMN_NAME = "LINK";

    SQLiteDatabase database;

    ListView listView;

    RSSDatabaseHelper databaseHelper;

    private void refresh(){//a method for refreshing feeds list
        if (database == null) {//getting a db
                database = databaseHelper.getReadableDatabase();
                Log.d(LOG_TAG, "Got readable database");
        }

        Cursor links = database.query(RSSDatabaseHelper.getTableName(),
                null,null,null,null,null,null);//getting all entries of the table
        links.moveToFirst();
        final List<RSSFeed> rssFeeds = new LinkedList<>();
        do{//iterating through all entries
            String link = links.getString(links.getColumnIndex(COLUMN_NAME));
            RSSFeed rssFeed;

            try {
                URL url = new URL(link);
                DownloadRSSTask task = new DownloadRSSTask();//starting an AsyncTask for downloading and parsing
                rssFeed = task.execute(url).get();
                rssFeeds.add(rssFeed);
                Log.d(LOG_TAG,"Finished parsing " + link);
            }
            catch (MalformedURLException ex){
                Log.e(LOG_TAG, ex.getMessage());
                Toast toast =Toast.makeText(getBaseContext(),"Some URLs are malformed",Toast.LENGTH_SHORT);
                toast.show();
            }
            catch (ExecutionException ex){
                Log.e(LOG_TAG, ex.getMessage());
            }
            catch (InterruptedException ex){
                Log.e(LOG_TAG,ex.getMessage());
            }
            links.moveToNext();
        }while(!links.isAfterLast());

        ArrayAdapter<RSSFeed> adapter = new ArrayAdapter<RSSFeed>(this,R.layout.rss_feed_view,rssFeeds){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {//overriding getView() for proper disptay of two strings
                if(convertView == null){
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.rss_feed_view, null);
                }
                TextView titleTextView = (TextView) convertView.findViewById(R.id.title_textview);
                titleTextView.setText(rssFeeds.get(position).getTitle());
                TextView descriptionTextView = (TextView) convertView.findViewById(R.id.description_textview);
                descriptionTextView.setText(rssFeeds.get(position).getDescription());
                return convertView;
            }
        };
        listView.setAdapter(adapter);
        Log.v(LOG_TAG, "adapter is set");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//setting OnItemClickListener for staring ItemsListActivity


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), ItemsListActivity.class);
                intent.putExtra("RSSFeed", rssFeeds.get((int) id));
                startActivity(intent);
            }
        });
        TextView refreshTimeTextView = (TextView) findViewById(R.id.refresh_time_textview);//writing refresh time in a TextView
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss ");
        refreshTimeTextView.setText(sdf.format(c.getTime()));
        Log.d(LOG_TAG,"Finished refreshing");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//FloatingActionButton to start AddingActivity
                Intent intent = new Intent(getBaseContext(),AddingActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.feeds_listview);

        databaseHelper = new RSSDatabaseHelper(this);

        refresh();

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.feeds_list_refresh);//pull-to-refresh implementation

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(3);//timer implementation
        stpe.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }, 120, 120, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feeds_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private RSSFeed parseRSS(String RSS, String urlString){
        String title = "",
                link ="",
                description = "";
        LinkedList<RSSItem> rssItems = new LinkedList<>();
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(new StringReader(RSS));
            int eventType = parser.getEventType();
            boolean inItem = false;//boolean which indicates whether the parser is within <item> tags
            String itemTitle = "",
                    itemLink = "",
                    itemDescription = "";
            while (eventType!=XmlPullParser.END_DOCUMENT){//parsing through the end of document
                switch (eventType){
                    case XmlPullParser.START_TAG:{
                        String tmp = parser.getName().toLowerCase();
                        switch (tmp){
                            case "item":{//reached <item> tag
                                inItem = true;
                                break;
                            }
                            case "title":{//copying title to String
                                eventType = parser.next();
                                if (eventType == XmlPullParser.CDSECT) eventType = parser.next();
                                if (!inItem){
                                    title = parser.getText();
                                }else{
                                    itemTitle = parser.getText();
                                }
                                break;
                            }
                            case "link":{//copying link to String
                                eventType = parser.next();
                                if (eventType == XmlPullParser.CDSECT) eventType = parser.next();
                                if(!inItem) {
                                    link = parser.getText();
                                }else{
                                    itemLink = parser.getText();
                                }
                                break;
                            }
                            case "description"://copying description to String
                            {
                                eventType = parser.next();
                                if (eventType == XmlPullParser.CDSECT) eventType = parser.next();
                                if(!inItem) {
                                    description = parser.getText();
                                }else{
                                    itemDescription = parser.getText();
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG:{
                        if(parser.getName().toLowerCase().equals("item")){//going out of <item> tags
                            inItem = false;
                            RSSItem rssItem = new RSSItem(itemTitle,itemLink,itemDescription);
                            rssItems.add(rssItem);
                            itemTitle = itemLink = itemDescription = "";
                        }
                    }
                }
                eventType = parser.next();
            }


        } catch (XmlPullParserException ex){
            Log.e(LOG_TAG,ex.getMessage());
            Toast toast =Toast.makeText(getBaseContext(),"Can't parse some RSS",Toast.LENGTH_SHORT);
            toast.show();
        } catch (IOException ex){
            Log.e(LOG_TAG,ex.getMessage());
            Toast toast =Toast.makeText(getBaseContext(),"Can't parse some RSS",Toast.LENGTH_SHORT);
            toast.show();
        }
        return new RSSFeed(urlString, title,link,description,rssItems);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            database.close();//closing DB when exiting the Activity
            Log.d(LOG_TAG,"Database closed");//doing it in onStop because it's almost 100% called
        }
        catch(NullPointerException ex){

        }
    }

    private class DownloadRSSTask extends AsyncTask<URL, Void, RSSFeed>{//AsyncTask for downloading and parsing

        private final String LOG_TAG = DownloadRSSTask.class.getName();
        @Override
        protected RSSFeed doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection connection;
            InputStream inputStream = null;
            StringBuffer stringBuffer = new StringBuffer("");
            try{
                connection = (HttpURLConnection)url.openConnection();//establishing connection
                connection.setRequestMethod("GET");
                connection.connect();
                inputStream = connection.getInputStream();
                stringBuffer = new StringBuffer();
                if (inputStream == null){
                    return null;
                }
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                String line=bufferedReader.readLine();
                while (bufferedReader.ready()){//copying webpage to stringbuffer
                    stringBuffer.append(line+"\n");
                    line = bufferedReader.readLine();
                    //if(line == null) line = bufferedReader.readLine();
                }

            } catch (IOException ex){
                Log.e(LOG_TAG,ex.getMessage());
            }

            return parseRSS(stringBuffer.toString(),url.toString());
        }
    }

}


class RSSDatabaseHelper extends SQLiteOpenHelper{

    public static String getNewDatabaseName() {
        return DATABASE_NAME;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getColumnName() {
        return COLUMN_NAME;
    }

    private final static String LOG_TAG = RSSDatabaseHelper.class.getName();

    private final static String DATABASE_NAME = "NewRSSDatabase";

    private final static int DATABASE_VERSION = 1;

    private final static String TABLE_NAME = "RSSLink";

    private final static String COLUMN_NAME = "LINK";

    public RSSDatabaseHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME + " TEXT PRIMARY KEY NOT NULL);");//creating table
            ContentValues contentValues = new ContentValues();
            contentValues.put("LINK", "http://feeds.bbci.co.uk/news/rss.xml");//putting a default RSS
            db.insert(TABLE_NAME, null, contentValues);
            Log.d(LOG_TAG, "table created" + TABLE_NAME);
        }
        catch (SQLException ex){
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
