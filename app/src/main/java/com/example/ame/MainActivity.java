package com.example.ame;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    String curr = "";
    EditText editText;
    ListView listView;
    LinearLayout linearLayout;
    DataHandler dataHandler;

    Context ctx;

    ArrayList<String> data;
    ArrayList<int[]> map;

    ArrayList<String> words;
    ArrayList<String> selected;

    CustomAdapter<String> adapter;
    LinearLayout.LayoutParams layoutParams;
    ProgressBar progressBar;
    boolean easterEgg = true;

    final float textSize = 20;

    TextView remaining;

    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseInput();
        initialiseSelected();
        initialiseList();
        setUpProgressBar();
        getData();
    }

    void initialiseInput() {
        editText = findViewById(R.id.input);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setCurrent(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    void initialiseSelected() {
        linearLayout = findViewById(R.id.selected);
        setRemaining();
    }

    void initialiseList() {
        listView = findViewById(R.id.list);
        words = new ArrayList<>();
        selected = new ArrayList<>();
        ctx = getApplicationContext();
        adapter = new CustomAdapter<>(words, ctx, this);
        listView.setAdapter(adapter);
    }

    void setUpProgressBar() {
        progressBar = findViewById(R.id.progress_loader);
    }

    void setRemaining() {
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = 10;
        layoutParams.rightMargin = 10;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;

        remaining = new TextView(this);
        if(curr == null) curr = "";
        remaining.setText(curr);
        remaining.setLayoutParams(layoutParams);
        remaining.setPadding(20, 10, 20, 10);
        remaining.setTextSize(textSize);
        remaining.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.noback));
        linearLayout.addView(remaining);
    }

    void getData() {
        dataHandler = new DataHandler(ctx);
        data = new ArrayList<>();
            getDataFromAssets("data_small.txt");
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), this::getDataFromURI);
    }


    void getDataFromURI(Uri uri) {
        new Thread() {
            @Override
            public void run() {
                showProgressBar();
                dataHandler.uri = uri;
                setData(dataHandler.get(DataHandler.FROM_URI));
                String fileName = getFileName(uri);
                String toastText = "Dataset from file " + fileName + " loaded";
                if(data.size() == 0) {
                    toastText = "Failed to load data from file " + fileName;
                } else {
                    dataHandler.write(fileName, data);
                }
                Log.d("log", "run: "+fileName);
                hideProgressBar(toastText);
            }
        }.start();
    }

    void getDataFromAssets(String assetName) {
        new Thread() {
            @Override
            public void run() {
                showProgressBar();
                dataHandler.assetName = assetName;
                setData(dataHandler.get(DataHandler.FROM_ASSETS));
                String toastText = "Default dataset Loaded";
                if(assetName.equals("data_large.txt"))
                    toastText = "Large dataset loaded";
                hideProgressBar(toastText);

            }
        }.start();
    }

    void getDataFromFile(String fileName) {
        new Thread() {
            @Override
            public void run() {
                showProgressBar();
                dataHandler.fileName = fileName;
                setData(dataHandler.get(DataHandler.FROM_FILE));
                String toastText = "Dataset from file " + fileName + " loaded";
                if(data.size() == 0) {
                    toastText = "Failed to load data from file " + fileName;
                }
                hideProgressBar(toastText);
            }
        }.start();
    }

    public String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int ind = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (ind >= 0)
                        result = cursor.getString(ind);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    void showProgressBar() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    void hideProgressBar(String toastText) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast toast=Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    void setData(ArrayList<String> d) {
        data = d;
        createMapping();
        Log.d("result", "setData: " + data.size());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        String path = getApplicationContext().getFilesDir().getPath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files != null) {
            Log.d("Files", "Size: " + files.length);
            for (File file : files) {
                Log.d("Files", "FileName:" + file.getName());
                menu.add(Menu.NONE, 1, Menu.NONE, file.getName());
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("TAG", "menuItem: " + item.getTitle() + item.getItemId());
        String title = item.getTitle().toString();
        int id = item.getItemId();
        if(id == R.id.small) {
            getDataFromAssets("data_small.txt");
        } else if(id == R.id.large) {
            getDataFromAssets("data_large.txt");
        } else if(id == R.id.newFile) {
            addTextFile();
        } else {
            getDataFromFile(title);
        }
        return true;
    }

    void addTextFile() {
        if(mGetContent == null) {
            Log.d("ERROR", "addTextFile: No mGetContext");
            return;
        }
        mGetContent.launch("text/plain");
    }


    void setCurrent(String s) {
        if(data.size() == 0) return;
        curr = "";
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<s.length();i++)
            if((int)s.charAt(i) <= 'z' && (int)s.charAt(i) >= 'a')
                sb.append(s.charAt(i));
        curr = sb.toString();
        Log.d("Input", "setCurrent: " + curr);
        clear();
        calculate("");
    }

    void clear() {
        words.clear();
        selected.clear();
        linearLayout.removeAllViewsInLayout();
        linearLayout.addView(remaining);
        adapter.notifyDataSetChanged();
    }

    void accept(String s) {
        if(linearLayout == null) {
            Log.d("ERROR", "accept: no linearLayout");
        }
        TextView textView = new TextView(this);
        textView.setText(s);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(textSize);
        textView.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.back));
        linearLayout.addView(textView);
        textView.setOnClickListener(view -> reject((TextView) view));
        if(s.equals("🐤")) {
            easterEgg = false;
            s = "";
        }
        calculate(s);
    }

    void reject(TextView view) {
        linearLayout.removeView(view);
        String s = view.getText().toString();
        if(s.equals("🐤")) return;
        curr += view.getText().toString();
        calculate("");
    }

    void calculate(String toSub) {
        if(words == null) {
            words = new ArrayList<>();
        }
        if(adapter == null) {
            Log.d("ERROR", "calculate: no Adapter");
            return;
        }
        for(int i=0;i<toSub.length();i++) {
            int ind = curr.indexOf(toSub.charAt(i));
            curr = curr.substring(0, ind).concat(curr.substring(ind + 1));
        }
        remaining.setText(curr);
        ArrayList<String> lt = new ArrayList<>();
        int[] arr = mapString(curr);
        for(int i=0;i<data.size();i++) {
            if(lessThanEqual(map.get(i), arr))
                lt.add(data.get(i));
        }
        Collections.sort(lt, (a, b) -> Integer.compare(b.length(), a.length()));
        words.clear();

        if(curr.equals("anagram") && easterEgg) {
            lt.add(0, "🐤");
        }
        words.addAll(lt);
        Log.d("size", "calculate: " + words.size() + (words.size() > 0 ? words.get(0) : ""));
        adapter.notifyDataSetChanged();
    }

    boolean lessThanEqual(int[] a, int[] b) {
        if(a.length != b.length) {
            Log.d("ERROR", "lessThanEqual: length of array a and b are not equal");
        }
        for(int i=0;i<Math.min(a.length, b.length);i++) {
            if(a[i] > b[i]) return false;
        }
        return true;
    }

    void createMapping() {
        if(data == null) return;
        map = new ArrayList<>();
        for(String t : data) {
            map.add(mapString(t));
        }
    }
    int[] mapString(String s) {
        int[] arr = new int[26];
        for(int i=0;i<s.length();i++) {
            int ind = s.charAt(i) - 'a';
            if(ind >= 0 && ind <= 25)
                arr[ind]++;
        }
        return arr;
    }
}