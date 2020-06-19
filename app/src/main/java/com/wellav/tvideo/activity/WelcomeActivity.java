package com.wellav.tvideo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.wellav.tvideo.R;
import com.wellav.tvideo.entity.Channel;
import com.wellav.tvideo.entity.Channels;
import com.wellav.tvideo.sysconfig.SysConfig;
import com.wellav.tvideo.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends BaseActivity {

    //节目列表
    private Channels mChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            initLazyData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLazyData();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    protected void initLazyData() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    //String data = Utils.httpGetChannels(SysConfig.mChannelsUrl);
                    String data = SysConfig.mChannels;

                    //节目列表
                    mChannels = new Channels();
                    JSONObject channelsJson = new JSONObject(data);
                    JSONArray contentListJson = channelsJson.getJSONArray("contentList");
                    List<Channel> contentList = new ArrayList<Channel>();
                    if (contentListJson.length() > 0) {
                        for (int i = 0; i < contentListJson.length(); i++) {
                            JSONObject channel = new JSONObject(contentListJson.get(i).toString());
                            Channel c = new Channel();
                            c.setProvider(channel.getString("provider"));
                            c.setGuid(channel.getString("guid"));
                            c.setLevel(channel.getString("level"));
                            c.setType(channel.getString("type"));
                            c.setHits(channel.getInt("hits"));
                            c.setChannelId(channel.getInt("channelId"));
                            c.setCatchupDays(channel.getInt("catchupDays"));
                            c.setAllowRecord(channel.getBoolean("allowRecord"));
                            c.setProtocol(channel.getString("protocol"));
                            c.setName(channel.getString("name"));
                            c.setPlayUrl(channel.getString("playUrl"));
                            contentList.add(c);
                        }
                        mChannels.setContentList(contentList);
                    }

                    Thread.sleep(1000);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                if (b) {
                    if (mChannels.getContentList().size() != 0) {
                        // 加载成功
                        Intent inte = new Intent(WelcomeActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("channels", mChannels);
                        inte.putExtras(bundle);
                        startActivity(inte);
                    } else {
                        Toast.makeText(WelcomeActivity.this, "no channel", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(WelcomeActivity.this, "Request error, code:", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }.execute();
    }

}
