package com.example.jsoup_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class settings extends AppCompatActivity {

    private TextView ipaddress;
    private TextView blindLength;
    private TextView txt_sunsetTime;
    private Switch autoWrkToggle;

    private LinearLayout settingsView;
    String data = "";
    String file = "blindConfigData";
    boolean settingsSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        blindLength = (TextView) findViewById(R.id.lengthInput);
        ipaddress = (TextView) findViewById(R.id.ipInput);
        autoWrkToggle = (Switch) findViewById(R.id.autoWorkToggle);
        settingsView = (LinearLayout) findViewById(R.id.settingsView);
        String data = readFromStorage(settingsView);
        parseData(data);
        settingsSaved = false;
    }

    public void goToMainView(View view)
    {
        Intent intent = new Intent(settings.this, MainActivity.class);

        startActivity(intent);
        finish();
    }

    public void writeToStorage(View view)
    {
        data = ipaddress.getText().toString() + '\n' + blindLength.getText().toString() + '\n' + (autoWrkToggle.isChecked() ? "TRUE" : "FALSE");
        try
        {
            FileOutputStream fOut = openFileOutput(file,Context.MODE_PRIVATE);
            fOut.write(data.getBytes());
            fOut.close();
            settingsSaved = true;
            Toast.makeText(getBaseContext(),"File saved!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        goToMainView(view);
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
                ipaddress.setText(_data.substring(beginIndex, j));
                beginIndex = j + 1;
                ipSet = true;
            }
            else if (_data.charAt(j) == '\n' && !lengthSet) {
                blindLength.setText(_data.substring(beginIndex, j));
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
            autoWrkToggle.setChecked(true);
        }
        else if(temp.equals("FALSE"))
        {
            autoWrkToggle.setChecked(false);
        }
    }


}