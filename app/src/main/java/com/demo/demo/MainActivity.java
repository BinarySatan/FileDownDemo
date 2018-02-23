package com.demo.demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ServiceConnection mConnection;
    private IMagaDownloadInterface mDownloadInterface;
    Button task1;
    Button task2;
    Button task3;
    Button task4;
    Button task5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        task1 = findViewById(R.id.task1);
        task1.setOnClickListener(this);
        task2 = findViewById(R.id.task2);
        task2.setOnClickListener(this);
        task3 = findViewById(R.id.task3);
        task3.setOnClickListener(this);
        task4 = findViewById(R.id.task4);
        task4.setOnClickListener(this);
        task5 = findViewById(R.id.task5);
        task5.setOnClickListener(this);


        startDownloadService();
    }

    private void startDownloadService() {
        Intent intent = new Intent(this, MagaDownloadService.class);
        startService(intent);
        bindService(intent, mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mDownloadInterface = IMagaDownloadInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, BIND_AUTO_CREATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        String itemId;
        String url;
        String text;
        switch (v.getId()) {
            case R.id.task1:
                itemId = "626166";
                url = "http://s.mrxinzhi.com//magazine/626166.pdf";
                text = task1.getText().toString();
                break;
            case R.id.task2:
                itemId = "626304";
                url = "http://s.mrxinzhi.com//magazine/626304.pdf";
                text = task2.getText().toString();
                break;
            case R.id.task3:
                itemId = "623580";
                url = "http://s.mrxinzhi.com//magazine/623580.pdf";
                text = task3.getText().toString();
                break;
            case R.id.task4:
                itemId = "619885";
                url = "http://s.mrxinzhi.com//magazine/619885.pdf";
                text = task4.getText().toString();
                break;
            case R.id.task5:
                itemId = "626298";
                url = "http://s.mrxinzhi.com//magazine/626298.pdf";
                text = task5.getText().toString();
                break;
            default:
                itemId = "626166";
                url = "http://s.mrxinzhi.com//magazine/626166.pdf";
                text = task1.getText().toString();
                break;
        }


        if (text.contains("任务") || text.contains("暂停")) {
            try {
                mDownloadInterface.start(itemId, url);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (text.contains("%")) {
            try {
                mDownloadInterface.pause(itemId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "下载完成", 0).show();
        }


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDownload(MagaDownloadEntity downloadEntity) {

        TextView tv;
        switch (downloadEntity.itemId) {
            case "626166":
                tv = task1;
                break;
            case "626304":
                tv = task2;
                break;
            case "623580":
                tv = task3;
                break;
            case "619885":
                tv = task4;
                break;
            case "626298":
                tv = task5;
                break;
            default:
                tv = task1;
                break;
        }

        switch (downloadEntity.type) {
            case 0:
                int progress = (int) ((float) downloadEntity.soFarBytes / (float) downloadEntity.totalBytes * 100);
                tv.setText(progress + "%");
                break;
            case 1:
                tv.setText("下载完成");
                break;
            case 2:
            case 3:
                tv.setText("暂停");
                break;
        }
    }
}
