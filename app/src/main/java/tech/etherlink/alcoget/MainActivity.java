package tech.etherlink.alcoget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public ConstraintLayout mainMenu;
    public ConstraintLayout settingsMenu;
    public ConstraintLayout workSelectMenu;
    public ConstraintLayout testLayout;
    public ConstraintLayout workInTTN;
    public ConstraintLayout workType3;
    public String ServerURL="";
    public String nameTSD="";

    ///recycle

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<docitemclass> dc = new ArrayList<>();
    private List<docitemclass> dcfiltered = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //Для сканера
    public final static String BROADCAST_ACTION = "android.intent.ACTION_DECODE_DATA";
    public final static String PARAM_RESULT = "barcode_string";
    BroadcastReceiver br;

    private Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public void initSettings()
    {

        mainMenu = (ConstraintLayout) findViewById(R.id.menuLayout);
        settingsMenu = (ConstraintLayout) findViewById(R.id.settingsLayout);
        testLayout =  (ConstraintLayout) findViewById(R.id.testLayout);
        workSelectMenu =  (ConstraintLayout) findViewById(R.id.doclistLayout);
        workInTTN =  (ConstraintLayout) findViewById(R.id.doc_inTTNLayout);
        //presettings
        settingsMenu.setVisibility(View.INVISIBLE);
        testLayout.setVisibility(View.INVISIBLE);
        workSelectMenu.setVisibility(View.INVISIBLE);
        workInTTN.setVisibility(View.INVISIBLE);


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        ServerURL = sharedPref.getString("server_url", "");
        nameTSD = sharedPref.getString("name_tsd", "");

        TextView tx = (TextView) findViewById(R.id.serverAddr);
        tx.setText(ServerURL);

        TextView tx2 = (TextView) findViewById(R.id.TerminalName);
        tx2.setText(nameTSD);



        ///recycle
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    testPostRequestDocList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.docsRecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter();
        mAdapter.setOnClickListener(new CustomItemOnClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Log.i("onClick",String.valueOf(position));
                Log.i("onClick",dcfiltered.get(position).name);
                Log.i("onClick",dcfiltered.get(position).client);
                Log.i("onClick",dcfiltered.get(position).doc_GUID);

                if(dcfiltered.get(position).type==1) {

                    TextView docname = (TextView) findViewById(R.id.ttn_in_docname);
                    docname.setText(dcfiltered.get(position).name + " от " + dcfiltered.get(position).date);

                    TextView docinfo = (TextView) findViewById(R.id.ttn_in_doc_info);
                    docinfo.setText(dcfiltered.get(position).client);


                    TextView docinfo2 = (TextView) findViewById(R.id.ttn_in_doc_info2);

                    
                    if(dcfiltered.get(position).status==1)
                    {
                        docinfo2.setText("Свободен");
                        docinfo2.setTextColor(Color.rgb(36,147,13));
                    }
                    else
                    if(dcfiltered.get(position).status==2)
                    {
                        docinfo2.setText("Обработан");
                        docinfo2.setTextColor(Color.LTGRAY);
                    }
                    else
                    if(dcfiltered.get(position).status==3)
                    {
                        docinfo2.setText("В работе");
                        docinfo2.setTextColor(Color.RED);
                    }
                    else
                        docinfo2.setText("Неизв.");

                    workSelectMenu.setVisibility(View.INVISIBLE);
                    workInTTN.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLongItemClick(View v, int position) {
                Log.i("onLongClick",String.valueOf(position));
                Log.i("onLongClick",dcfiltered.get(position).name);
                Log.i("onLongClick",dcfiltered.get(position).client);
                Log.i("onLongClick",dcfiltered.get(position).doc_GUID);
            }
        });
        recyclerView.setAdapter(mAdapter);


        final Switch filter1 = (Switch) findViewById(R.id.switchFilter1);
        final Switch filter2 = (Switch) findViewById(R.id.switchFilter2);
        final Switch filter3 = (Switch) findViewById(R.id.switchFilter3);

        filter1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if( b == true)
                {
                    filter2.setChecked(false);
                    filter3.setChecked(false);

                    mAdapter.clearItems();
                    filterListdocs();
                    mAdapter.setItems(dcfiltered);
                    layoutManager.scrollToPosition(dcfiltered.size()-1);
                }
                else
                {
                    if(!filter2.isChecked() && !filter3.isChecked())
                    {
                        filter1.setChecked(true);
                    }
                }

            }
        });

        filter2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if( b == true)
                {
                    filter1.setChecked(false);
                    filter3.setChecked(false);

                    mAdapter.clearItems();
                    filterListdocs();
                    mAdapter.setItems(dcfiltered);

                    layoutManager.scrollToPosition(dcfiltered.size()-1);
                }
                else
                {
                    if(!filter1.isChecked() && !filter3.isChecked())
                    {
                        filter2.setChecked(true);
                    }
                }

            }
        });

        filter3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if( b == true)
                {
                    filter1.setChecked(false);
                    filter2.setChecked(false);

                    mAdapter.clearItems();
                    filterListdocs();
                    mAdapter.setItems(dcfiltered);

                    layoutManager.scrollToPosition(dcfiltered.size()-1);
                }
                else
                {
                    if(!filter1.isChecked() && !filter2.isChecked())
                    {
                        filter3.setChecked(true);
                    }
                }

            }
        });
    }


    private void initUIDocTTN() {
        //settingsMenu UI listeners
        //back
        Button but_Back_settingsMenu = (Button) findViewById(R.id.ttn_in_exit_button);
        but_Back_settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workSelectMenu.setVisibility(View.VISIBLE);
                workInTTN.setVisibility(View.INVISIBLE);
            }
        });
        //save
    }
    private void initUImainMenu()
    {
        //mainMenu UI listeners

        Button but_work_mainMenu = (Button) findViewById(R.id.but_Work);
        but_work_mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeRefreshLayout.setRefreshing(true);
                try {
                    testPostRequestDocList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mainMenu.setVisibility(View.INVISIBLE);
                workSelectMenu.setVisibility(View.VISIBLE);
            }
        });

        Button but_settings_mainMenu = (Button) findViewById(R.id.but_Settings);
        but_settings_mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //активация установок
                TextView tx = findViewById(R.id.testConnection_text_OK);
                tx.setVisibility(View.INVISIBLE);

                mainMenu.setVisibility(View.INVISIBLE);
                settingsMenu.setVisibility(View.VISIBLE);
            }
        });

        Button but_test_mainMenu = (Button) findViewById(R.id.but_TestMark);
        but_test_mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //активация установок
                TextView tx = findViewById(R.id.testConnection_text_OK);
                tx.setVisibility(View.INVISIBLE);

                mainMenu.setVisibility(View.INVISIBLE);
                testLayout.setVisibility(View.VISIBLE);
            }
        });

        Button but_exit_mainMenu = (Button) findViewById(R.id.but_Exit);
        but_exit_mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregisterReceiver(br);
                finishAndRemoveTask();
            }
        });
    }

    private void initUIsettingsMenu()
    {
        //settingsMenu UI listeners
        //back
        Button but_Back_settingsMenu = (Button) findViewById(R.id.but_Exit2);
        but_Back_settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainMenu.setVisibility(View.VISIBLE);
                settingsMenu.setVisibility(View.INVISIBLE);
            }
        });
        //save
        Button but_Save_settingsMenu = (Button) findViewById(R.id.but_SaveSettings);
        but_Save_settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity host = getActivity(view);
                SharedPreferences sharedPref = host.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                TextView tx = (TextView) findViewById(R.id.serverAddr);
                ServerURL = tx.getText().toString();

                TextView tx2 = (TextView) findViewById(R.id.TerminalName);
                nameTSD = tx2.getText().toString();

                editor.putString("server_url", ServerURL);
                editor.putString("name_tsd", nameTSD);

                editor.commit();

                mainMenu.setVisibility(View.VISIBLE);
                settingsMenu.setVisibility(View.INVISIBLE);
            }
        });
        //test
        Button but_Test_settingsMenu = (Button) findViewById(R.id.but_TestSettings);
        but_Test_settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testRequest();
            }
        });
    }

    private void filterListdocs()
    {
        Switch filter1 = (Switch) findViewById(R.id.switchFilter1);
        Switch filter2 = (Switch) findViewById(R.id.switchFilter2);
        Switch filter3 = (Switch) findViewById(R.id.switchFilter3);

        dcfiltered.clear();
        for(int i=0;i<dc.size();i++)
        {
            docitemclass dcone = dc.get(i);
            if(filter3.isChecked())
                dcfiltered.add(dcone);
            else if(filter2.isChecked())
            {
                if(dcone.type==2)
                {
                    dcfiltered.add(dcone);
                }
            }
            else if(filter1.isChecked())
            {
                if(dcone.type==1)
                {
                    dcfiltered.add(dcone);
                }
            }
        }
    }

    private void initUItestMark()
    {
        //testMark UI listeners
        //back
        Button but_Back_testMark= (Button) findViewById(R.id.but_Exit_testLayout);
        but_Back_testMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainMenu.setVisibility(View.VISIBLE);
                testLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initUIlistDocs()
    {
        //doclist UI listeners
        //back
        Button but_Back_testMark= (Button) findViewById(R.id.but_Exit_doclistLayout);
        but_Back_testMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainMenu.setVisibility(View.VISIBLE);
                workSelectMenu.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initScanner()
    {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String barcode = intent.getStringExtra(PARAM_RESULT);
                switch (getActiveLayout())
                {
                    case 2:
                        try {
                            getBarcodeForTestLayout(barcode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                            break;
                }
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }


    private void getBarcodeForTestLayout(String barcode) throws JSONException {
        testPostRequest(barcode);
    }

    private int getActiveLayout()
    {
        if(mainMenu.getVisibility()==View.VISIBLE)
            return 0;
        if(settingsMenu.getVisibility()==View.VISIBLE)
            return 1;
        if(testLayout.getVisibility()==View.VISIBLE)
            return 2;
        if(workSelectMenu.getVisibility()==View.VISIBLE)
            return 3;

        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSettings();

        initUImainMenu();
        initUIsettingsMenu();
        initUItestMark();
        initUIlistDocs();
        initUIDocTTN();
        initScanner();

    }

    public void testRequest()
    {

        TextView tx = (TextView) findViewById(R.id.serverAddr);
        ServerURL = tx.getText().toString();

        TextView tx2 = (TextView) findViewById(R.id.TerminalName);
        nameTSD = tx2.getText().toString();


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerURL+"/testconnection.php";

    // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textView.setText("Response is: "+ response.substring(0,500));
                        TextView tx = findViewById(R.id.testConnection_text_OK);
                        tx.setText(response);
                        tx.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView tx = findViewById(R.id.testConnection_text_OK);
                tx.setText("Нет связи с сервером");
                tx.setVisibility(View.VISIBLE);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void testPostRequest(String mark) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        int operation=0;
        if(mark.length()==150)
        {
            jsonBody.put("code", mark.substring(0,21));
            jsonBody.put("operation",4);
            operation=4;
            TextView txm = (TextView) findViewById(R.id.test_mark);
            txm.setText(mark);
        }
        else {
            jsonBody.put("code", mark);
            jsonBody.put("operation", 3);
            operation=3;
            TextView txm = (TextView) findViewById(R.id.test_box);
            txm.setText(mark);
        }
        final String requestBody = jsonBody.toString();
        final int operation_f = operation;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerURL+"/testmmk_get.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("VOLLEY", response);
                TextView tx  = (TextView) findViewById(R.id.test_box);
                TextView txst  = (TextView) findViewById(R.id.test_box_status);
                TextView txmkst  = (TextView) findViewById(R.id.test_mark_status);
                LinearLayout LMark = (LinearLayout) findViewById(R.id.LMark);
                TextView txm_alconame  = (TextView) findViewById(R.id.test_alconame);
                TextView txm_pt  = (TextView) findViewById(R.id.test_alco_partyname);
                TextView txm_formbname  = (TextView) findViewById(R.id.test_alco_formbname);

                try {
                    JSONObject json = new JSONObject(response);
                    if(operation_f==3) //Только марка
                    {
                        LMark.setVisibility(View.VISIBLE);
                        tx.setText(json.getString("box"));
                        txst.setText("");
                        txm_alconame.setText(json.getString("n")+"/ Объем:"+json.getString("v"));
                        txm_formbname.setText(json.getString("fb"));
                        txm_pt.setText(json.getString("pt"));
                        int status = json.getInt("status");
                        if(status==0)
                        {
                            txmkst.setText("Резерв");
                        }
                        else
                        {
                            txmkst.setText("Свободна");
                        }
                    }
                    if(operation_f==4)
                        {
                            txst.setText(json.getString("scanned")+"/"+json.getString("all"));
                            txmkst.setText("");
                            txm_alconame.setText(json.getString("n")+"/ Объем:"+json.getString("v"));
                            txm_formbname.setText(json.getString("fb"));
                            txm_pt.setText(json.getString("pt"));
                            LMark.setVisibility(View.GONE);

                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }


public void testPostRequestDocList() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        int operation=0;

            jsonBody.put("interval", 25);
            jsonBody.put("operation",1);
            operation=1;

        final String requestBody = jsonBody.toString();
        final int operation_f = operation;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ServerURL+"/get_doclist.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.i("VOLLEY", response);

                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray docsArray = json.getJSONArray("docs");
                    dc.clear();
                    for (int i = 0; i < docsArray.length(); i++) {
                        JSONObject jo_inside = docsArray.getJSONObject(i);
                        String date = jo_inside.getString("date1c");
                        date = date.substring(0,4)+"."+date.substring(4,6)+"."+date.substring(6,8);
                        docitemclass dci = new docitemclass(jo_inside.getString("num1c"),date,jo_inside.getInt("type"),jo_inside.getString("client"),
                                jo_inside.getString("doc_GUID"),jo_inside.getInt("status"));
                        dc.add(dci);
                    }
                    mAdapter.clearItems();
                    filterListdocs();
                    mAdapter.setItems(dcfiltered);
                    mSwipeRefreshLayout.setRefreshing(false);
                    layoutManager.scrollToPosition(dcfiltered.size()-1);
                    Activity ac = getActivity(workSelectMenu);
                    SharedPreferences sharedPref = ac.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("lastworklist", response);
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
                Snackbar.make(workSelectMenu, "Ошибка работы с интернет. Попытка восстановить последний список документов", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                Activity ac = getActivity(workSelectMenu);
                SharedPreferences sharedPref = ac.getPreferences(Context.MODE_PRIVATE);
                String response = sharedPref.getString("lastworklist", "");
                if(response.length()>0) {

                    JSONObject json = null;
                    try {
                        json = new JSONObject(response);

                        JSONArray docsArray = json.getJSONArray("docs");
                        dc.clear();
                    for (int i = 0; i < docsArray.length(); i++) {
                        JSONObject jo_inside = docsArray.getJSONObject(i);

                        String date = jo_inside.getString("date1c");
                        date = date.substring(0,4)+"."+date.substring(4,6)+"."+date.substring(6,8);
                        docitemclass dci = new docitemclass(jo_inside.getString("num1c"),date,jo_inside.getInt("type"),jo_inside.getString("client"),
                                jo_inside.getString("doc_GUID"),jo_inside.getInt("status"));
                        dc.add(dci);
                    }
                        mAdapter.clearItems();
                        mAdapter.setItems(dc);
                        filterListdocs();
                        mAdapter.setItems(dcfiltered);
                        mSwipeRefreshLayout.setRefreshing(false);
                        layoutManager.scrollToPosition(dcfiltered.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

        };

        queue.add(stringRequest);
    }
}
