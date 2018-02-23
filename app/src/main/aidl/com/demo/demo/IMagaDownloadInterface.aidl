package com.demo.demo;


interface IMagaDownloadInterface {
     void start(String itemId,String url);

     void pause(String itemId);

     int handleIfRunning(String itemId);


     int getRunningCount();
}
