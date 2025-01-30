package com.nestcheck_app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class DropboxRunnable implements Runnable {
    private String ACCESS_TOKEN = null;
    private File myFile;
    private Context context;
    private boolean upload_download_switch = false;

    public DropboxRunnable(String ACCESS_TOKEN, Context context){
        this.ACCESS_TOKEN = ACCESS_TOKEN;
        this.context = context;
    }

    public void setMyFile(File myFile) {
        this.myFile = myFile;
        upload_download_switch = true;        //File is set, so next Time upload instead of download
    }

    public File getMyFile(){
        return myFile;
    }



    @Override
    public void run() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/NestChecks").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        String filepath = context.getString(R.string.filePathString);

        //Download
        if(!upload_download_switch) {
            myFile = new File(context.getFilesDir(), "NestboxFile.xlsx");

            //Dowload File from Dropbox and save in the created File myFile
            try (OutputStream outputStream = new FileOutputStream(myFile);
                 DbxDownloader<FileMetadata> downloader = client.files().download(filepath)) {
                 downloader.download(outputStream);
            } catch (DbxException | IOException ignored) {}
        }
        //Upload
        else{
            //Upload File to Dropbox - notify user on problem eg internet connection lost
            try (FileInputStream inputStream = new FileInputStream(myFile)){
                client.files().uploadBuilder(filepath).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
                Looper.prepare();
                Toast.makeText(context, "Upload Finished!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            } catch(DbxException | IOException e){
                Log.d("DEBUG Exception", e.toString());
                Looper.prepare();
                Toast.makeText(context, "ERROR during Uploading", Toast.LENGTH_SHORT).show();
                if(e.toString().equals("com.dropbox.core.RateLimitException")){
                    Toast.makeText(context, "Request problem - Try again in 5 min!", Toast.LENGTH_SHORT).show();
                }
                Looper.loop();
            }
        }
    }

}
