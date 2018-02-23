package com.example.administrator.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LoadMoreListView mtestListView; //實際顯示在
    private TextView NotFoundMessage;//找不到資料時顯示
    List<String> TitleArrayList = new ArrayList<String>();
    List<String> TextArrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    private String Usertext;
    private String WebName;
    private String SelectedTime;
    private int PageNO = 0;

    private String TempTitle; //用來長按時暫存用
    private String TempText; //用來長按時暫存用

    /** Called when the activity is first created. */
    //找到UI工人的經紀人，這樣才能派遣工作  (找到顯示畫面的UI Thread上的Handler)
    private Handler mUI_Handler = new Handler();
    //宣告特約工人的經紀人
    private Handler mThreadHandler;
    //宣告特約工人
    private HandlerThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotFoundMessage = (TextView) findViewById(R.id.NotFoundMessage);

        Bundle ResultBundle =getIntent().getExtras();//建立bundle

        Usertext = ResultBundle.getString("UserText");//取出使用者輸入的字串
        WebName = ResultBundle.getString("WebName");//取出使用者選擇的網站並轉成網址
        SelectedTime = ResultBundle.getString("SelectedTime");

        //聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)
        mThread = new HandlerThread("name");
        //讓Worker待命，等待其工作 (開啟Thread)
        mThread.start();
        //找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)
        mThreadHandler=new Handler(mThread.getLooper());
        //請經紀人指派工作名稱 r，給工人做
        mThreadHandler.post(r1);

        mtestListView = (LoadMoreListView) findViewById(R.id.listview2);
        mtestListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                loadMore();
            }
        });


    }

    //工作名稱 r1 的工作內容
    private Runnable r1=new Runnable () {
        public void run() {
            // TODO Auto-generated method stub

            PageNO=0;

            String url = "https://www.google.com.tw/search?q=" +
                    Usertext +
                    "+site:ptt.cc&hl=zh-TW&as_qdr=all&" +
                    "start=" + String.valueOf(PageNO);
            try {
                for(int i=0;i<2;i++) {
                    PageNO = PageNO + i*10; //一開始加載兩次
                    if(i==1) //第二次暫停一下
                    {
                        try {
                            Thread.sleep(2000); //避免滑太快被GOOGLE鎖ip
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Connection conn = Jsoup.connect(WebNameToWebURL(WebName, SelectedTime));
                    // 修改http包中的header,伪装成浏览器进行抓取
                    conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                    Document doc = conn.get();

                    System.out.println(doc.title());
                    Elements h1s = doc.select("h3.r a");
                    Element thisOne = null;
                    for (Iterator it = h1s.iterator(); it.hasNext(); ) {
                        thisOne = (Element) it.next();
                        System.out.println(thisOne.text());
                        System.out.println(thisOne.attr("abs:href"));
                        TitleArrayList.add(thisOne.text());
                        TextArrayList.add(thisOne.attr("abs:href"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //請經紀人指派工作名稱 r，給工人做

            mUI_Handler.post(r2);
            mThreadHandler.removeCallbacks(r1);//查完第一則結果 直接結束此thread
        }
    };

    //工作名稱 r2 的工作內容
    private Runnable r2=new Runnable () {
        public void run() { //顯示搜尋完的結果
            // TODO Auto-generated method stub
            //.............................
            //顯示畫面的動作
            CreateTestListView();

        }
    };

    @Override

    protected void onDestroy() {
        super.onDestroy();

        //移除工人上的工作
        if (mThreadHandler != null) {

            mThreadHandler.removeCallbacks(r1);

        }
        //解聘工人 (關閉Thread)
        if (mThread != null) {
            mThread.quit();
        }
    }

    //設定單點擊時的超連結
    private ListView.OnItemClickListener mListClickListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position , long id){
            String sel=parent.getItemAtPosition(position).toString(); //取得ListView中某格的元素
            //mTextMessage.setText(sel);//將點擊內容顯示在測資資訊

            Uri uri=Uri.parse(TextArrayList.get(position));///按下去後有超連結
            Intent i=new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
        }
    };

    //設定長按時加至我的最愛
    private ListView.OnItemLongClickListener mListLongClickListener = new ListView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position , long id){

            TempText = TextArrayList.get(position);
            TempTitle = parent.getItemAtPosition(position).toString(); //取得ListView中某格的元素

            AlertDialog.Builder FavoriteCheckBuilder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("加入我的最愛")
                    .setIcon(R.mipmap.my_ic_launcher_round)
                    .setMessage("要將此頁加入我的最愛嗎?")
                    .setPositiveButton("確定",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialoginterface, int id)
                        {
                            WriteFile(TempTitle);
                            TempTitle = ""; //清空
                            WriteFile(TempText);
                            TempText = ""; //清空

                            Toast.makeText(getApplicationContext(),
                                    "已加入書籤",
                                    Toast.LENGTH_SHORT).show();
                            TempText = ReadFile();
                            //mTextMessage.setText(TempText);
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

    private void CreateTestListView(){

        arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.listview_layout,
                TitleArrayList );

        mtestListView.setAdapter(arrayAdapter);
        mtestListView.setOnItemClickListener(mListClickListener);//短按超連結
        mtestListView.setOnItemLongClickListener(mListLongClickListener);//長按加至我的最愛
        if(TitleArrayList.isEmpty()){ //設定找不到結果時顯示的字串{
            NotFoundMessage.setText("找不到搜尋結果，請更換關鍵字或搜尋時間");//將點擊內容顯示在測資資訊
        }
        else{
            NotFoundMessage.setText(null);
        }

        mtestListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                loadMore();
            }
        });

    }

    private void loadMore() { //上滑加載新內容
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(2000); //避免滑太快被GOOGLE鎖ip
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PageNO = PageNO + 10; //增加搜尋的頁數
                String url = "https://www.google.com.tw/search?q=" +
                        Usertext +
                        "+site:ptt.cc&hl=zh-TW&as_qdr=all&" +
                        "start=" + String.valueOf(PageNO);
                try {
                    Connection conn = Jsoup.connect(WebNameToWebURL(WebName,SelectedTime));
                    // 修改http包中的header,伪装成浏览器进行抓取
                    conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                    Document doc = conn.get();

                    //Document doc = Jsoup.connect(url).get();
                    System.out.println(doc.title());
                    Elements h1s = doc.select("h3.r a");
                    Element thisOne = null;
                    for(Iterator it = h1s.iterator(); it.hasNext();)
                    {
                        thisOne = (Element)it.next();
                        System.out.println(thisOne.text());
                        System.out.println(thisOne.attr("abs:href"));
                        TitleArrayList.add(thisOne.text());
                        TextArrayList.add(thisOne.attr("abs:href"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arrayAdapter.notifyDataSetChanged();
                        mtestListView.setLoadCompleted();
                    }
                });

            }
        }.start();
    }

    private String WebNameToWebURL(String WebName,String SelectedTime) {
        String WebURL= "https://www.google.com.tw"; //初始值 出error就搜這個
        String Site="";
        String qdr="";//時間的參數

        switch (WebName)
        {
            case "Ptt":
                Site="ptt.cc";
                break;
            case "Mobile01":
                Site="m.Mobile01.com";
                break;
            case "痞克幫":
                Site="pixnet.net";
                break;
        }

        switch (SelectedTime)
        {
            case "無":
                qdr="";
                break;
            case "過去1小時":
                qdr="h";
                break;
            case "過去24小時":
                qdr="d";
                break;
            case "過去1週":
                qdr="w";
                break;
            case "過去1個月":
                qdr="m";
                break;
            case "過去1年":
                qdr="y";
                break;
            default:
        }

        WebURL = "https://www.google.com.tw/search?q=" +
                Usertext +
                "+site:" + Site +
                "&hl=zh-TW&as_qdr=all" +
                "&tbs=qdr:" + qdr +
                "&start=" + String.valueOf(PageNO);
        return WebURL;
    }

    public void WriteFile(String DataString)
    {
        String fileName = "test.txt";
        getDir(fileName, Context.MODE_APPEND);

        try{
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_APPEND);
            OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter  bw=new BufferedWriter(osw);
            bw.write(DataString);
            bw.write("\n");

            bw.close();
            osw.close();
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        intent.setClass(MainActivity.this  , FaveritePage.class);

        Bundle bundle = new Bundle();///傳遞使用者輸入給搜尋結果頁面
        bundle.putString("FavData",ReadFile());
        intent.putExtras(bundle);

        startActivity(intent);
    }

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

}
