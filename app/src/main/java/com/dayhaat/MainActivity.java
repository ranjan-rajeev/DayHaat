package com.dayhaat;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    WebView webView;
    String url;
    String name;
    String title;
    SwipeRefreshLayout swipe;

    private DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("https://www.dayhaat.com/");
            }
        });


        swipe = findViewById(R.id.swipeContainer);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                        swipe.setRefreshing(false);
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        prepareListData();

        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Log.d("DEBUG", "submenu item clicked");
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                return false;
            }
        });


        webView = (WebView) findViewById(R.id.webView);
        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("NAME");
        title = getIntent().getStringExtra("TITLE");

        // check for notification data
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //bundle must contain all info sent in "data" field of the notification
            url = bundle.getString("url");
        }


        if (url == null || url.equals(""))
            url = "https://www.dayhaat.com/";

        if (isNetworkConnected()) {
            settingWebview();
            startCountDownTimer();
        } else
            Snackbar.make(webView, "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("Books & Magazines");
        item1.setIconImg(android.R.drawable.ic_delete);
        // Adding data header
        listDataHeader.add(item1);

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("heading2");
        item2.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item2);

        ExpandedMenuModel item3 = new ExpandedMenuModel();
        item3.setIconName("heading3");
        item3.setIconImg(android.R.drawable.ic_delete);
        listDataHeader.add(item3);

        // Adding child data
        List<String> heading1 = new ArrayList<String>();
        heading1.add("Academic Books");
        heading1.add("Competitive Exams");
        heading1.add("Research Oriented");
        heading1.add("Tip of the Tongue");
        heading1.add("In-Depth Books");
        heading1.add("New/Trending Books");
        heading1.add("Show All");


        List<String> heading2 = new ArrayList<String>();
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");
        heading2.add("Submenu of item 2");

        listDataChild.put(listDataHeader.get(0), heading1);// Header, Child data
        listDataChild.put(listDataHeader.get(1), heading2);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cart) {
            webView.loadUrl("https://www.dayhaat.com/index.php?route=checkout/cart");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (isNetworkConnected()) {
            if (id == R.id.nav_academic) {
                webView.loadUrl("https://www.dayhaat.com/Academic");
            } else if (id == R.id.nav_home) {
                webView.loadUrl("https://www.dayhaat.com/");

            } else if (id == R.id.nav_accessory) {
                webView.loadUrl("https://www.dayhaat.com/accessories");

            } else if (id == R.id.nav_competitive) {
                webView.loadUrl("https://www.dayhaat.com/Academic-Books");

            } else if (id == R.id.nav_in_depth) {
                webView.loadUrl("https://www.dayhaat.com/In-Depth-Books");

            } else if (id == R.id.nav_Justfun) {
                webView.loadUrl("https://www.dayhaat.com/Just4Fun");

            } else if (id == R.id.nav_research) {
                webView.loadUrl("https://www.dayhaat.com/Research-Oriented");

            } else if (id == R.id.nav_tip_tongue) {
                webView.loadUrl("https://www.dayhaat.com/Tip-of-the-Tongue");

            } else if (id == R.id.nav_trending) {
                webView.loadUrl("https://www.dayhaat.com/new-trending-books");

            } else if (id == R.id.nav_orders) {
                webView.loadUrl("https://www.dayhaat.com/index.php?route=account/order");

            } else if (id == R.id.nav_cart) {
                webView.loadUrl("https://www.dayhaat.com/index.php?route=checkout/cart");

            } else if (id == R.id.nav_wishlist) {
                webView.loadUrl("https://www.dayhaat.com/index.php?route=account/wishlist");

            } else if (id == R.id.nav_account) {
                webView.loadUrl("https://www.dayhaat.com/index.php?route=account/account");

            }
        } else {
            Snackbar.make(webView, "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCountDownTimer() {
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                try {
                    cancelDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void settingWebview() {
        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString("kurlz");
        settings.setJavaScriptEnabled(true);

        settings.setBuiltInZoomControls(false);
        settings.setUseWideViewPort(false);
        settings.setJavaScriptEnabled(true);
        settings.setSupportMultipleWindows(false);

        settings.setLoadsImagesAutomatically(true);
        settings.setLightTouchEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);


      /*  MyWebViewClient webViewClient = new MyWebViewClient(this);
        webView.setWebViewClient(webViewClient);*/
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO show you progress image
                showDialog();
                // new ProgressAsync().execute();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO hide your progress image
                cancelDialog();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /*if (url.endsWith(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        //user does not have a pdf viewer installed
                        String googleDocs = "https://docs.google.com/viewer?url=";
                        webView.loadUrl(googleDocs + url);
                    }
                }*/
                return false;
            }
        });
        webView.getSettings().setBuiltInZoomControls(false);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "Android");
        Log.d("URL", url);

        if (url.endsWith(".pdf")) {
            webView.loadUrl("https://docs.google.com/viewer?url=" + url);
            //webView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
        } else {
            webView.loadUrl(url);
        }
        //webView.loadUrl(url);
    }


    private class ProgressAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            for (int i = 0; i < 8000000; i++) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            cancelDialog();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    class MyJavaScriptInterface {
        /*@JavascriptInterface
        public void AddNewMotorQuote() { //Android.AddNewMotorQuote();
            Intent intent;
            if (url.contains("buynowTwoWheeler")) {
                intent = new Intent(CommonWebViewActivity.this, TwoWheelerQuoteAppActivity.class);
            } else {
                intent = new Intent(CommonWebViewActivity.this, InputQuoteBottmActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        @JavascriptInterface
        public void AddNewHealthQuote() {//Android.AddNewHealthQuote();
            Intent intent;
            intent = new Intent(CommonWebViewActivity.this, HealthQuoteAppActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        @JavascriptInterface
        public void AddNewTermQuote() {//Android.AddNewTermQuote();
            Intent intent;
            intent = new Intent(CommonWebViewActivity.this, TermSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        @JavascriptInterface
        public void RedirectToHomepage() {//Android.RedirectToHomepage();
            Intent intent = new Intent(CommonWebViewActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }*/
    }

}
