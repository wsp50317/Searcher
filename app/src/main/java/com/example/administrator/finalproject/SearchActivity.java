package com.example.administrator.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Button Search;
    private EditText ItemEditText;
    private String WebName;
    private String SelectedTime;
    List<String> WebArrayList = new ArrayList<String>();
    List<String> TimeArrayList = new ArrayList<String>();
    ArrayAdapter<String> WebSpinnerArrayAdapter;
    ArrayAdapter<String> TimeSpinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Search = (Button)findViewById(R.id.Search);
        ItemEditText = (EditText)findViewById(R.id.ItemEditText);
        Search.setOnClickListener(SearchListener);

        Spinner WebSpinner = (Spinner)findViewById(R.id.WebSpinner);
        Spinner TimeSpinner = (Spinner)findViewById(R.id.TimeSpinner);
        InitializeWebSpinner(WebSpinner);
        InitializeTimeSpinner(TimeSpinner);
    }

    private void InitializeWebSpinner(Spinner spinner)
    {
        WebArrayList.add("Ptt");
        WebArrayList.add("Mobile01");
        WebArrayList.add("痞克幫");

        WebSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                WebArrayList );

        spinner.setAdapter(WebSpinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WebName = WebArrayList.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }

    private void InitializeTimeSpinner(Spinner spinner)
    {
        TimeArrayList.add("無");
        TimeArrayList.add("過去1小時");
        TimeArrayList.add("過去24小時");
        TimeArrayList.add("過去1週");
        TimeArrayList.add("過去1個月");
        TimeArrayList.add("過去1年");

        TimeSpinnerArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                TimeArrayList );

        spinner.setAdapter(TimeSpinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectedTime = TimeArrayList.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }


    private Button.OnClickListener SearchListener= new Button.OnClickListener(){
        @Override
        public void onClick(View v){ //按下搜尋時的動作
            String UserText = ItemEditText.getText().toString();

            Intent intent = new Intent();//切換到搜尋結果頁面
            intent.setClass(SearchActivity.this  , MainActivity.class);

            Bundle bundle = new Bundle();///傳遞使用者輸入給搜尋結果頁面
            bundle.putString("UserText", UserText);
            bundle.putString("WebName", WebName);
            bundle.putString("SelectedTime", SelectedTime);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //右上角選單程式
        /*for(int i = 0; i < 5; i++){
            menu.add(Menu.NONE , Menu.FIRST + i , Menu.NONE , "Item " + Integer.toString(i + 1));
        }*/
        menu.add(Menu.NONE , Menu.FIRST , Menu.NONE , "書籤");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //點選選單後的處理
        /*if(item.getGroupId() == Menu.NONE) {
            Toast.makeText(getApplicationContext(),
                    item.getTitle(),
                    Toast.LENGTH_SHORT).show();
        }*/
        if(item.getTitle() == "書籤")
        {
            SwitchToFavoriteList();
        }
        return super.onOptionsItemSelected(item);
    }

    public String ReadFile() {
        //取得檔名
        String fileName = "test.txt";
        String line="";
        StringBuffer buf = new StringBuffer();

        //Input stream
        try {
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            while ((line=br.readLine())!=null) {
                buf.append(line).append("\n");
            }

            br.close();
            isr.close();
            fis.close();
            return buf.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "0";
        } catch (IOException e) {
            e.printStackTrace();
            return "0";
        }
    }

    private void SwitchToFavoriteList() //跳轉至我的最愛
    {
        Intent intent = new Intent();//切換到搜尋結果頁面
        intent.setClass(SearchActivity.this  , FaveritePage.class);

        Bundle bundle = new Bundle();///傳遞使用者輸入給搜尋結果頁面
        bundle.putString("FavData",ReadFile());
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
