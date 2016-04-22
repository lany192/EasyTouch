package com.lany.easytouch.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.lany.easytouch.R;
import com.lany.easytouch.service.AuxiliaryService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        findViewById(R.id.show_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoAppDetailSetting();
                startService(new Intent(MainActivity.this, AuxiliaryService.class));
                // new TableShowView(this).fun(); 如果只是在activity中启动
                // 当activity跑去后台的时候[暂停态，或者销毁态] 我们设置的显示到桌面的view也会消失
                // 所以这里采用的是启动一个服务，服务中创建我们需要显示到table上的view，并将其注册到windowManager上
                finish();
            }
        });
    }

    private void gotoAppDetailSetting() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings",
                    "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName",
                    getPackageName());
        }
        startActivity(intent);
    }
}
