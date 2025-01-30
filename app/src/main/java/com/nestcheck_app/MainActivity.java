package com.nestcheck_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREF = "sharedPrefsNeu";

    public static final String SAVE_STRING = "SaveNeu";


    ActivityResultLauncher<Intent> launcher;
    List<Nestbox> listNestbox = new ArrayList<>();
    List<Nestbox> listReceived;
    ListView listViewData;
    ArrayAdapter<String> adapter;

    // Not all Boxes exist
    public static final String[] SELECTED_BOXES_STRING = {
            "Box 1", "Box 2", "Box 3","Box 4","Box 5", "Box 7", "Box 8", "Box 9", "Box 10",
            "Box 11", "Box 12", "Box 13", "Box 14", "Box 16", "Box 18", "Box 19", "Box 20",
            "Box 21", "Box 22", "Box 23", "Box 24", "Box 26", "Box 27", "Box 28", "Box 29", "Box 30",
            "Box 31", "Box 33", "Box 34", "Box 35", "Box 36", "Box 37", "Box 38", "Box 39", "Box 40",
            "Box 41", "Box 42", "Box 43", "Box 44", "Box 46", "Box 47", "Box 48", "Box 50",
            "Box 51", "Box 52", "Box 53", "Box 54", "Box 55", "Box 56", "Box 57", "Box 58", "Box 59", "Box 60",
            "Box 61", "Box 62", "Box 63", "Box 64", "Box 65", "Box 66", "Box 67", "Box 68", "Box 69", "Box 70",
            "Box 71", "Box 72", "Box 73", "Box 74", "Box 75", "Box 76", "Box 77", "Box 78", "Box 79", "Box 80",
            "Box 81", "Box 82", "Box 83", "Box 84", "Box 85", "Box 86", "Box 87", "Box 88", "Box 89", "Box 90",
            "Box 91", "Box 92", "Box 93", "Box 94", "Box 95", "Box 96", "Box 97", "Box 98", "Box 99", "Box 100",
            "Box 101", "Box 102", "Box 103", "Box 104", "Box 105","Box 200",
            "Box 201", "Box 202", "Box 203", "Box 204", "Box 205", "Box 206", "Box 207", "Box 208", "Box 209", "Box 210",
            "Box 211", "Box 212", "Box 213", "Box 214", "Box 215", "Box 216", "Box 217", "Box 218", "Box 219", "Box 220",
            "Box 221", "Box 222", "Box 223", "Box 224", "Box 225", "Box 226", "Box 227", "Box 228", "Box 229", "Box 230",
            "Box 231", "Box 232", "Box 233", "Box 234", "Box 235", "Box 236", "Box 237", "Box 238", "Box 239", "Box 240",
            "Box 241", "Box 242", "Box 243", "Box 244", "Box 245", "Box 246", "Box 247", "Box 248", "Box 249", "Box 250",
            "Box 251"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Menu"); // Name der Angezeigt wird der Aktivity

        listViewData = findViewById(R.id.listViewID);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, SELECTED_BOXES_STRING);
        listViewData.setAdapter(adapter);

        // Pass Data between Main Activity and ResultActivity
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if ( data != null) {
                    listReceived = data.getParcelableArrayListExtra("key");
                    //Load Result if applicable
                    if(listReceived != null && !listReceived.isEmpty()) {
                        Intent iResult = new Intent(this, ResultsActivity.class);
                        iResult.putParcelableArrayListExtra("NestboxesToResult", new ArrayList<>(listReceived));
                        startActivity(iResult);
                    }
                    else{
                        Toast.makeText(this, "No saved Results in Memory", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void SaveDataNeu(View v){
        //create ArrayList of Nestboxes for each checked box
        if(listViewData.getCount()==0){
            Toast.makeText(this, "No Boxes selected - unable to save", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i=0; i<listViewData.getCount(); i++){
                if(listViewData.isItemChecked(i)){
                    String nestboxString = SELECTED_BOXES_STRING[i];
                    String nestboxNummerString = nestboxString.replaceAll("[^0-9]", "");
                    int nestboxNummerInt = Integer.parseInt(nestboxNummerString);
                    listNestbox.add(new Nestbox(nestboxNummerInt)); //maybe +0
                }
            }
        }
        listViewData.clearChoices();
        listViewData.requestLayout();

        //Save List with Json/Gson to sharedPrefNeu.
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonString = gson.toJson(listNestbox);

        editor.putString(SAVE_STRING, jsonString);
        editor.apply();

        Toast.makeText(this, "New List saved!", Toast.LENGTH_SHORT).show();
    }

    public void LaunchMapNew (View v){
        //load last saved Data
        LoadDataFromMemory();
        if(!listNestbox.isEmpty()) {
            Intent i = new Intent(this, MapActivity.class);
            i.putParcelableArrayListExtra("NestboxesToCheckListKey", new ArrayList<>(listNestbox));
            startActivity(i);
        }
    }

    public void LoadDataFromMemory(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(SAVE_STRING, null);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Nestbox>>(){}.getType();
        if(jsonString != null) {
            listNestbox = gson.fromJson(jsonString, listType);
        }
        else{
            Toast.makeText(this, "No Boxlist saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void LaunchResults (View v) {
        //Launch Map, grab Nestboxlist, close map ->registerForActivityResult
        Intent iMap = new Intent(this, MapActivity.class);
        iMap.putExtra("GetNestbox", true);
        launcher.launch(iMap);
    }
}