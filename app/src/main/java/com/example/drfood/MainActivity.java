package com.example.drfood;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private BackKeyClickHandler backKeyClickHandler;

    ImageButton startButton;
    ImageButton Button;
    ImageButton Setting;
    ImageButton UserButton;
    materialParser pharm;
    searchAsync search;
    searchAsync searchAsync;
    String newT;
    ListView search_list;
    private final static int CAMERA_PERMISSIONS_GRANTED = 100;

    //데이터 베이스 쪽
    private DatabaseReference mDatabase;
    private String Snack_Name;
    private boolean Exgist_Result;

    String imgUrl;
    String rawMaterial;
    String tag;
    String allergy;


    //시작모드 바코드 모드 관련
    private SharedPreferences Start_auto;
    private SharedPreferences.Editor autoLogin;
    String True;
    Boolean Ready;

    //additives관련
    int Additives_Num = 0;
    int Num_Size = 0;
    String Additives_EWG[] = new String[100];
    String Additives_Name[] = new String[100];
    String No_Additives_Name[] = new String[100];
    int No_Additives_Num = 0;
    int allergy_num_intent = 0;
    int isContained = 0;

    String Intent_rawMaterialSplitedArray[] = new String[100];
    String Intent_allergyListSplitedArray[] = new String[20];

    //Person(User) 정보관련
    private String UserUid;
    private String UserEmail;
    private String UserName;
    private String UserAge;
    private String UserS;
    private ArrayList<String> Allegy_Types;
    private int Allegy_Exgist_Num;
    private ArrayList<Integer> Allegy_Exgist_index;
    private String Trans_Allegy_Exgist_index;



    Intent intent_PDInfo;
    Intent  User_Information;

    int Temp1;
    int Temp2;
    String Temp3;
    String Temp4;
    int Count;

    SearchView searchView;
    private List<String> searchList;
    SearchViewAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search_list = findViewById(R.id.search_list);
        search_list.setVisibility(View.INVISIBLE);


        backKeyClickHandler = new BackKeyClickHandler(this);
        //초기화
        Allegy_Exgist_index = new ArrayList<Integer>();
        Allegy_Exgist_index.clear();
        UserAge = "0";
        UserS = "";

        //시작모드 관련
        Start_auto = getSharedPreferences(" Start_auto", MainActivity.MODE_PRIVATE);
        True = Start_auto.getString("inputpas","0");
        autoLogin = Start_auto.edit();
        Log.d("True", ""+True);

        searchView = findViewById(R.id.search_ex);
        searchView.setQueryHint("검색하실 제품명을 입력하세요.");
        searchView.setIconified(false);
        searchView.clearFocus();

        adapter = new SearchViewAdapter();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snack_Name = query;
                pharm = new materialParser();
                pharm.execute();
                search_list.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search_list.setVisibility(View.VISIBLE);
                if(newText.equals(newT)) return true;
                else if(!newText.equals("")) {
                    Log.e("반복","반복");
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    newT = newText;
                    search_list.setAdapter(adapter);
                    search = new searchAsync();
                    search.execute();
                }
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.clear();
                adapter.notifyDataSetChanged();
                search_list.setVisibility(View.GONE);
                return true;
            }
        });

            // 위에서 생성한 listview에 클릭 이벤트 핸들러 정의.
            search_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    // get item
                    searchViewItem item = (searchViewItem) parent.getItemAtPosition(position) ;

                    Snack_Name = item.getTitle();
                    pharm = new materialParser();
//                    adapter.clear();
//                    adapter.notifyDataSetChanged();
                    pharm.execute();

                }
            }) ;


        intent_PDInfo = new Intent(MainActivity.this, Product_Information.class);
        //데이터 베이스 주소 가져오기
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //로그인 화면으로 넘어감
        Intent Go_Login_Page = new Intent(MainActivity.this,LoginPageAct.class);

        startActivityForResult(Go_Login_Page, 3000);

        if(True.equals("1")){
            Intent goNextActivity = new Intent(getApplicationContext(), QRCodeScan.class);
            startActivityForResult(goNextActivity, 1001);
        }



        //3000은 로그인



