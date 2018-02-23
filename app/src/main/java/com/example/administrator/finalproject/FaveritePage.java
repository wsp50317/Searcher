package com.example.administrator.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FaveritePage extends AppCompatActivity {

    private TextView NotFoundMessage;//找不到資料時顯示
    private ListView FavListView;
    List<String> FaveriteTitleArrayList = new ArrayList<String>();
    List<String> FaveriteTextArrayList = new ArrayList<String>();
    private ArrayAdapter arrayAdapter;
    private String FavData;
    private String SplitFile[];

    private int TempPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faverite_page);

        NotFoundMessage = (TextView) findViewById(R.id.NotFoundMessage);

        FavListView = (ListView) findViewById(R.id.FavListView);

        Bundle ResultBundle =getIntent().getExtras();//建立bundle
        FavData = ResultBundle.getString("FavData");//取出File資料
        LoadFavoriteList(FavData);
        CreateTestListView();

    }

    private void LoadFavoriteList(String FileData)//return值為false則list
    {
        //mTextMessage.setText(FileData);
        SplitFile = FileData.split("\n");
        FaveriteTitleArrayList.clear();
        FaveriteTextArrayList.clear();
        if(FileData.isEmpty()) //LIST為空
        {
            //
        }
        else//如果Data為空則讀取
        {
            for (int i = 0; i < SplitFile.length; i = i + 2) {
                FaveriteTitleArrayList.add(SplitFile[i]);
                FaveriteTextArrayList.add(SplitFile[i + 1]);
            }
        }
    }

    private void CreateTestListView() {

        arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.listview_layout,
                FaveriteTitleArrayList);

        FavListView.setAdapter(arrayAdapter);
        FavListView.setOnItemClickListener(FavListClickListener);//短按超連結

        if(FaveriteTitleArrayList.isEmpty()){ //設定找不到結果時顯示的字串
            NotFoundMessage.setText("目前尚無書籤");
        }
        else{
            NotFoundMessage.setText(null);
        }

        FavListView.setOnItemLongClickListener(FavListLongClickListener);//長按刪除
    }

    //設定單點擊時的超連結
    private ListView.OnItemClickListener FavListClickListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position , long id){
            String sel=parent.getItemAtPosition(position).toString(); //取得ListView中某格的元素
            //mTextMessage.setText(FaveriteTextArrayList.get(position));//將點擊內容顯示在測資資訊

            Uri uri=Uri.parse(FaveriteTextArrayList.get(position));///按下去後有超連結
            Intent i=new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
        }
    };

    //設定長點擊時的刪除
    private ListView.OnItemLongClickListener FavListLongClickListener = new ListView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position , long id){

            TempPosition = position;

            AlertDialog.Builder FavoriteCheckBuilder = new AlertDialog.Builder(FaveritePage.this)
                    .setTitle("刪除")
                    .setIcon(R.mipmap.my_ic_launcher_round)
                    .setMessage("要將此頁刪除嗎?")
                    .setPositiveButton("確定",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialoginterface, int id)
                        {
                            RewriteFavData(TempPosition);
                            WriteFile(FavData);
                            LoadFavoriteList(FavData);
                            arrayAdapter.notifyDataSetChanged();

                            Toast.makeText(getApplicationContext(),
                                    "已刪除書籤",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消",new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialoginterface, int id)
                        {
                            //取消不做事
                        }
                    });
            AlertDialog alert = FavoriteCheckBuilder.create();
            alert.show();
            return true; //回傳 false，長按後該項目被按下的狀態不會保持。
        }
    };

    public void WriteFile(String DataString)
    {
        String fileName = "test.txt";
        getDir(fileName, Context.MODE_PRIVATE);

        try{
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw=new BufferedWriter(osw);
            bw.write(DataString);
            //bw.write("\n");
            bw.close();
            osw.close();
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void RewriteFavData(int position)
    {
        String line="";
        StringBuffer buf = new StringBuffer();

        //Input stream
        try {
            InputStream fis = new ByteArrayInputStream(FavData.getBytes()); //讀取FavData資料
            InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            int readtimes = 0;
            while ((line=br.readLine())!=null) {
                if(readtimes/2 != position) { //長按的position不要讀取
                    buf.append(line).append("\n");
                }
                readtimes++;
            }
            br.close();
            isr.close();
            fis.close();
            FavData = buf.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
