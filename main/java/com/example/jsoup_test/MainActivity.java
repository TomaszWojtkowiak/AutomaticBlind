package com.example.jsoup_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import org.joda.time.DateTimeZone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout mainView;
    private TextView textview;
    private Button btnUp;
    private Button btnDown;
    private ImageButton btnSettings;

    private String ip = "192.168.0.16";
    private int port = 4210;

    final Handler handler = new Handler();
    final int delay = 5000; // 1000 milliseconds == 1 second
    String sunsetTime = "";
    String dawnTime = "";
    int dayOfMonth = 0;


    int blindLength = -1;
    boolean autoWorkOn = false;

    String file = "blindConfigData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = (ConstraintLayout) findViewById(R.id.mainLayout);
        textview = (TextView) findViewById(R.id.feedbackText);
        btnUp = (Button) findViewById(R.id.buttonUp);
        btnDown = (Button) findViewById(R.id.buttonDown);
        btnSettings = (ImageButton) findViewById(R.id.btnSettings);

        String fileData = readFromStorage(mainView);
        parseData(fileData);

        Intent intent = getIntent();

        handler.postDelayed(new Runnable() {
            public void run() {
                DateTime dt = new DateTime(DateTimeZone.forOffsetHours(2));
                int minute = dt.getMinuteOfHour();
                int hour = dt.getHourOfDay();
                if(hour == 17 && minute == 41 && autoWorkOn)
                {

                }
                handler.postDelayed(this, delay);
            }
        }, delay);

        btnUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    sendUdp('U');
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    sendUdp('H');
                }
                return false;
            }
        });

        btnDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    sendUdp('D');
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    sendUdp('H');
                }
                return false;
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                goToSettings(view);
            }
        });
    }

    private void goToSettings(View view)
    {
        Intent intent = new Intent(MainActivity.this, settings.class);
        startActivity(intent);
        finish();
    }

    private void checkHour()
    {

    }

    public void getWebsite() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                final StringBuilder builder = new StringBuilder();

                try {
                    String sunsetTime = "Today blinds will close at: ";
                    Document doc = Jsoup.connect("https://www.timeanddate.com/sun/poland/poznan").get();
                    String title = doc.title();
                    Elements els = doc.select("tr");

                    builder.append(title).append("\n");

                    for (Element el : els) {
                        builder.append("\n").append("Attr : ").append(el.attr("tr"))
                                .append("\n").append("Text : ").append(el.text());
                        if(el.text().contains("Sunset Today:"))
                        {
                            int index = el.text().indexOf(':') + 2;
                            sunsetTime += el.text().substring(index, index+5);
                            textview.setText(sunsetTime);
                            break;
                        }
                    }
                } catch (IOException e) {
                    builder.append("Error: ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getBaseContext(),builder.toString(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void sendUdp(char data) {
        new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    DatagramSocket udpSocket = new DatagramSocket(port);
                    InetAddress serverAddr = InetAddress.getByName(ip);
                    byte[] buf = (String.valueOf(data)).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                    udpSocket.send(packet);
                    udpSocket.close();
                    textview.append(String.valueOf(data) + ' ');
                } catch(SocketException e){
                    Log.e("Udp:", "Socket Error:", e);
                } catch (IOException e){
                    Log.e("Udp send:", "IO Error:", e);
                }
            }
        }).start();
    }

    public String readFromStorage(View view)
    {
        try
        {
            FileInputStream fin = openFileInput(file);
            int c;
            String temp="";
            while((c = fin.read()) != -1)
            {
                temp = temp + Character.toString((char) c);
            }
            Toast.makeText(getBaseContext(),"File read!",Toast.LENGTH_SHORT).show();
            textview.append(temp + '\n');
            return temp;
        }
        catch(Exception e)
        {
            return "FILE NOT READ";
        }
    }

    public void parseData(String _data) {
        int j = 0;
        boolean ipSet = false, lengthSet = false;
        String temp = "";
        int beginIndex = 0;
        _data += '\0';
        while (_data.charAt(j) != '\0') {
            if (_data.charAt(j) == '\n' && !ipSet) {
                ip = _data.substring(beginIndex, j);
                beginIndex = j + 1;
                ipSet = true;
            }
            else if (_data.charAt(j) == '\n' && !lengthSet) {
                blindLength = parseInt(_data.substring(beginIndex, j));
                beginIndex = j + 1;
                lengthSet = true;
            }
            else if(ipSet && lengthSet)
            {
                temp += _data.charAt(j);
            }
            j++;
        }
        if(temp.equals("TRUE"))
        {
            autoWorkOn = true;
        }
        else if(temp.equals("FALSE"))
        {
            autoWorkOn = false;
        }
    }
}