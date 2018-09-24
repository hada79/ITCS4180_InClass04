package com.jasonhada.inclass04;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> urls;
    int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton prev_btn = (ImageButton) findViewById(R.id.prev_btn);
        ImageButton next_btn = (ImageButton) findViewById(R.id.next_btn);
        final ImageView photo_iv = (ImageView) findViewById(R.id.photo_iv);

        prev_btn.setVisibility(View.INVISIBLE);
        next_btn.setVisibility(View.INVISIBLE);

        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "index is " + index);
                if(index == 0) {
                    Log.d("demo", "urls size is " + urls.size());
                    index = urls.size() - 1;
                } else {
                    index--;
                }

                new GetPhotoAsync().execute(urls.get(index));
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index == urls.size() - 1) {
                    index = 0;
                } else {
                    index++;
                }

                new GetPhotoAsync().execute(urls.get(index));
            }
        });


        // This goes after alert dialog
        RequestParams params = new RequestParams();
        params.addParameter("keyword", "android");
        new GetUrlsAsync(params).execute("http://dev.theappsdr.com/apis/photos/index.php");
    }

    private class GetUrlsAsync extends AsyncTask<String, Void, ArrayList<String>> {
        RequestParams mParams;
        public GetUrlsAsync(RequestParams params) {
            mParams = params;
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            ArrayList<String> result = new ArrayList<>();

            try {
                URL url = new URL(mParams.getEncodedUrl(strings[0]));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        result.add(line);
                    }
                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        // Runs on main thread

        @Override
        protected void onPostExecute(ArrayList<String> apiUrls) {
            if(apiUrls != null) {
                urls = apiUrls;
                new GetPhotoAsync().execute(urls.get(index));
                if(apiUrls.size() > 1){
                    findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.prev_btn).setVisibility(View.VISIBLE);
                }
            } else {
                Log.d("demo", "No images found");
            }
            super.onPostExecute(apiUrls);
        }
    }

    private class GetPhotoAsync extends AsyncTask<String, Void, Bitmap> {
        private ProgressDialog dialog;

        private GetPhotoAsync() {
            dialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            HttpURLConnection connection = null;
            ArrayList<String> result = new ArrayList<>();
            Bitmap bitmap = null;

            try {
                URL url = new URL(strings[0]    );
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Downloading Photo...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            ImageView image = findViewById(R.id.photo_iv);
            image.setImageBitmap(bitmap);

            super.onPostExecute(bitmap);
        }
    }
}
