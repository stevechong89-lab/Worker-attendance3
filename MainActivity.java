package com.example.workerattendance;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 100;
    private TextView tvStatus;
    private TextView tvEmployee;
    private ImageView ivQr;

    private final ArrayList<Record> records = new ArrayList<>();

    // Demo account data. 下一版接云端后会改成真正的员工/工头资料。
    private final String employeeId = "EMP001";
    private final String employeeName = "Ali";
    private final String foreman = "Ahmad";
    private final String worksite = "Putra Heights";

    private String currentState = "未上班";
    private long workStart = 0L;
    private long lunchOut = 0L;
    private long lunchBack = 0L;

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> scanner =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    tvStatus.setText("取消扫描");
                    return;
                }
                handleScan(result.getContents());
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        tvEmployee = findViewById(R.id.tvEmployee);
        ivQr = findViewById(R.id.ivQr);

        Button btnForeman = findViewById(R.id.btnForeman);
        Button btnEmployee = findViewById(R.id.btnEmployee);
        Button btnRecords = findViewById(R.id.btnRecords);

        btnForeman.setOnClickListener(v -> openScanner());
        btnEmployee.setOnClickListener(v -> showEmployeeQr());
        btnRecords.setOnClickListener(v -> showRecords());

        tvStatus.setText("工头：Ahmad｜工地：Putra Heights");
    }

    private void openScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION
            );
            return;
        }

        ScanOptions options = new ScanOptions();
        options.setPrompt("请扫描员工 QR");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanner.launch(options);
    }

    private void showEmployeeQr() {
        ivQr.setVisibility(View.VISIBLE);
        String data = employeeId + "|" + employeeName;
        Bitmap bitmap = createQr(data, 800);
        if (bitmap != null) {
            ivQr.setImageBitmap(bitmap);
        }
        tvEmployee.setText(
                "员工：" + employeeName +
                "\n员工编号：" + employeeId +
                "\n把这个 QR 给工头扫描"
        );
        tvStatus.setText("员工 QR 已显示");
    }

    private Bitmap createQr(String text, int size) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.QR_CODE, size, size
            );
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y)
                            ? 0xFF000000
                            : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (Exception e) {
            Toast.makeText(this, "QR 生成失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void handleScan(String contents) {
        if (!contents.startsWith(employeeId)) {
            tvStatus.setText("QR 无效：不是本系统员工");
            Toast.makeText(this, "员工 QR 不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        String action;

        switch (currentState) {
            case "未上班":
                workStart = now;
                currentState = "已上班";
                action = "上班";
                break;

            case "已上班":
                lunchOut = now;
                currentState = "午休中";
                action = "午休出去";
                break;

            case "午休中":
                lunchBack = now;
                currentState = "已回来";
                action = "午休回来";
                break;

            case "已回来":
                action = "下班";
                currentState = "已下班";
                break;

            default:
                tvStatus.setText("今天已经完成下班打卡");
                return;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(now));

        records.add(new Record(employeeName, foreman, worksite, action, time));

        String message = employeeName + "｜" + action + "｜" + time;
        tvStatus.setText(message);
        Toast.makeText(this, action + " 成功", Toast.LENGTH_SHORT).show();

        if ("下班".equals(action)) {
            long lunchMinutes = 0;
            if (lunchOut > 0 && lunchBack > lunchOut) {
                lunchMinutes = (lunchBack - lunchOut) / 60000;
            }
            long totalMinutes = (now - workStart) / 60000 - lunchMinutes;
            tvEmployee.setText(
                    "今天实际工作时间：" + formatMinutes(totalMinutes) +
                    "\n午休：" + lunchMinutes + " 分钟"
            );
        }
    }

    private String formatMinutes(long minutes) {
        if (minutes < 0) minutes = 0;
        long h = minutes / 60;
        long m = minutes % 60;
        return h + " 小时 " + m + " 分钟";
    }

    private void showRecords() {
        if (records.isEmpty()) {
            tvEmployee.setText("今天还没有打卡记录");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("今天记录\n\n");
        for (Record r : records) {
            sb.append(r.employee)
                    .append("｜")
                    .append(r.action)
                    .append("\n工头：")
                    .append(r.foreman)
                    .append("｜工地：")
                    .append(r.worksite)
                    .append("\n")
                    .append(r.time)
                    .append("\n\n");
        }
        tvEmployee.setText(sb.toString());
    }

    private static class Record {
        final String employee;
        final String foreman;
        final String worksite;
        final String action;
        final String time;

        Record(String employee, String foreman, String worksite, String action, String time) {
            this.employee = employee;
            this.foreman = foreman;
            this.worksite = worksite;
            this.action = action;
            this.time = time;
        }
    }
}
