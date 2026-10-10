package com.derju.tableau;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24),dp(24),dp(24),dp(24)); root.setBackgroundColor(0xFF080B18);
        TextView logo = new TextView(this); logo.setText("⚄  DERJU  ⚅"); logo.setTextSize(32);
        logo.setGravity(Gravity.CENTER); logo.setTextColor(0xFF75E7FF); logo.setShadowLayer(dp(14),0,0,0xFF8A48FF);
        root.addView(logo,new LinearLayout.LayoutParams(-1,dp(80)));
        TextView title = new TextView(this); title.setText("TABLEAU DE STATISTIQUES"); title.setTextSize(16);
        title.setGravity(Gravity.CENTER); title.setTextColor(0xFFE7E9FF);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView desc = new TextView(this); desc.setText("Tableau flottant translucide • Néon bleu & violet\nSaisis les sommes et compare les fréquences.");
        desc.setTextSize(14); desc.setGravity(Gravity.CENTER); desc.setTextColor(0xFFB8C1E8);
        root.addView(desc,new LinearLayout.LayoutParams(-1,dp(80)));
        Button start = new Button(this); start.setText("LANCER LE TABLEAU FLOTTANT");
        root.addView(start,new LinearLayout.LayoutParams(-1,dp(56)));
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            } else { startService(new Intent(this,OverlayService.class)); finish(); }
        });
        TextView note = new TextView(this); note.setText("Détection automatique des dés non incluse dans cette version.");
        note.setTextSize(12); note.setGravity(Gravity.CENTER); note.setTextColor(0xFF8792B8);
        root.addView(note,new LinearLayout.LayoutParams(-1,dp(64))); setContentView(root);
    }
}
