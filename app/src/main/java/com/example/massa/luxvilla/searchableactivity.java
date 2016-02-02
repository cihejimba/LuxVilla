package com.example.massa.luxvilla;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.massa.luxvilla.Actividades.casaactivity;
import com.example.massa.luxvilla.adaptadores.adaptadorrvtodas;
import com.example.massa.luxvilla.adaptadores.adaptadorrvtodasoffline;
import com.example.massa.luxvilla.network.VolleySingleton;
import com.example.massa.luxvilla.sqlite.BDAdapter;
import com.example.massa.luxvilla.utils.RecyclerViewOnClickListenerHack;
import com.example.massa.luxvilla.utils.keys;
import com.example.massa.luxvilla.utils.listacasas;
import com.example.massa.luxvilla.utils.listasql;
import com.example.massa.luxvilla.utils.todascasas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class searchableactivity extends Activity implements RecyclerViewOnClickListenerHack {
    private RecyclerView rvc1;
    private adaptadorrvtodas adaptador;
    Toolbar barsearch;
    private RequestQueue requestQueue;
    private ArrayList<todascasas> casas=new ArrayList<>();
    private VolleySingleton volleySingleton;
    SwipeRefreshLayout swipeRefreshLayout;
    static String query;
    static ArrayList<listacasas> ids=new ArrayList();
    static BDAdapter adapter;
    private adaptadorrvtodasoffline adaptadoroffline;
    static Context ctxtodas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchableactivity);
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }


            volleySingleton=VolleySingleton.getInstancia(searchableactivity.this);
        requestQueue=volleySingleton.getRequestQueue();
        ctxtodas=searchableactivity.this;


        barsearch=(Toolbar)findViewById(R.id.brcimasearch);
        barsearch.setTitle(query);
        barsearch.setNavigationIcon(R.mipmap.ic_arrow_back_white_24dp);
        barsearch.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        rvc1 = (RecyclerView)findViewById(R.id.rv_search);
        rvc1.setLayoutManager(new LinearLayoutManager(searchableactivity.this));
        if (isNetworkAvailable(searchableactivity.this)) {

            adaptador=new adaptadorrvtodas(searchableactivity.this);
            rvc1.setAdapter(adaptador);

            sendjsonRequest();
        } else {
            adaptadoroffline=new adaptadorrvtodasoffline(searchableactivity.this,getdados());
            rvc1.setAdapter(adaptadoroffline);
        }

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipesearch);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable(searchableactivity.this)) {

                    adaptador=new adaptadorrvtodas(searchableactivity.this);
                    rvc1.setAdapter(adaptador);

                    sendjsonRequest();
                } else {
                    adaptadoroffline=new adaptadorrvtodasoffline(searchableactivity.this,getdados());
                    rvc1.setAdapter(adaptadoroffline);
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        rvc1.addOnItemTouchListener(new RecyclerViewTouchListener(searchableactivity.this, rvc1, this));

    }

    private void sendjsonRequest(){

        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, "http://brunomassa.esy.es/resultado.json", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                //Toast.makeText(getActivity(),"Resposta: "+response,Toast.LENGTH_LONG).show();
                casas=parsejsonResponse(response);
                adaptador.setCasas(casas);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    private ArrayList<todascasas> parsejsonResponse(JSONArray array){
        ArrayList<todascasas> casas=new ArrayList<>();
        ids.clear();
        if (array!=null||array.length()>0){
            for (int i=0;i<array.length();i++){
                try {
                    JSONObject casaexata=array.getJSONObject(i);
                    String id=casaexata.getString(keys.allkeys.KEY_ID);
                    String local=casaexata.getString(keys.allkeys.KEY_LOCAL);
                    String preco=casaexata.getString(keys.allkeys.KEY_PRECO);
                    String imgurl=casaexata.getString(keys.allkeys.KEY_IMGURL);
                    String info=casaexata.getString(keys.allkeys.KEY_INFO);
                    if (local.equalsIgnoreCase(query)){
                        todascasas casasadd=new todascasas();
                        casasadd.setLOCAL(local);
                        casasadd.setPRECO(preco);
                        casasadd.setIMGURL(imgurl);
                        listacasas cs=new listacasas();
                        cs.Local=local;
                        cs.Preço=preco;
                        cs.IMGurl=imgurl;
                        cs.info=info;
                        ids.add(0,cs);

                        casas.add(0,casasadd);
                    }

                    //Toast.makeText(getActivity(),casas.toString(),Toast.LENGTH_LONG).show();

                } catch (JSONException e) {

                }
            }

        }

        //Toast.makeText(getActivity(),casas.toString(),Toast.LENGTH_LONG).show();
        return casas;
    }

    @Override
    public void onClickListener(View view, int position) {
        List<listacasas> casas;
        casas=ids;
        listacasas cs=casas.get(position);
        Intent infocasa = new Intent(searchableactivity.this, casaactivity.class);
        infocasa.putExtra("localcasa", cs.Local);
        infocasa.putExtra("precocasa",cs.Preço);
        infocasa.putExtra("imgurl", cs.IMGurl);
        infocasa.putExtra("infocs", cs.info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View iv=view.findViewById(R.id.imgcasa);
            ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation(searchableactivity.this, Pair.create(iv, "elementimg"));
            searchableactivity.this.startActivity(infocasa, optionsCompat.toBundle());
        } else {
            startActivity(infocasa);
        }
    }


    @Override
    public void onLongPressClickListener(View view, int position) {

    }


    public static class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {
        private Context mContext;
        private GestureDetector mGestureDetector;
        private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

        public RecyclerViewTouchListener(Context c, final RecyclerView rv, RecyclerViewOnClickListenerHack rvoclh){
            mContext = c;
            mRecyclerViewOnClickListenerHack = rvoclh;

            mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    View cv = rv.findChildViewUnder(e.getX(), e.getY());

                    boolean fav = false;
                    if( cv instanceof CardView){
                        float x = ((RelativeLayout) ((CardView) cv).getChildAt(0)).getChildAt(3).getX();
                        float w = ((RelativeLayout) ((CardView) cv).getChildAt(0)).getChildAt(3).getWidth();
                        float y;// = ((RelativeLayout) ((CardView) cv).getChildAt(0)).getChildAt(3).getY();
                        float h = ((RelativeLayout) ((CardView) cv).getChildAt(0)).getChildAt(3).getHeight();

                        Rect rect = new Rect();
                        ((RelativeLayout) ((CardView) cv).getChildAt(0)).getChildAt(3).getGlobalVisibleRect(rect);
                        y = rect.top;

                        if( e.getX() >= x && e.getX() <= w + x && e.getRawY() >= y && e.getRawY() <= h + y ){
                            fav = true;
                        }
                    }


                    if(cv != null && mRecyclerViewOnClickListenerHack != null && !fav){
                        mRecyclerViewOnClickListenerHack.onClickListener(cv,
                                rv.getChildAdapterPosition(cv) );
                    }

                    return(true);
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {}
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static List<listasql> getdados(){

        List<listasql>dados=new ArrayList<>();

        adapter=new BDAdapter(ctxtodas);
        int colunas=adapter.numerodecolunas();
        ids.clear();


        for(int i=0;i<colunas;i++){
            listasql txtexato=new listasql();
            String locsqloffline=adapter.verlocais(String.valueOf(i+1));
            String precsqloffline=adapter.verprecos(String.valueOf(i+1));
            String infossqloffline=adapter.verinfos(String.valueOf(i+1));
            if (locsqloffline.equalsIgnoreCase(query)) {
                txtexato.Loc = locsqloffline;
                txtexato.Prec = precsqloffline;
                txtexato.Inf = infossqloffline;
                dados.add(0,txtexato);
                listacasas cs = new listacasas();
                cs.Local = locsqloffline;
                cs.Preço = precsqloffline;
                cs.info = infossqloffline;
                ids.add(0,cs);
            }else {

            }
        }
        return dados;
    }
}