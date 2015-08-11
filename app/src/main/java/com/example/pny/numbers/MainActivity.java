package com.example.pny.numbers;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class MainActivity extends ActionBarActivity {
    private int rangeStart = 0;
    private int rangeEnd = 0;
    private int magicNumber = 0;
    private ArrayList<Integer> rangeList = new ArrayList<>(Arrays.asList(5, 10, 15, 20, 25, 40, 50, 75, 100, 200, 500, 1000, 5000, 10000, 100000, 1000000, 10000000));
    protected ArrayList<Integer> previousNumbers = new ArrayList();
    public void addNumber(View view) {
        EditText numberInput = (EditText)findViewById(R.id.numberOutput);

        numberInput.append(view.getTag().toString());
    }

    public void deleteNumber(View view) {
        EditText numberInput = (EditText)findViewById(R.id.numberOutput);
        String numberText = numberInput.getText().toString();

        if (numberText.length() == 1)
            numberInput.setText("");
        else
            numberInput.setText(numberText.substring(0, numberText.length() - 1));

    }

    public void submitNumber(View view) throws IOException {
        EditText numberInput = (EditText)findViewById(R.id.numberOutput);
        String numberText = numberInput.getText().toString();

        TextView congratsView = (TextView)findViewById(R.id.congrats);
        if (Integer.parseInt(numberText) == magicNumber) {
            congratsView.setText("congrats");
            numberInput.setText("");
            increaseRange();
            new File(getFilesDir(), "previousNumbers").delete();
            previousNumbers = new ArrayList<>();

        } else if (previousNumbers.contains(Integer.parseInt(numberText))) {
            congratsView.setText("number already guessed");
            numberInput.setText("");
        } else {
            congratsView.setText("");
            congratsView.setText("try again");
            numberInput.setText("");

            previousNumbers.add(Integer.parseInt(numberText));
            File myfile = getFileStreamPath("previousNumbers");

            try {
                if (myfile.exists() || myfile.createNewFile()) {
                    //May run into issues here when getting into large lists with fast guesses
                    FileOutputStream fos = openFileOutput("previousNumbers", MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(previousNumbers);
                    oos.close();
                    fos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void restart(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.restartdialog);

        Button yes = (Button)dialog.findViewById(R.id.yesButton);
        Button no = (Button)dialog.findViewById(R.id.noButton);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rangeEnd = 5;
                setRandomNumber();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                new File(getFilesDir(), "previousNumbers").delete();
                previousNumbers = new ArrayList();

                editor.putInt("rangeEnd", rangeEnd);
                editor.putInt("magicNumber", magicNumber);
                editor.commit();

                TextView range = (TextView)findViewById(R.id.rangeText);
                range.setText("Range: 0 to " + rangeEnd);

                TextView congratsView = (TextView)findViewById(R.id.congrats);
                congratsView.setText("");

                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

    public void getPreviousNumbers() throws ClassNotFoundException {
        File myfile = getFileStreamPath("previousNumbers");
        try {
            if(myfile.exists()){
                FileInputStream fis = this.openFileInput("previousNumbers");
                ObjectInputStream ois = new ObjectInputStream(fis);
                previousNumbers = (ArrayList) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void increaseRange() {
        int index = rangeList.indexOf(rangeEnd);
        TextView congratsView = (TextView)findViewById(R.id.congrats);

        if (index == rangeList.size()) {
            congratsView.setText("congrats, you win");
        } else {
            rangeEnd = rangeList.get(index+1);
            setRandomNumber();
            TextView range = (TextView)findViewById(R.id.rangeText);
            range.setText("Range: 0 to " + rangeEnd);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt("rangeEnd", rangeEnd);
            editor.putInt("magicNumber", magicNumber);
            editor.commit();
        }
    }

    public void setRandomNumber() {

        Random r = new Random();
        magicNumber = r.nextInt(rangeEnd - rangeStart) + rangeStart;
    }

    public void setCurrentRange() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getInt("rangeEnd", -1) > -1) {

            rangeEnd = preferences.getInt("rangeEnd", 1);
            magicNumber = preferences.getInt("magicNumber", 0);

        } else {
            SharedPreferences.Editor editor = preferences.edit();

            rangeEnd = 5;
            setRandomNumber();
            editor.putInt("rangeEnd", rangeEnd);
            editor.putInt("magicNumber", magicNumber);
            editor.commit();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCurrentRange();

        try {
            getPreviousNumbers();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        TextView range = (TextView)findViewById(R.id.rangeText);
        range.setText("Range: 0 to " + rangeEnd);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
