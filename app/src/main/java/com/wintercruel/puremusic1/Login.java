package com.wintercruel.puremusic1;

import static com.wintercruel.puremusic1.cloud.Net.GetPlayList;
import static com.wintercruel.puremusic1.cloud.Net.GetUser;
import static com.wintercruel.puremusic1.cloud.Net.SaveUser;
import static com.wintercruel.puremusic1.cloud.Net.client;
import static com.wintercruel.puremusic1.cloud.Net.initialize;
import static com.wintercruel.puremusic1.cloud.Net.login;
import static com.wintercruel.puremusic1.cloud.Net.sendCaptcha;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.suke.widget.SwitchButton;
import com.wintercruel.puremusic1.cloud.Net;
import com.wintercruel.puremusic1.cloud.server;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.entity.User;
import com.wintercruel.puremusic1.event_bus.LoginSuccessUpdateUI;
import com.wintercruel.puremusic1.tools.TransparentBar;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    private EditText PhoneNumber;
    private EditText Code;
    private ImageButton GetCode;
    private ImageButton Login;
    private ImageView imageView;
    private SwitchButton switchButton;
    private String qrKey = "your_generated_key";  // 替换为实际生成的key
    private boolean isPolling = true;
    private TextView Reminder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        TransparentBar.transparentNavBar(this);
        TransparentBar.transparentStatusBar(this);
        PhoneNumber=findViewById(R.id.PhoneNumber);
        Code=findViewById(R.id.AuthCode);
        GetCode=findViewById(R.id.GetAuthCode);
        Login=findViewById(R.id.LoginButton);
        imageView=findViewById(R.id.imageView);
        Reminder=findViewById(R.id.remind);



        initialize(Login.getContext());


        GetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber= String.valueOf(PhoneNumber.getText());
                new Thread(()->{
                    if(MusicHolder.isPlayerMode()){

                    }else {
                        sendCaptcha(phoneNumber, Login.getContext());
                    }

                }).start();

            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber= String.valueOf(PhoneNumber.getText());
                String authCode=String.valueOf(Code.getText());
                new Thread(()->{
                    if(MusicHolder.isPlayerMode()){

                    }else {
                        login(phoneNumber,authCode, Login.getContext());
                    }


                }).start();

            }
        });

        GetQRCode();
        pollQrCodeStatus();

    }

    private void GetQRCode(){
        OkHttpClient client = new OkHttpClient();
        String url = server.ADDRESS+"/login/qr/key";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                // Handle failure here
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println(responseData);
                    try {
                        JSONObject jsonObject=new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        String key=dataObject.getString("unikey");
                        GetBase(key);
                        qrKey=key;

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    // Handle error here
                    System.out.println("Request failed with status code: " + response.code());
                }
            }
        });

    }

    private void GetBase(String key) {
        OkHttpClient client = new OkHttpClient();
        String url = server.ADDRESS+"/login/qr/create?key=" + key;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                // Handle failure here
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println(responseData);

                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        String QRImage = dataObject.getString("qrurl");

                        // 使用runOnUiThread确保UI更新在主线程中进行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                    Bitmap bitmap = barcodeEncoder.encodeBitmap(QRImage, BarcodeFormat.QR_CODE, 400, 400);
                                    imageView.setImageBitmap(bitmap);
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Request failed with status code: " + response.code());
                }
            }
        });
    }


    private void pollQrCodeStatus() {
        new Thread(() -> {
            while (isPolling) {
                try {
                    // 构建请求URL
                    String url = server.ADDRESS + "/login/qr/check?key=" + qrKey;

                    // 构建请求
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    // 执行请求
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d("QRCodeCheck", "收到二维码检查响应: " + responseBody);
                        handleQrCodeResponse(responseBody);
                    } else {
                        Log.e("QRCodeCheck", "Request failed: " + response.code());
                    }

                    // 每隔一定时间轮询一次
                    Thread.sleep(1000); // 2秒轮询一次

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void handleQrCodeResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            int code = jsonObject.getInt("code");

            switch (code) {
                case 800:
                    runOnUiThread(()->{
                        Reminder.setText("二维码已过期，请重新生成");
                    });
                    break;

                case 801:
                    runOnUiThread(()->{
                        Reminder.setText("等待扫码");
                    });
                    break;

                case 802:
                    runOnUiThread(()->{
                        Reminder.setText("等待确认");
                    });
                    break;

                case 803:
                    runOnUiThread(()->{
                        Reminder.setText("授权登录成功");
                    });
                    isPolling = false; // 停止轮询

                    // 处理返回的 cookies
                    // 你可以在这里做后续的操作，比如保存用户信息等
                    System.out.println("登录结果："+responseBody);
                    getUserAccount();
                    break;

                default:
                    Log.e("QRCodeCheck", "Unknown status code: " + code);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getUserAccount(){
        Request request = new Request.Builder()
               .url(server.ADDRESS+"/user/account")
               .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                System.out.println("用户信息："+responseData);
                User user= null;
                try {
                    user = GetUser(responseData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                SaveUser(user, Login.getContext());
                GetPlayList(Login.getContext());
//                Index.isLogin =true;
                EventBus.getDefault().postSticky(new LoginSuccessUpdateUI());
            }
        });


    }

}