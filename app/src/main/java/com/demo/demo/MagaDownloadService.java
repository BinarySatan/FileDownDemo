package com.demo.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import com.demo.demo.utils.PathUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.Set;

/**
 * Author:BinarySatan
 * Time: 2018/2/2
 */

public class MagaDownloadService extends Service {

    private static final String TAG = "mrxz_download";

    private ArrayMap<String, BaseDownloadTask> mDownTasks = new ArrayMap<>();
    private boolean _stoped;
    private ArrayMap<String, ProgressInfo> mProgresses = new ArrayMap<>();
    private Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Set<String> key = mDownTasks.keySet();
            Iterator<String> iterator = key.iterator();
            while (iterator.hasNext()) {
                String k = iterator.next();
                BaseDownloadTask baseDownloadTask = mDownTasks.get(k);
                ProgressInfo progressInfo = mProgresses.get(k);
                if (progressInfo != null) {
                    switch (baseDownloadTask.getStatus()) {
                        case FileDownloadStatus.pending:
                        case FileDownloadStatus.connected:
                        case FileDownloadStatus.started:
                        case FileDownloadStatus.retry:
                            progressInfo.otherTime += 1000;
                            break;
                        case FileDownloadStatus.progress:
                            progressInfo.time += 1000;
                            break;
                    }
                    Log.i(TAG,"time=" + progressInfo.time +
                            "\notheTime=" + progressInfo.otherTime +
                            "\nstatus=" + baseDownloadTask.getStatus() + "\n" + k);
                    if (progressInfo.time >= 15000 || progressInfo.otherTime >= 30000) {
//                        Logger.t(TAG).i("restart " + k);
                        progressInfo.pause_start = true;
                        baseDownloadTask.pause();
//                        baseDownloadTask.reuse();
//                        baseDownloadTask.start();
                    }
                }
            }
            mH.sendEmptyMessageDelayed(0x10, 1000);
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        mH.sendEmptyMessageDelayed(0x10, 1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileDownloader.setup(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        _stoped = true;
        Set<String> key = mDownTasks.keySet();
        Iterator<String> iterator = key.iterator();
        while (iterator.hasNext()) {
            mDownTasks.get(iterator.next()).pause();
        }
        mDownTasks.clear();
        mH.removeCallbacksAndMessages(null);
        mH = null;
        super.onDestroy();
    }

    private void startDownload(final String itemId, final String url) {
        if (mDownTasks.get(itemId) != null) return;


        FileDownloader.getImpl().create(url)
                .setPath(PathUtils.getMagazinePdfPath(itemId)).setAutoRetryTimes(5)
                .setListener(new FileDownloadListener() {

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.i(TAG,"pending  " + itemId);
                        mDownTasks.put(itemId, task);
                        mProgresses.put(itemId, new ProgressInfo(soFarBytes, 0, 0));
                        EventBus.getDefault().post(new MagaDownloadEntity(itemId, 0, soFarBytes, totalBytes));
                    }

                    @Override
                    protected void started(BaseDownloadTask task) {
                        Log.i(TAG,"started  " + itemId);
                    }


                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        Log.i(TAG,"connected " + itemId);
                        mDownTasks.put(itemId, task);
                        mProgresses.put(itemId, new ProgressInfo(soFarBytes, 0, 0));
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress = (int) ((float) soFarBytes / (float) totalBytes * 100);
                        Log.i(TAG,"progress =" + progress + " " + itemId);
                        if (mProgresses.get(itemId) == null || mProgresses.get(itemId).progress != soFarBytes) {
                            mProgresses.put(itemId, new ProgressInfo(soFarBytes, 0, 0));
                        }
                        EventBus.getDefault().post(new MagaDownloadEntity(itemId, 0, soFarBytes, totalBytes));
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        Log.i(TAG,"blockComplete " + itemId);
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        Log.i(TAG,"retry " + itemId);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.i(TAG,"completed " + itemId);
                        if (!_stoped) mDownTasks.remove(itemId);
                        EventBus.getDefault().post(new MagaDownloadEntity(itemId, 1, -1, -1));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.i(TAG,"pause " + itemId);
                        if (!_stoped) mDownTasks.remove(itemId);
                        EventBus.getDefault().post(new MagaDownloadEntity(itemId, 2, -1, -1));
                        mProgresses.remove(itemId);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.i(TAG,"error " + e.getMessage());
                        if (!_stoped) mDownTasks.remove(itemId);
                        EventBus.getDefault().post(new MagaDownloadEntity(itemId, 3, -1, -1));
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.i(TAG,"warn " + itemId);
                    }
                }).start();
    }


    private void stopDownload(String itemId) {
        BaseDownloadTask downloadTask = mDownTasks.get(itemId);
        if (downloadTask != null) downloadTask.pause();
    }


    class DownloadBinder extends IMagaDownloadInterface.Stub {


        @Override
        public void start(String itemId, String url) throws RemoteException {
            startDownload(itemId, url);
        }

        @Override
        public void pause(String itemId) throws RemoteException {
            stopDownload(itemId);
        }

        @Override
        public int handleIfRunning(String itemId) throws RemoteException {
            BaseDownloadTask downloadTask = mDownTasks.get(itemId);
            if (downloadTask != null) {
                int progress = (int) ((float) downloadTask.getSmallFileSoFarBytes() /
                        (float) downloadTask.getSmallFileTotalBytes() * 100);
                return progress;
            }

            return -1;
        }

        public int getRunningCount() throws RemoteException {
            return mDownTasks.size();
        }
    }


    class ProgressInfo {
        int progress;
        int time;
        int otherTime;
        boolean pause_start;

        public ProgressInfo(int progress, int time, int otherTime) {
            this.progress = progress;
            this.time = time;
            this.otherTime = otherTime;
        }
    }

}
