package com.inception.software.jsonparsingexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.inception.software.jsonparsingexample.utils.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getName();
    private static final String  url = "https://gist.githubusercontent.com/anishbajpai014/d482191cb4fff429333c5ec64b38c197/raw/b11f56c3177a9ddc6649288c80a004e7df41e3b9/HiringTask.json";

    private ProgressDialog progressDialog;
    
    @BindView(R.id.btn_load_data_from_local)
    Button btnLoadDataFromLocal;

    @BindView(R.id.btn_load_from_url)
    Button btnLoadDataFromURL;

    @BindView(R.id.list_data)
    ListView listViewData;

    ArrayList<HashMap<String, String>> dataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        dataList = new ArrayList<>();
        setListeners();








    }

    private void setListeners(){
        btnLoadDataFromLocal.setOnClickListener(this);
        btnLoadDataFromURL.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
     switch (v.getId()){
         case R.id.btn_load_data_from_local:
              LoadDataFromLocal();
             break;
         case R.id.btn_load_from_url:
             loadDataFromUrl();
             break;
     }
    }

    private void LoadDataFromLocal(){
            dataList = convertJsonToList(loadJSONFromAsset());
            updateJsonDataToListView();
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("HireingTask.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void loadDataFromUrl(){
        new FetchDataFromUriAsyncTask().execute();
    }


    private class  FetchDataFromUriAsyncTask extends AsyncTask<Void, Void, Void>{



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Fetching Data From...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();

            // Making a request to url and getting response
            String jsonResponseString= httpHandler.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonResponseString);
            jsonResponseString = jsonResponseString.replace("/","");
            convertJsonToList(jsonResponseString);


            
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressDialog.isShowing())
                progressDialog.dismiss();
                updateJsonDataToListView();

        }
    }



    private void updateJsonDataToListView(){
        ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, dataList,
                R.layout.row_data_items, new String[]{"id", "text",
        }, new int[]{R.id.tv_id,
                R.id.tv_text});

        listViewData.setAdapter(adapter);
    }

    private ArrayList<HashMap<String,String>> convertJsonToList(String jsonParsingString) {
        if (jsonParsingString != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonParsingString);

                // Getting JSON Array node
                JSONArray contacts = jsonObj.getJSONArray("data");

                // looping through All data
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);

                    String id = c.getString("id");
                    String text = c.getString("text");

                    // tmp hash map for single data
                    HashMap<String, String> dataMap = new HashMap<>();

                    // adding each child node to HashMap key => value
                    dataMap.put("id", id);
                    dataMap.put("text", text);

                    // adding contact to contact list
                    dataList.add(dataMap);


                }
                return dataList;
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

        }
      return dataList;
    }



}
