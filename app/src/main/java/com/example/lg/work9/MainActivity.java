package com.example.lg.work9;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    EditText url;
    WebView webView;
    ListView list;
    LinearLayout url_space,panel;
    Handler mHandler = new Handler();
    Animation animTop,animBottom;
    ProgressDialog dialog;
    ArrayList<Url> favor_list = new ArrayList<>();
    ArrayAdapter<Url> adapter;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    void init(){
        webviewinit();
        animinit();

        setTitle("WebView");
        imm  = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        adapter = new ArrayAdapter<Url>(this,
                android.R.layout.simple_list_item_1,favor_list);
        dialog = new ProgressDialog(this);
        url = (EditText)findViewById(R.id.url);
        list = (ListView)findViewById(R.id.favorite);
        url_space = (LinearLayout)findViewById(R.id.url_space);
        panel = (LinearLayout)findViewById(R.id.panel);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제확인")
                        .setMessage("삭제하시겠습니까?")
                        .setNegativeButton("취소",null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                favor_list.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        }).show();
                return true;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                url_space.startAnimation(animBottom);
                webView.setVisibility(View.VISIBLE);
                list.setVisibility(View.GONE);
                webView.loadUrl(favor_list.get(position).url);
                url.setText(favor_list.get(position).url);
            }
        });
        url.setText("http://naver.com");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"즐겨찾기추가");
        menu.add(0,2,1,"즐겨찾기목록");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){//asset의 html파일을 열어줌
            list.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl("file:///android_asset/www/urladd.html");
            if(url_space.getVisibility()==View.VISIBLE) {
                url_space.startAnimation(animTop);
            }
        }
        else if(item.getItemId() == 2){//즐겨찾기목록을 보여줌
            if(url_space.getVisibility()==View.VISIBLE) {
                url_space.startAnimation(animTop);
            }
            webView.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    class JSMethods{

        @JavascriptInterface //즐겨찾기추가를 했을 때 즐겨찾기 정보를 가져오는 method
        public void getData(final String name, String url){
            url = urlCheck(url);
            final String cmp_url = url;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    boolean flag = true;
                    for(Url now : favor_list){
                        if(now.getUrl().equals(cmp_url)){
                            flag = false;
                            break;
                        }
                    }
                    if(flag) {
                        favor_list.add(new Url(name, cmp_url));
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        webView.loadUrl("javascript:displayMsg()");//자바스크립트 method사용방법
                    }
                }
            });
        }
        @JavascriptInterface
        public void vi_url(){ //url보이기를 눌렀을 때 url_space를 보여주게 하는 method
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(url_space.getVisibility()==View.GONE) {
                        url_space.startAnimation(animBottom);
                        Log.d("visible",Integer.toString(View.VISIBLE)+" "+Integer.toString(url_space.getVisibility()));
                    }
                }
            });
        }
    }
    public void animinit(){
        animTop = AnimationUtils.loadAnimation(this,R.anim.translate_top);//url_space사라지는 애니매이션
        animTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        url_space.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animBottom = AnimationUtils.loadAnimation(this,R.anim.translate_bottom); //url_space등장 애니메이션
        animBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        url_space.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void webviewinit(){
        webView = (WebView)findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); //자바스크립트사용
        webSettings.setSupportZoom(true); //확대사용
        webView.addJavascriptInterface(new JSMethods(),"MyApp"); //자바스크립트인터페이스를 추가
        webView.setWebViewClient(new WebViewClient(){
            @Override //아래의 메소드가 없으면 기기내의 브라우저를 사용하여 웹페이지를 연다.(꼭 써주자)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override //웹페이지를 열때 loading중이라는 메세지를 띄울 dialog
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.setMessage("Loading....");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override //웹페이지 loading이 끝나면 dialog를 없애줌
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress >= 100) dialog.dismiss();
            }

            @Override //자바스크립트를 사용할 때 경고를 띄워줌
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }
        });
        webView.loadUrl("http://naver.com"); //url을 webview로 load해줌
    }

    public void onMyClick(View v){
        if(v.getId() == R.id.go){
            webView.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            String connect = getText(url);
            url.setText(connect);
            webView.loadUrl(connect);
            imm.hideSoftInputFromWindow(url.getWindowToken(),0); //키보드 내려주기
        }
    }
    @Override //아래의 메소드는 뒤로가기버튼으로 전 페이지로 돌아가는 기능을 원할때 씁니다.
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if(webView.getVisibility()==View.GONE){
                        list.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                    }
                    else {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    String getText(EditText e){ //url 가져오기
        String url = e.getText().toString();
        url = urlCheck(url);
        return url;
    }
    String urlCheck(String url){ //url의 포맷에 맞는지 확인하고 아니면 변환
        if (!(url.contains("http://") || url.contains("https://"))) {
            url = "http://" + url;
        }
        return url;
    }
}
