package com.harshit.newsreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Statement;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> dbUrl = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    SQLiteDatabase newsDB;

    public void updateListView (){

        Cursor c = newsDB.rawQuery("SELECT * FROM newArticle", null);
        int urlIndex = c.getColumnIndex("url");
        int titleIndex = c.getColumnIndex("title");
        if(c.moveToFirst()){
            titles.clear();
            dbUrl.clear();
            do{

                titles.add(c.getString(titleIndex));
                dbUrl.add(c.getString(urlIndex));

            }while(c.moveToNext());
        }
        arrayAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsDB = this.openOrCreateDatabase("newArticles", MODE_PRIVATE, null);
        newsDB.execSQL("CREATE TABLE IF NOT EXISTS newArticle(id INTEGER PRIMARY KEY, articleId VARCHAR, title VARCHAR, url VARCHAR)");
        newsDB.execSQL("DELETE FROM newArticle");

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = newsDB.rawQuery("SELECT * FROM newArticle", null);
                c.moveToPosition(position);
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                intent.putExtra("url", dbUrl.get(position));
                startActivity(intent);
            }
        });

        downloadTask task = new downloadTask();

        task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        updateListView();
    }

     public class downloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            try {

                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1){

                    result += (char) data ;
                    data = reader.read();
                }
                JSONArray jsonArray = new JSONArray(result);
                for(int i =0 ; i<20 ; i++){

                    String articleId = jsonArray.get(i).toString();
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();
                    String articleInfo = "";
                    while(data!=-1){

                        articleInfo += (char) data;
                        data = reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(articleInfo);

                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");

                        Log.i("check", Integer.toString(i));

                        String sql = "INSERT INTO newArticle (articleId, title, url) VALUES (?, ?, ?)";
                        SQLiteStatement statement = newsDB.compileStatement(sql);
                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleUrl);
                        statement.execute();

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute (String result){

            updateListView();

        }
    }

}
