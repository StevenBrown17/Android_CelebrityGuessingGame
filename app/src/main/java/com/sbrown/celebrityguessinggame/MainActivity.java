package com.sbrown.celebrityguessinggame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This application is dependant on 'http://www.posh24.se/kandisar'.
 * if the HTML format of this page changes, this app will not work unless you alter the regex below to match
 * the new format.
 *
 * This application is mostly to show how to pull data from a website. JSON is a much more reliable way to pull data.
 */

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button1, button2, button3, button4;
    DownloadImage imageTask;
    DownloadTask task;
    List<Bitmap> imageList = new ArrayList<Bitmap>();
    List<String> nameList = new ArrayList<String>();
    Random rand = new Random();
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);

        loadLists();// only need this once.
        
        //Initial setup
        setImage();
        setButtons();

    }//end onCreate


    /**
     * compares the index of the nameList, with the tag of the button that was clicked.
     * @param view
     */
    public void checkAnswer(View view){
        int id = (int)view.getTag();
        Toast toast = new Toast(this);

        if(id == index){
            toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        }else{
            toast.makeText(this, "Incorrect! This is: "+ nameList.get(index), Toast.LENGTH_SHORT).show();
        }

        setImage();
        setButtons();

    }

    /**
     * setImage will generate a number between 0 and list.size()
     * The random number will then give us the index of where the image is we will use.
     * the global variable is set to this random number
     */
    public void setImage(){

        //rand.nextInt((max - min) + 1) + min;
        int max = imageList.size();
        int min = 0;

        if(max != min)
            index = rand.nextInt((max - min) + 1) + min;
        else
            index = 0;

        imageView.setImageBitmap(imageList.get(index));

    }//end setImage

    /**
     * loadLists use our UD classes and load the celebrity pictures and names into list.
     * The index of the picture in the imageList, will line up with the index of the name in the nameList
     */
    public void loadLists(){
        String html="";
        task = new DownloadTask();
        imageTask = new DownloadImage();
        Bitmap celebrity;

        //try and get HTML into variable
        try{
            html = task.execute("http://www.posh24.se/kandisar").get();
        }catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        String[] htmlSplit = html.split("<div class=\"sidebarContainer\">");

        Pattern imagePattern = Pattern.compile("<img src=\"(.*?)\"");
        Pattern namePattern = Pattern.compile("alt=\"(.*?)\"");
        Matcher imageMatcher = imagePattern .matcher(htmlSplit[0]);
        Matcher nameMatcher = namePattern.matcher(htmlSplit[0]);


        while(imageMatcher.find()){

            try {
                imageTask = new DownloadImage();
                celebrity = imageTask.execute(imageMatcher.group(1)).get();
                imageList.add(celebrity);

            }catch(Exception e){
                e.printStackTrace();
            }
        }//end image while

        while(nameMatcher.find()){
            nameList.add(nameMatcher.group(1));

        }//end name while

    }//end loadLists()

    /**
     * setButtons randomly sets a button to the correct answer and sets the tag to the correct index.
     * the remaining buttons are assigned a random name, and tag set to 999;
     */
    public void setButtons(){

        int i = rand.nextInt(4);

        if(i == 0){
            button1.setText(nameList.get(index)); button1.setTag(index);
            button2.setText(nameList.get(rand.nextInt(101))); button2.setTag(999);
            button3.setText(nameList.get(rand.nextInt(101))); button3.setTag(999);
            button4.setText(nameList.get(rand.nextInt(101))); button4.setTag(999);
        }else if(i == 1){
            button2.setText(nameList.get(index)); button2.setTag(index);
            button1.setText(nameList.get(rand.nextInt(101))); button1.setTag(999);
            button3.setText(nameList.get(rand.nextInt(101))); button3.setTag(999);
            button4.setText(nameList.get(rand.nextInt(101))); button4.setTag(999);
        }else if(i == 2){
            button3.setText(nameList.get(index));button3.setTag(index);
            button2.setText(nameList.get(rand.nextInt(101))); button2.setTag(999);
            button1.setText(nameList.get(rand.nextInt(101))); button1.setTag(999);
            button4.setText(nameList.get(rand.nextInt(101))); button4.setTag(999);
        }else if(i == 3){
            button4.setText(nameList.get(index)); button4.setTag(index);
            button2.setText(nameList.get(rand.nextInt(101))); button2.setTag(999);
            button3.setText(nameList.get(rand.nextInt(101))); button3.setTag(999);
            button1.setText(nameList.get(rand.nextInt(101))); button1.setTag(999);
        }


    }//end set buttons


    //UD classes

    /**
     * used to pull the images from the website, given the URL of the image.
     */
    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... urls) {

            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.connect();
                InputStream inputStream = connection.getInputStream();

                Bitmap bm = BitmapFactory.decodeStream(inputStream);

                return bm;


            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    /**
     * used to pull the HTML from the website
     */
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char)data;

                    result += current;
                    data = reader.read();
                }

                return result;



            }catch (Exception e){
                e.printStackTrace();

                return("Failed");
            }
        }
    }//end downloadTask class


}//end MainActivity class