//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//        StrictMode.setThreadPolicy(policy);
        //사용자 화면으로 넘어가는 intent
        User_Information = new Intent(MainActivity.this, Person_Information.class);

        startButton = findViewById(R.id.barcode);   // Button Boilerplate
        Button = findViewById(R.id.chips_button);
        UserButton = findViewById(R.id.human);
        Setting = findViewById(R.id.setting);

        getCameraPermission();

        // 다음 Activity로 넘어가기 위한 onClickListener
        // 이렇게 한 이유는 Permission Check 가
        // 기본적으로 UI Thread가 아닌 다른 Thread 에서 동시에 실행되기 때문에
        // 첫 실행 때, 권한이 없어서 SurfaceView 에서 addCallback 처리를 제대로 못하는 상황이 생긴다.
        // 그래서 검은 화면이 나온다. 고로, 아예 Activity를 다르게 해줬다.
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goNextActivity = new Intent(getApplicationContext(), QRCodeScan.class);
                startActivityForResult(goNextActivity, 1001);
            }
        });

        UserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User_Information.putExtra("UserUid", UserUid);
                User_Information.putExtra("UserName", UserName);
                User_Information.putExtra("UserEmail", UserEmail);
                User_Information.putExtra("UserAge", UserAge);
                User_Information.putExtra("UserS", UserS);
                User_Information.putExtra("Allegy_Types", Allegy_Types);
                User_Information.putExtra("Allegy_Exgist_index", Allegy_Exgist_index);
                User_Information.putExtra("Allegy_Exgist_Num",Allegy_Exgist_Num);
                User_Information.putExtra("Trans_Allegy_Exgist_index",Trans_Allegy_Exgist_index);

                startActivityForResult(User_Information,3100);
            }
        });

        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Setting_Page = new Intent(MainActivity.this, Setting_Activity.class);
                True = Start_auto.getString("inputpas","0");
                Setting_Page.putExtra("True",True);
                startActivityForResult(Setting_Page, 3200);
            }
        });

        //알러지 유발하는 것들 배열안에 넣습니다.
        Allegy_Types = new ArrayList<String>();
        Allegy_Types.clear();
        Allegy_Types.add("게");        Allegy_Types.add("새우");        Allegy_Types.add("땅콩");        Allegy_Types.add("호두");
        Allegy_Types.add("대두");        Allegy_Types.add("밀");        Allegy_Types.add("메밀");        Allegy_Types.add("우유");
        Allegy_Types.add("난류");        Allegy_Types.add("생선류");        Allegy_Types.add("오징어");        Allegy_Types.add("조개");
        Allegy_Types.add("닭도리");        Allegy_Types.add("돼지고기");        Allegy_Types.add("쇠고기");        Allegy_Types.add("딸기");
        Allegy_Types.add("망고");        Allegy_Types.add("멜론");        Allegy_Types.add("바나나");        Allegy_Types.add("사과");
        Allegy_Types.add("살구");        Allegy_Types.add("오렌지");        Allegy_Types.add("자두");        Allegy_Types.add("참외");
        Allegy_Types.add("체리");        Allegy_Types.add("키위");        Allegy_Types.add("복숭아");        Allegy_Types.add("토마토");
        Allegy_Types.add("계피");        Allegy_Types.add("마늘");        Allegy_Types.add("버섯");        Allegy_Types.add("당근");
        Allegy_Types.add("오이");        Allegy_Types.add("쌀");        Allegy_Types.add("번데기");

    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        backKeyClickHandler.onBackPressed();
    }

    private boolean getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // 권한이 왜 필요한지 설명이 필요한가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(this, "카메라 사용을 위해 확인버튼을 눌러주세요!", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        CAMERA_PERMISSIONS_GRANTED);
                return true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            final String result = data.getStringExtra("key");
            if(TextUtils.isEmpty(result)){
                return;
            }
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

            mDatabase.child("food").child("snack").child("바코드").child(result).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getKey().equals(result)){

                        //데이터베이스 안에 존재하면 true 없으면 false
                        Exgist_Result = true;
                        if(dataSnapshot.getValue() != null && dataSnapshot.getValue() != "") {
                            Snack_Name = dataSnapshot.getValue().toString();
                            Log.d("Snack name : ", Snack_Name);

                            pharm = new materialParser();
                            pharm.execute();
                        }
                        else{
                            Intent popup_intent = new Intent(MainActivity.this, no_db_popup_activity.class);
                            startActivityForResult(popup_intent, 1);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else if(requestCode == 3000){
            UserUid = data.getStringExtra("UserUid");
            UserName = data.getStringExtra("UserName");
            UserEmail = data.getStringExtra("UserEmail");
            UserAge = data.getStringExtra("UserAge");
            UserS = data.getStringExtra("UserS");
            Trans_Allegy_Exgist_index = data.getStringExtra("Trans_Allegy_Exgist_index");
            Allegy_Exgist_Num = data.getIntExtra("Allegy_Exgist_Num", 0);


            Temp1 = 0;
            Temp2 = 0;
            Temp4 = Trans_Allegy_Exgist_index;
            Count = 0;
            while(Count !=Allegy_Exgist_Num){
                Temp2 = Temp4.indexOf("_");
                Temp3 = Temp4.substring(Temp1, Temp2);
                Temp4 = Temp4.replaceFirst( Temp3+"_", "");

                Allegy_Exgist_index.add(Integer.parseInt(Temp3));
                Log.d("Temp4", Temp4);
                Log.d("Temp", Temp3);
                Count++;

            }

            Log.d("UserUid_Main", UserUid);
            Log.d("UserName_Main",UserName);
            Log.d("UserEmail_Main", UserEmail);

        }
        else if(requestCode == 3100){
            Trans_Allegy_Exgist_index = data.getStringExtra("Trans_Allegy_Exgist_index");
            Allegy_Exgist_Num = data.getIntExtra("Allegy_Exgist_Num", 0);
            Allegy_Exgist_index = data.getIntegerArrayListExtra("Allegy_Exgist_index");
            //Log.d("Trans다다다", Trans_Allegy_Exgist_index);
            //Log.d("Trans다다다", Allegy_Exgist_Num +"");
            //Log.d("Trans다다다", Allegy_Exgist_index.get(0)+ "");
        }
        else if(requestCode == 3200){
            Ready = data.getBooleanExtra("Ready", false);
            Log.d("ReadyMain", ""+Ready);
            if(Ready){

                autoLogin.putString("inputpas", "1");
                autoLogin.commit();
                True = Start_auto.getString("inputpas","0");
                Log.d("True1", "" + True);
            }else{

                autoLogin.putString("inputpas", "0");
                autoLogin.commit();
                True = Start_auto.getString("inputpas","0");
                Log.d("True2", "" + True);
            }
        }


    }
    class searchAsync extends AsyncTask<Void,Void,String>{
        List<String> productName = new ArrayList<>();
        List<String> productKind = new ArrayList<>();
        String avoidRepeat = newT;
        searchAsync(){
            Log.e("반복","반복_CONSTRUCT");
            searchList = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            Log.e("반복","반복_PRE");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params){
            try {
                if(avoidRepeat.equals(newT)){
                URL url = new URL("http://apis.data.go.kr/B553748/CertImgListService/getCertImgListService?serviceKey=%2BwvPpNobnpO%2BxNDsB3NdwZqjZYg4C8JqEy7NhZxXof%2F2Owy9Vu2eYP1pZVtIw%2FcPEVTx8nKQ1ph%2F4ppRNxKBLA%3D%3D&prdlstNm="
                        + newT);
                Log.e("뉴텍", newT);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                xpp.setInput(bis, "utf-8");
                String prdname = "";
                String prdkind = "";
                int event_type = xpp.getEventType();

                while (event_type != XmlPullParser.END_DOCUMENT) {
                    if (event_type == XmlPullParser.START_TAG) {
                        tag = xpp.getName();
                    } else if (event_type == XmlPullParser.TEXT) {
                        /*
                         * 성분만 가져와 본다.
                         */
                        if (tag.equals("prdlstNm")) {
                            if (!xpp.getText().equals("\n")) {
                                //searchList.add(xpp.getText());
                                prdname = xpp.getText();
                                productName.add(prdname);
                                Log.e("제품", xpp.getText());
                            }
                        } else if (tag.equals("prdkind")) {
                            if (!xpp.getText().equals("\n")) {
                                //searchList.add(xpp.getText());
                                prdkind = xpp.getText();
                                productKind.add(prdkind);
                                Log.e("제품정보", xpp.getText());
                            }
                        }
                    } else if (event_type == XmlPullParser.END_TAG) {
                        tag = xpp.getName();
                        if (tag.equals("item")) {
                            //Log.e("태그의 끝",rawMaterial);
                            //System.out.println(rawMaterial);
                        }
                        //item별로 분리
                    }
                    //adapter.addItem(prdname, prdkind);
                    event_type = xpp.next();
                }
            }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return newT;
        }


        protected void onPostExecute(String text){
            for(int i = 0; i < productKind.size(); i++){
                adapter.addItem(productName.get(i),productKind.get(i));
                adapter.notifyDataSetChanged();
            }
        }
    }
    class materialParser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //public final static String PHARM_URL = "http://openapi.hira.or.kr/openapi/service/pharmacyInfoService/getParmacyBasisList";
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://apis.data.go.kr/B553748/CertImgListService/getCertImgListService?serviceKey=%2BwvPpNobnpO%2BxNDsB3NdwZqjZYg4C8JqEy7NhZxXof%2F2Owy9Vu2eYP1pZVtIw%2FcPEVTx8nKQ1ph%2F4ppRNxKBLA%3D%3D&prdlstNm="
                        + Snack_Name);


                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                xpp.setInput(bis, "utf-8");

                int event_type = xpp.getEventType();

                ArrayList<String> materialList = new ArrayList<>();
                ArrayList<String> allergyList = new ArrayList<>();
                int i = 0;
                while (event_type != XmlPullParser.END_DOCUMENT && (i != 1)) {
                    if (event_type == XmlPullParser.START_TAG) {
                        tag = xpp.getName();
                    } else if (event_type == XmlPullParser.TEXT) {
                        /*
                         * 성분만 가져와 본다.
                         */
                        if(tag.equals("totalCount")) {
                            if(!xpp.getText().equals("\n")){
                                String count = xpp.getText();
                                if(count.equals("0")){
                                    Intent popup_intent = new Intent(MainActivity.this, no_db_popup_activity.class);
                                    startActivityForResult(popup_intent, 1);
                                    this.cancel(true);
                                }

                            }
                        }
                        else if (tag.equals("rawmtrl")) {
                            if(!xpp.getText().equals("\n")){
                                rawMaterial = xpp.getText();
                            }
                        }
                        else if(tag.equals("imgurl1")){
                            if(!xpp.getText().equals("\n")){
                                imgUrl = xpp.getText();
                            }
                        }
                        else if(tag.equals("allergy")){
                            if(!xpp.getText().equals("\n")) {
                                allergy = xpp.getText();
                            }
                            //Log.e("알러지",allergy);
                        }
                    } else if (event_type == XmlPullParser.END_TAG) {
                        tag = xpp.getName();
                        if (tag.equals("item")) {
                            //Log.e("태그의 끝",rawMaterial);
                            //System.out.println(rawMaterial);
                            materialList.add(rawMaterial);
                            allergyList.add(allergy);
                            i++;
                        }
                        //item별로 분리
                    }
                    event_type = xpp.next();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return rawMaterial;
        }

        @Override
        protected void onPostExecute(String material){
            String[] rawMaterialSplited = new String[8];
            String[] allergyListSplited = new String[8];
            if(rawMaterial != null && rawMaterial != "") {
                rawMaterialSplited = rawMaterial.split("\\(|\\)|\\{|\\}|\\[|\\]|\\,|\\s|\\;");
            }
            if(allergy != null && rawMaterial != "") {
                allergyListSplited = allergy.split("\\,|\\s|\\(|\\)");
            }

            final List<String> rawMaterialSplitedArray = new ArrayList<>();
            final List<String> allergyListSplitedArray = new ArrayList<>();
            for(int i = 0; i < rawMaterialSplited.length; i++){
                rawMaterialSplitedArray.add(rawMaterialSplited[i]);
            }
            for(int i = 0; i < allergyListSplited.length; i++){
                allergyListSplitedArray.add(allergyListSplited[i]);
            }








            //이 밑은 재료중 ~산이라는 글자가 포함되면 삭제
            //검토중
            Iterator<String> rawIt = rawMaterialSplitedArray.iterator();
            Iterator<String> allergyIt = allergyListSplitedArray.iterator();
            Count = 0;
            Num_Size = rawMaterialSplitedArray.size();
            Additives_Num = 0;
            No_Additives_Num = 0;
            Boolean Temp_Exgist = false;
            int Temp = 0;
            final int Where_Exgist_i[] = new int[100];

            while(rawIt.hasNext()){
                String test = rawIt.next();
                if(test.contains("%") || test.contains("러시아") ||test.contains("헝가리") || test.contains("세르비아") || test.contains("말레이시아")
                        || test.contains("베트남") || test.contains("미국") || test.contains("태국") || test.contains("벨기에") || test.contains("국내") || test.contains("국산") || test.contains("외국")
                        || test.contains("스페인") || test.contains("캐나다") || test.contains("호주")){
                    rawIt.remove();
                }
            }
            while(allergyIt.hasNext()){
                if(allergyIt.next().equals("함유")){
                    allergyIt.remove();
                    isContained++;
                }
            }

            Count = rawMaterialSplitedArray.size();

            for(int i = 0; i < rawMaterialSplitedArray.size(); i++){
                Log.e("재려전부다",rawMaterialSplitedArray.get(i));
                final String temp = rawMaterialSplitedArray.get(i);
                Intent_rawMaterialSplitedArray[i] = rawMaterialSplitedArray.get(i);
                No_Additives_Name[i] = temp;
                final int i_num = i;
                if(temp.equals("")){Count--; continue;}
                mDatabase.child("additives").child(temp).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Count--;
                        if(dataSnapshot.exists() && (dataSnapshot.getKey() != "additives")) {
                            Additives_EWG[Additives_Num] = dataSnapshot.child("EWG").getValue().toString();
                            Additives_Name[Additives_Num] = dataSnapshot.getKey();
                            No_Additives_Name[i_num] = "";
                            Log.d("재영이빠순아" + Additives_Num + "  " + Additives_Name[Additives_Num], Additives_EWG[Additives_Num]);
                            Additives_Num++;
                        }/*else if(dataSnapshot.getKey() != "additives"){
                            No_Additives_Name[No_Additives_Num] = rawMaterialSplitedArray.get(i_num);
                            Log.d("재영이빠돌아"+ No_Additives_Num +"  "  , No_Additives_Name[No_Additives_Num]);
                            No_Additives_Num++;
                        }*/


                        if(Count == 0 ){
                            if(Additives_Num == 0){
                                Additives_Name[0] = "없음";
                                Additives_EWG[0] = "없음";
                            }
                            intent_PDInfo.putExtra("성분EWG", Additives_EWG);
                            intent_PDInfo.putExtra("성분Name", Additives_Name);
                            intent_PDInfo.putExtra("성분Num", Additives_Num);
                            intent_PDInfo.putExtra("No성분Name", No_Additives_Name);
                            //intent_PDInfo.putExtra("No성분Num", No_Additives_Num);
                            Log.d("성분EWG", Additives_EWG.toString());
                            search_list.setVisibility(View.GONE);
                            startActivity(intent_PDInfo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });



            }

            int size = allergyListSplitedArray.size();
            for(int j = 0; j < allergyListSplitedArray.size(); j++){
                if(!(TextUtils.isEmpty(allergyListSplitedArray.get(j)))){
                    Log.e("알러지전부다",allergyListSplitedArray.get(j));
                    allergy_num_intent++;
                }
                else{
                    allergyListSplitedArray.remove(j);
                    j--;
                }
            }
            for(int k = 0; k < allergy_num_intent; k++){
                //Log.e("알러지넘버인텐트",allergy_num_intent);
                Log.e("알러지전부다2",allergyListSplitedArray.get(k));
                Intent_allergyListSplitedArray[k] = allergyListSplitedArray.get(k);
                Log.e("알러지전부다3",Intent_allergyListSplitedArray[k]);
            }
            String str = Integer.toString(allergyListSplitedArray.size());
            Log.e("알러지개수임ㅇㅇㅇㅇ",Integer.toString(allergy_num_intent));


            Log.e("이미지URL",imgUrl);
            //Intent intent_PDInfo = new Intent(MainActivity.this, Product_Information.class);
            intent_PDInfo.putExtra("이미지",imgUrl);
            intent_PDInfo.putExtra("이름",Snack_Name);
            intent_PDInfo.putExtra("성분",Intent_rawMaterialSplitedArray);
            intent_PDInfo.putExtra("알러지",Intent_allergyListSplitedArray);
            intent_PDInfo.putExtra("알러지개수",allergy_num_intent);
            intent_PDInfo.putExtra("Allegy_Types",Allegy_Types);
            intent_PDInfo.putExtra("Allegy_Exgist_index", Allegy_Exgist_index);
            intent_PDInfo.putExtra("Allegy_Exgist_Num",Allegy_Exgist_Num);
            intent_PDInfo.putExtra("No_Additives_Num",Num_Size);
            allergy_num_intent = 0;
            //intent_PDInfo.putExtra("isContained",isContained);






            //startActivity(intent_PDInfo);

        }

    }

    @Override
    public void onDestroy(){
        try {
            if (pharm.getStatus() == AsyncTask.Status.RUNNING) {
                pharm.cancel(true);
            }
            else{

            }
        } catch (Exception e) {
        }
        super.onDestroy();
    }


}