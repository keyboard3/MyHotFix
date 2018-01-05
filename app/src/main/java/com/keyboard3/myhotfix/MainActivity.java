package com.keyboard3.myhotfix;

import android.app.Activity;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {
    Util util = new Util();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btnFix:
                try {
                    MyApplication.moveAsset(this, "path_dex.jar");
                    Toast.makeText(this, "已处理，请重新启动", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "处理失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.btnDelete:
                MyApplication.deltePatch(this, "path_dex.jar");
                Toast.makeText(this, "已处理，请重新启动", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOk:
                util.Toast(this);
                break;
            default:
        }
    }
}