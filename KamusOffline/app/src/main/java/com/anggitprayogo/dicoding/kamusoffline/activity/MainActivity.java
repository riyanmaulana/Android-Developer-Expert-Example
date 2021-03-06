package com.anggitprayogo.dicoding.kamusoffline.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.anggitprayogo.dicoding.kamusoffline.R;
import com.anggitprayogo.dicoding.kamusoffline.db.KamusHelper;
import com.anggitprayogo.dicoding.kamusoffline.entity.Kamus;
import com.anggitprayogo.dicoding.kamusoffline.prefrence.AppPrefrence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_logo)
    ImageView ivLogo;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    AppPrefrence appPrefrence;
    KamusHelper kamusHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        appPrefrence = new AppPrefrence(this);
        kamusHelper = new KamusHelper(this);

        new LoadDataIdToEn().execute();
    }

    class LoadDataIdToEn extends AsyncTask<Void, Integer, Void> {

        double progress;
        double maxProgress = 100;

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean isFirstRun = appPrefrence.isAppFirstRun();

            if (isFirstRun) {

                ArrayList<Kamus> kamusIdToEn = preLoadData(0);
                ArrayList<Kamus> kamusEnToId = preLoadData(1);

                progress = 30;
                publishProgress((int) progress);
                Double progressMaxInsert = 80.0;
                Double progressDiff = (progressMaxInsert - progress) / kamusIdToEn.size();

                kamusHelper.open();

                kamusHelper.beginTransaction();

                for (Kamus kamus : kamusIdToEn) {
                    kamusHelper.insertTransaction(kamus, 0);
                    progress += progressDiff;
                    publishProgress((int) progress);
                }

                progress = 30;
                publishProgress((int) progress);
                progressMaxInsert = 80.0;
                progressDiff = (progressMaxInsert - progress) / kamusEnToId.size();

                for (Kamus kamus : kamusEnToId) {
                    kamusHelper.insertTransaction(kamus, 1);
                    progress += progressDiff;
                    publishProgress((int) progress);
                }

                kamusHelper.setTransaction();

                kamusHelper.endTransaction();

                kamusHelper.close();

                appPrefrence.setAppFirstRun(false);

            } else {
                try {
                    Thread.sleep(1000);
                    Intent intent = new Intent(MainActivity.this, TranslateActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent i = new Intent(MainActivity.this, TranslateActivity.class);
            startActivity(i);
            finish();
        }
    }

    private ArrayList<Kamus> preLoadData(int i) {
        ArrayList<Kamus> kamuses = new ArrayList<>();
        String line = null;
        BufferedReader reader;

        InputStream inputStream = null;

        if (i == 0) {
            inputStream = getResources().openRawResource(R.raw.indonesia_english);
        } else {
            inputStream = getResources().openRawResource(R.raw.english_indonesia);
        }

        reader = new BufferedReader(new InputStreamReader(inputStream));
        int count = 0;

        try {

            do {

                line = reader.readLine();

                String[] splitData = line.split("\t");

                Kamus kamus = new Kamus();
                kamus.setKosakata(splitData[0]);
                kamus.setDeskripsi(splitData[1]);
                kamuses.add(kamus);
                count++;

            } while (line != null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return kamuses;

    }
}
