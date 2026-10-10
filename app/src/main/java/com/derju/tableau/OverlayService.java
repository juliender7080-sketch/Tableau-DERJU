package com.derju.tableau;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class OverlayService extends Service {
    private WindowManager wm; private LinearLayout panel; private WindowManager.LayoutParams params;
    private final int[] counts = new int[13]; private int total=0, mode=0;
    private float downX,downY; private int startX,startY; private TextView summary,detail; private GridLayout grid;
    private int dp(float v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    @Override public void onCreate() {
        super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE); buildPanel();
        params=new WindowManager.LayoutParams(dp(310),WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START; params.x=dp(18); params.y=dp(100); wm.addView(panel,params);
    }
    private GradientDrawable shape(int color,int stroke,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); d.setStroke(dp(1),stroke); return d;
    }
    private TextView text(String s,int size,int color) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); t.setGravity(Gravity.CENTER); return t;
    }
    private Button smallButton(String s,int border) {
        Button b=new Button(this); b.setText(s); b.setTextSize(11); b.setAllCaps(false); b.setTextColor(0xFFFFFFFF);
        b.setPadding(dp(3),0,dp(3),0); b.setBackground(shape(0xEE20294D,border,10)); return b;
    }
    private void buildPanel() {
        panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(10),dp(8),dp(10),dp(10));
        panel.setBackground(shape(0xE9101630,0xFF5D6BFF,18)); panel.setElevation(dp(12));
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("⚄ DERJU  •  DÉS",17,0xFF8BEAFF); brand.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(brand,new LinearLayout.LayoutParams(0,dp(42),1));
        Button close=smallButton("×",0xFFFA70D9); header.addView(close,new LinearLayout.LayoutParams(dp(42),dp(38)));
        close.setOnClickListener(v->stopSelf());
        header.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();startX=params.x;startY=params.y;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){params.x=startX+(int)(e.getRawX()-downX);params.y=startY+(int)(e.getRawY()-downY);wm.updateViewLayout(panel,params);return true;}
            return true;
        }); panel.addView(header);
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER);
        Button minus=smallButton("−",0xFF8A75FF),size=smallButton("TAILLE",0xFF55DFFF),plus=smallButton("+",0xFF55DFFF);
        controls.addView(minus,new LinearLayout.LayoutParams(0,dp(36),1)); controls.addView(size,new LinearLayout.LayoutParams(0,dp(36),1.5f));
        controls.addView(plus,new LinearLayout.LayoutParams(0,dp(36),1)); panel.addView(controls);
        minus.setOnClickListener(v->resize(0.85f)); plus.setOnClickListener(v->resize(1.15f));
        size.setOnClickListener(v->{mode=(mode+1)%3;updateMode();});
        summary=text("Lancers : 0",13,0xFFFFFFFF); panel.addView(summary,new LinearLayout.LayoutParams(-1,dp(34)));
        detail=text("Fréquences observées et théoriques",11,0xFFBFC8FF); panel.addView(detail,new LinearLayout.LayoutParams(-1,dp(28)));
        ScrollView scroll=new ScrollView(this); grid=new GridLayout(this); grid.setColumnCount(3); scroll.addView(grid);
        panel.addView(scroll,new LinearLayout.LayoutParams(-1,dp(220)));
        for(int sum=2;sum<=12;sum++){ final int s=sum; Button b=new Button(this); b.setTextSize(11); b.setAllCaps(false); b.setTextColor(0xFFFFFFFF);
            b.setBackground(shape(0xCC1D2850,(sum%2==0?0xFF52DFFF:0xFFB66CFF),12));
            GridLayout.LayoutParams gp=new GridLayout.LayoutParams(); gp.width=dp(88); gp.height=dp(58); gp.setMargins(dp(2),dp(2),dp(2),dp(2)); grid.addView(b,gp);
            b.setOnClickListener(v->{counts[s]++;total++;updateStats();}); }
        LinearLayout bottom=new LinearLayout(this); Button undo=smallButton("−1",0xFFB66CFF),reset=smallButton("EFFACER",0xFFFA70D9);
        bottom.addView(undo,new LinearLayout.LayoutParams(0,dp(36),1)); bottom.addView(reset,new LinearLayout.LayoutParams(0,dp(36),2)); panel.addView(bottom);
        undo.setOnClickListener(v->{if(total>0){for(int s=12;s>=2;s--)if(counts[s]>0){counts[s]--;total--;break;}updateStats();}});
        reset.setOnClickListener(v->{for(int i=0;i<counts.length;i++)counts[i]=0;total=0;updateStats();}); updateStats();
    }
    private void updateStats(){
        summary.setText(String.format(Locale.FRANCE,"Lancers : %d",total));
        int[] ways={0,0,1,2,3,4,5,6,5,4,3,2,1};
        for(int i=0;i<grid.getChildCount();i++){int s=i+2;Button b=(Button)grid.getChildAt(i);
            double obs=total==0?0:100.0*counts[s]/total, th=ways[s]*100.0/36.0;
            b.setText(String.format(Locale.FRANCE,"%d\n%d (%.1f%%)\nTh. %.1f%%",s,counts[s],obs,th));}
    }
    private void resize(float f){params.width=Math.max(dp(190),Math.min(dp(430),(int)(params.width*f)));wm.updateViewLayout(panel,params);}
    private void updateMode(){
        detail.setVisibility(mode==0?View.VISIBLE:View.GONE); grid.setVisibility(mode==2?View.GONE:View.VISIBLE);
        params.width=dp(mode==0?310:mode==1?250:190); panel.setAlpha(mode==2?0.88f:0.97f); wm.updateViewLayout(panel,params);
    }
    @Override public int onStartCommand(Intent i,int flags,int id){return START_STICKY;}
    @Override public void onDestroy(){if(panel!=null)try{wm.removeView(panel);}catch(Exception ignored){} super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
