package com.example.workerattendance;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends Activity {

    private LinearLayout root;
    private int workerState = 0;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result == null) {
                    toast("没有扫描结果");
                    return;
                }
                String contents = result.getContents();
                if (contents == null) {
                    toast("已取消扫码");
                    return;
                }
                showScanResult(contents);
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home();
    }

    private TextView text(String value, float size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(35, 45, 60));
        t.setPadding(20, 20, 20, 20);
        return t;
    }

    private Button button(String title, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(16);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 8, 0, 8);
        b.setLayoutParams(p);
        return b;
    }

    private void createBase(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25, 25, 25, 25);
        root.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView titleView = text(title, 24);
        titleView.setGravity(Gravity.CENTER);
        root.addView(titleView);
        setContentView(root);
    }

    private void home() {
        createBase("工友打卡系统");

        root.addView(text(
                "Android 测试版\n\n支持：员工二维码 + 工头扫码",
                18));

        root.addView(button("工头：扫码打卡", v -> foremanPage()));
        root.addView(button("员工：显示我的二维码", v -> employeePage()));
        root.addView(button("老板：查看今日记录", v -> adminPage()));
    }

    private void employeePage() {
        createBase("我的员工二维码");

        root.addView(text(
                "员工姓名：Ali\n员工编号：EMP001\n\n请把这个二维码给工头扫描",
                18));

        TextView qr = text(
                "\n\n████████████\n██  ██  ████\n██ ████ ██  \n████  ██████\n██ ██ ██  ██\n████████████\n\n\n员工编号：EMP001",
                20);
        qr.setGravity(Gravity.CENTER);
        root.addView(qr);

        root.addView(text(
                "正式版本会自动生成真正 QR Code。\n没有手机的员工也可以使用打印二维码卡。",
                15));

        root.addView(button("返回", v -> home()));
    }

    private void foremanPage() {
        createBase("工头扫码");

        root.addView(text(
                "工头：Ahmad\n工头编号：S001\n\n当前工地：Putra Heights",
                18));

        root.addView(button("打开摄像头扫描员工 QR", v -> startScanner()));

        root.addView(text(
                "\n系统自动判断：\n\n" +
                "未上班 → 上班\n\n" +
                "已上班 → 午休出去\n\n" +
                "午休中 → 午休回来\n\n" +
                "已回来 → 下班\n\n" +
                "异常状态会提示。",
                16));

        root.addView(button("返回", v -> home()));
    }

    private void startScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    100);
            return;
        }

        ScanOptions options = new ScanOptions();
        options.setPrompt("请把员工 QR Code 放入扫描框");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanLauncher.launch(options);
    }

    private void showScanResult(String code) {
        createBase("扫描成功");

        root.addView(text(
                "二维码内容：\n" + code +
                "\n\n员工：Ali\n员工编号：EMP001\n\n" +
                "工头：Ahmad\n工地：Putra Heights",
                18));

        String nextAction;
        if (workerState == 0) nextAction = "上班";
        else if (workerState == 1) nextAction = "午休出去";
        else if (workerState == 2) nextAction = "午休回来";
        else if (workerState == 3) nextAction = "下班";
        else nextAction = "今天已经完成打卡";

        root.addView(text("系统判断下一步：\n\n" + nextAction, 22));

        if (workerState < 4) {
            root.addView(button("确认：" + nextAction, v -> confirmAttendance()));
        } else {
            root.addView(text("今天已经完成全部打卡", 18));
        }

        root.addView(button("取消", v -> foremanPage()));
    }

    private void confirmAttendance() {
        String action;
        if (workerState == 0) action = "上班";
        else if (workerState == 1) action = "午休出去";
        else if (workerState == 2) action = "午休回来";
        else action = "下班";

        workerState++;

        Toast.makeText(this, "记录成功：" + action, Toast.LENGTH_LONG).show();
        foremanPage();
    }

    private void adminPage() {
        createBase("老板 · 今日出勤");

        root.addView(text("今日出勤概况", 22));

        root.addView(text(
                "员工：Ali\n工头：Ahmad\n工地：Putra Heights\n\n" +
                "目前状态：" + getWorkerStateText() + "\n\n" +
                "后续版本会自动计算：\n" +
                "• 上班时间\n• 午休开始\n• 午休回来\n• 下班时间\n" +
                "• 实际工作小时\n• 迟到\n• 早退\n• 午休超过 1 小时\n• 漏打卡",
                17));

        root.addView(button("返回", v -> home()));
    }

    private String getWorkerStateText() {
        switch (workerState) {
            case 0: return "未上班";
            case 1: return "已上班";
            case 2: return "午休中";
            case 3: return "已回来";
            case 4: return "已下班";
            default: return "未知";
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                toast("需要允许摄像头权限才能扫码");
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
