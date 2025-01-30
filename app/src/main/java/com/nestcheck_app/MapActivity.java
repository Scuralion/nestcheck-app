package com.nestcheck_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ortiz.touchview.TouchImageView;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity {
    public static final String SHARED_PREF_MAP = "sharedPrefsMap";
    public static final String SAVE_STRING_MAP = "SaveMap";
    public static final String SAVE_STRING_MAP_PREV_DATA = "COMPARE";
    private static final String[] ITEMS_NESTBOX = {"E", "I", "L", "U", "O", "Eggs AND/OR Sampling", "CC", "CU", "WU", "Chics","Something else"};
    private List<Nestbox> listNestboxMap = new ArrayList<>();
    private List<Nestbox> listNestboxMapInMemory = new ArrayList<>();
    private Button[] nestboxButtonArray = null;
    private Button[] nestboxButtonArrayChosen = null;
    private TouchImageView zivMapNestbox;
    private String CharacterCoordinate = "";
    private String NumberCoordinate = "";
    private Intent intent;
    private float[][] originalButtonCoordinates;
    private int currentNestboxIndex;

    public String currentBoxnummerString;
    public NumberPicker numberPicker;
    public CheckBox checkBoxSampling;
    public CheckBox checkBoxBT;
    public CheckBox checkBoxGT;
    private ListView neststateListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        intent = getIntent();

        //Loop for retrieving Result Data, only executed on "Launch Result"
        if(intent.getBooleanExtra("GetNestbox", false)){
            loadDataFromMemory();
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("key", new ArrayList<>(listNestboxMap));
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }

        //DEFAULT map activity, executed on "Launch map"
        else {
            //Determin the correct list to use between memory and userinput
            List<Nestbox> listNestboxMapNeu = intent.getParcelableArrayListExtra("NestboxesToCheckListKey");
            loadDataFromMemoryToCompare();
            loadDataFromMemory();

            // Compare Lists and update the current list if a new one has been loaded
            if (!listNestboxMapInMemory.equals(listNestboxMapNeu)) {
                listNestboxMap = listNestboxMapNeu;
                listNestboxMapInMemory = listNestboxMapNeu;
                saveDataMapToCompare();
                saveDataMap();
            }
            // lists aint equal -> Update the map
            else {
                listNestboxMap = listNestboxMapNeu;
                listNestboxMapInMemory = listNestboxMapNeu;
                saveDataMap();
                saveDataMapToCompare();
            }

            /* HEART of the Map:
            * Sets up the Map and connects Buttons to the Appropriate NEESTBOX-Object*/
            zivMapNestbox = (TouchImageView) findViewById(R.id.mapNestboxID);
            nestboxButtonArrayChosen = new Button[listNestboxMap.size()];
            createNestboxArrayChosen();
            originalButtonCoordinates = new float[listNestboxMap.size()][2];

            //get the original button coordinates.
            ViewTreeObserver viewTreeObserver = zivMapNestbox.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    zivMapNestbox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    for (int a = 0; a < listNestboxMap.size(); a++) { //itterates trought my List of Nestboxes to check
                        originalButtonCoordinates[a][0] = nestboxButtonArrayChosen[a].getX() + nestboxButtonArrayChosen[a].getWidth() / 2;
                        originalButtonCoordinates[a][1] = nestboxButtonArrayChosen[a].getY() + nestboxButtonArrayChosen[a].getHeight() / 2;
                    }
                    visibilityOfButtonsON();
                }
            });

            //ListView for NestProgress
            neststateListView = findViewById(R.id.neststateListID);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, ITEMS_NESTBOX);
            neststateListView.setAdapter(adapter);
            neststateListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            //Number Picker for Number of Eggs
            //For blue tits no more than 10 are expected, so 50% buffer should be safe
            numberPicker = (NumberPicker) findViewById(R.id.numberPickerID);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(15);
            //Checkbox for Sample True/false
            checkBoxSampling = (CheckBox) findViewById(R.id.checkBoxEggsSampledID);
            checkBoxBT = (CheckBox) findViewById(R.id.checkBox_BT_id);
            checkBoxGT= (CheckBox) findViewById(R.id.checkBox_GT_id);

            //Setup of The 2 Spinners for the Coordinates of Collected Eggs
            // Spinner 1 - Letters
            String[] coordinateOfCollectedEggsChar = {"A", "B", "C", "D", "E", "F", "G", "H"};
            ArrayAdapter<String> adapterSpin = new ArrayAdapter(this, android.R.layout.simple_spinner_item, coordinateOfCollectedEggsChar);
            adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner = findViewById(R.id.spinner_id);
            spinner.setAdapter(adapterSpin);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean firstCheck = true;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(firstCheck){
                        firstCheck = false;
                    }
                    else{
                        CharacterCoordinate = parent.getSelectedItem().toString();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            // Spinner 2 - Numbers
            String[] coordinateOfCollectedEggsInt = {"1", "2", "3", "4", "5", "6", "7", "8"};
            ArrayAdapter adapterSpinTwo = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, coordinateOfCollectedEggsInt);
            adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinnerTwo = findViewById(R.id.spinner_two_id);
            spinnerTwo.setAdapter(adapterSpinTwo);

            spinnerTwo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean firstCheck = true;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(firstCheck){
                        firstCheck = false;
                    }
                    else{
                        NumberCoordinate = parent.getSelectedItem().toString();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            checkBoxSampling.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    LinearLayout layoutCoordinates = findViewById(R.id.layout_coordinates);
                    if(isChecked){
                        layoutCoordinates.setVisibility(View.VISIBLE);
                    }
                    else{
                        layoutCoordinates.setVisibility(View.GONE);
                    }
                }
            });
            //update Button Position if Screen Moves
            zivMapNestbox.setOnTouchImageViewListener(this::updateNestboxButtonPosition);
        }
    }

    public void nestboxButtonClick(View v){
        LinearLayout outerLayout = findViewById(R.id.outer_layout_id);
        outerLayout.setVisibility(View.VISIBLE);
        visibilityOfButtonsOFF();
        TextView header = findViewById(R.id.textViewHeaderID);
        currentBoxnummerString = getResources().getResourceEntryName(v.getId());
        String boxnumberS = currentBoxnummerString.replaceAll("[^0-9]", "");
        header.setText("Boxnumber " + boxnumberS);
    }

    public void updateNestboxButtonPosition() {
        PointF pointScroll = zivMapNestbox.getScrollPosition();
        int widthView = zivMapNestbox.getWidth();
        int heightView = zivMapNestbox.getHeight();
        float screenCenterX = pointScroll.x * widthView; //IST 75 X
        float screenCenterY = pointScroll.y * heightView;
        float screenCenterSollX = widthView / 2; // SOll 50 X
        float screenCenterSollY = heightView / 2;
        float cFunctionX = screenCenterSollX - (zivMapNestbox.getCurrentZoom() * screenCenterX);
        float cFunctionY = screenCenterSollY - (zivMapNestbox.getCurrentZoom() * screenCenterY);

        float[][] newPositionXY = new float[listNestboxMap.size()][2];

        for(int c = 0; c < listNestboxMap.size(); c++){
            newPositionXY[c][0] = zivMapNestbox.getCurrentZoom() * originalButtonCoordinates[c][0] + cFunctionX;
            newPositionXY[c][1] = zivMapNestbox.getCurrentZoom() * originalButtonCoordinates[c][1] + cFunctionY;
            nestboxButtonArrayChosen[c].setX(newPositionXY[c][0]-nestboxButtonArrayChosen[c].getWidth()/2);
            nestboxButtonArrayChosen[c].setY(newPositionXY[c][1]-nestboxButtonArrayChosen[c].getHeight()/2);
        }
    }

    public void visibilityOfButtonsON(){
        for(int b = 0; b < listNestboxMap.size(); b++){
            nestboxButtonArrayChosen[b].setVisibility(View.VISIBLE);
            if(!"default".equals(listNestboxMap.get(b).getNestProgress())){
                nestboxButtonArrayChosen[b].setBackgroundResource(R.drawable.nestbox_checked);
            }
        }
    }

    public void visibilityOfButtonsOFF(){
        for(int b = 0; b < listNestboxMap.size(); b++){
            nestboxButtonArrayChosen[b].setVisibility(View.INVISIBLE);
        }
    }

    public void saveEntry(View v){ //save the data and set layout to gone again
        //find Nestbox in the list.
        String currentBoxnummerStringNeu = currentBoxnummerString.replaceAll("[^0-9]", "");
        int currentBoxnummerInt = Integer.parseInt(currentBoxnummerStringNeu); //Boxnumber of clicked box

        for(int i = 0; i < listNestboxMap.size(); i++){
            if(currentBoxnummerInt == listNestboxMap.get(i).getNestboxNumber()){
                currentNestboxIndex = i;
                i = listNestboxMap.size();
            }
        }
        //Save choices into listNestboxMap
        int choice = neststateListView.getCheckedItemPosition();
        String currentNestProgress = "default";
        if(choice != ListView.INVALID_POSITION){
            currentNestProgress = ITEMS_NESTBOX[choice];
        }

        // What bird has been sighted?
        boolean currentSampled = checkBoxSampling.isChecked();
        boolean currentBirdGT = checkBoxGT.isChecked();
        boolean currentBirdBT = checkBoxBT.isChecked();
        if(currentBirdBT && !currentBirdGT){
            //BT
            listNestboxMap.get(currentNestboxIndex).setSpottedBird("(BT)");
        } else if (!currentBirdBT && currentBirdGT) {
            //GT
            listNestboxMap.get(currentNestboxIndex).setSpottedBird("(GT)");
        }
        else if(currentBirdBT){
            Toast.makeText(this, "you picked both BT and GT!", Toast.LENGTH_SHORT).show();
        }

        int currentEggsOrChics = numberPicker.getValue();

        listNestboxMap.get(currentNestboxIndex).setNestProgress(currentNestProgress);
        listNestboxMap.get(currentNestboxIndex).setSampled(currentSampled);
        listNestboxMap.get(currentNestboxIndex).setEggsOrChics(currentEggsOrChics);

        if (!(CharacterCoordinate.equals("")) && !(NumberCoordinate.equals(""))) {
            listNestboxMap.get(currentNestboxIndex).setEggCoordinateCharacter(CharacterCoordinate);
            listNestboxMap.get(currentNestboxIndex).setEggCoordinateNumber(NumberCoordinate);
        }

        //reset choices.
        numberPicker.setValue(0);
        neststateListView.clearChoices();
        neststateListView.requestLayout();
        checkBoxSampling.setChecked(false);
        checkBoxBT.setChecked(false);
        checkBoxGT.setChecked(false);
        NumberCoordinate = "";
        CharacterCoordinate = "";


        EditText editText = findViewById(R.id.editTextID);
        String userInput = editText.getText().toString();

                //checks if a NestProgress has been selected
        if(currentNestProgress.equals("default")) {
            Toast.makeText(this, "Please select Nestprogress", Toast.LENGTH_SHORT).show();
        }
        else{
            if (!userInput.isEmpty()) {
                listNestboxMap.get(currentNestboxIndex).setNestProgress(userInput);
            }
            saveDataMap();
            LinearLayout layout = findViewById(R.id.outer_layout_id);
            layout.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            editText.setText("");
            visibilityOfButtonsON();
        }
        LinearLayout innerLayoutLeft = findViewById(R.id.inner_layout_right_ID);
        innerLayoutLeft.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
    }

    public void confirmNeststate(View v){
        //Logic for handling of ListView
        LinearLayout innerLayoutLeft = findViewById(R.id.inner_layout_right_ID);
        innerLayoutLeft.setVisibility(View.INVISIBLE);
        int checkedItem = neststateListView.getCheckedItemPosition();
        if(checkedItem > 4 && checkedItem !=10){
            innerLayoutLeft.setVisibility(View.VISIBLE);
        }
        else if (checkedItem == 10) {
            EditText editText = findViewById(R.id.editTextID);
            editText.setVisibility(View.VISIBLE);
        }
    }
    public void cancel(View v){
        EditText editText = findViewById(R.id.editTextID);
        LinearLayout layout = findViewById(R.id.outer_layout_id);
        layout.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        editText.setText("");
        visibilityOfButtonsON();
        LinearLayout innerLayoutLeft = findViewById(R.id.inner_layout_right_ID);
        innerLayoutLeft.setVisibility(View.INVISIBLE);

        numberPicker.setValue(0);
        neststateListView.clearChoices();
        neststateListView.requestLayout();
        checkBoxSampling.setChecked(false);
        checkBoxBT.setChecked(false);
        checkBoxGT.setChecked(false);
    }


    public void saveDataMap(){
        //Save List with Json/Gson to sharedPrefNeu.
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_MAP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonString = gson.toJson(listNestboxMap);

        editor.putString(SAVE_STRING_MAP, jsonString);
        editor.apply();
    }

    public void saveDataMapToCompare(){
        //Save List with Json/Gson to sharedPrefNeu.
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_MAP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonString = gson.toJson(listNestboxMapInMemory);

        editor.putString(SAVE_STRING_MAP_PREV_DATA, jsonString);
        editor.apply();
    }

    public void loadDataFromMemory(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_MAP, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(SAVE_STRING_MAP, null);
        if (jsonString == null || jsonString.isEmpty()){ //Set default empty listView in case of no Data in Memory
            listNestboxMap = new ArrayList<>();
        }
        else{
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Nestbox>>(){}.getType();
            listNestboxMap = gson.fromJson(jsonString, listType);
        }
    }

    public void loadDataFromMemoryToCompare(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_MAP, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(SAVE_STRING_MAP_PREV_DATA, null);
        if (jsonString == null || jsonString.isEmpty()){ //Set default empty listView in case of no Data in Memory
            listNestboxMapInMemory = new ArrayList<>();
        }
        else{
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Nestbox>>(){}.getType();
            listNestboxMapInMemory = gson.fromJson(jsonString, listType);
        }
    }

    public void createNestboxArrayChosen(){
        nestboxButtonArray = new Button[]{//When new "Nestbox" added append here. Make Sure ID is set Appropriatly
                (Button) findViewById(R.id.NestBox1),
                (Button) findViewById(R.id.NestBox2),
                (Button) findViewById(R.id.NestBox3),
                (Button) findViewById(R.id.NestBox4),
                (Button) findViewById(R.id.NestBox5),
                (Button) findViewById(R.id.NestBox7),
                (Button) findViewById(R.id.NestBox8),
                (Button) findViewById(R.id.NestBox9),
                (Button) findViewById(R.id.NestBox10),
                (Button) findViewById(R.id.NestBox11),
                (Button) findViewById(R.id.NestBox12),
                (Button) findViewById(R.id.NestBox13),
                (Button) findViewById(R.id.NestBox14),
                //No 16
                (Button) findViewById(R.id.NestBox16),
                //No 17
                (Button) findViewById(R.id.NestBox18),
                (Button) findViewById(R.id.NestBox19),
                (Button) findViewById(R.id.NestBox20),
                (Button) findViewById(R.id.NestBox21),
                (Button) findViewById(R.id.NestBox22),
                (Button) findViewById(R.id.NestBox23),
                (Button) findViewById(R.id.NestBox24),
                //No 25
                (Button) findViewById(R.id.NestBox26),
                (Button) findViewById(R.id.NestBox27),
                (Button) findViewById(R.id.NestBox28),
                (Button) findViewById(R.id.NestBox29),
                (Button) findViewById(R.id.NestBox30),
                (Button) findViewById(R.id.NestBox31),
                //No 32
                (Button) findViewById(R.id.NestBox33),
                (Button) findViewById(R.id.NestBox34),
                (Button) findViewById(R.id.NestBox35),
                (Button) findViewById(R.id.NestBox36),
                (Button) findViewById(R.id.NestBox37),
                (Button) findViewById(R.id.NestBox38),
                (Button) findViewById(R.id.NestBox39),
                (Button) findViewById(R.id.NestBox40),
                (Button) findViewById(R.id.NestBox41),
                (Button) findViewById(R.id.NestBox42),
                (Button) findViewById(R.id.NestBox43),
                (Button) findViewById(R.id.NestBox44),
                //No 45
                (Button) findViewById(R.id.NestBox46),
                (Button) findViewById(R.id.NestBox47),
                (Button) findViewById(R.id.NestBox48),
                //No 49
                (Button) findViewById(R.id.NestBox50),
                (Button) findViewById(R.id.NestBox51),
                (Button) findViewById(R.id.NestBox52),
                (Button) findViewById(R.id.NestBox53),
                (Button) findViewById(R.id.NestBox54),
                (Button) findViewById(R.id.NestBox55),
                (Button) findViewById(R.id.NestBox56),
                (Button) findViewById(R.id.NestBox57),
                (Button) findViewById(R.id.NestBox58),
                (Button) findViewById(R.id.NestBox59),
                (Button) findViewById(R.id.NestBox60),
                (Button) findViewById(R.id.NestBox61),
                (Button) findViewById(R.id.NestBox62),
                (Button) findViewById(R.id.NestBox63),
                (Button) findViewById(R.id.NestBox64),
                (Button) findViewById(R.id.NestBox65),
                (Button) findViewById(R.id.NestBox66),
                (Button) findViewById(R.id.NestBox67),
                (Button) findViewById(R.id.NestBox68),
                (Button) findViewById(R.id.NestBox69),
                (Button) findViewById(R.id.NestBox70),
                (Button) findViewById(R.id.NestBox71),
                (Button) findViewById(R.id.NestBox72),
                (Button) findViewById(R.id.NestBox73),
                (Button) findViewById(R.id.NestBox74),
                (Button) findViewById(R.id.NestBox75),
                (Button) findViewById(R.id.NestBox76),
                (Button) findViewById(R.id.NestBox77),
                (Button) findViewById(R.id.NestBox78),
                (Button) findViewById(R.id.NestBox79),
                (Button) findViewById(R.id.NestBox80),
                (Button) findViewById(R.id.NestBox81),
                (Button) findViewById(R.id.NestBox82),
                (Button) findViewById(R.id.NestBox83),
                (Button) findViewById(R.id.NestBox84),
                (Button) findViewById(R.id.NestBox85),
                (Button) findViewById(R.id.NestBox86),
                (Button) findViewById(R.id.NestBox87),
                (Button) findViewById(R.id.NestBox88),
                (Button) findViewById(R.id.NestBox89),
                (Button) findViewById(R.id.NestBox90),
                (Button) findViewById(R.id.NestBox91),
                (Button) findViewById(R.id.NestBox92),
                (Button) findViewById(R.id.NestBox93),
                (Button) findViewById(R.id.NestBox94),
                (Button) findViewById(R.id.NestBox95),
                (Button) findViewById(R.id.NestBox96),
                (Button) findViewById(R.id.NestBox97),
                (Button) findViewById(R.id.NestBox98),
                (Button) findViewById(R.id.NestBox99),
                (Button) findViewById(R.id.NestBox100),
                (Button) findViewById(R.id.NestBox101),
                (Button) findViewById(R.id.NestBox102),
                (Button) findViewById(R.id.NestBox103),
                (Button) findViewById(R.id.NestBox104),
                (Button) findViewById(R.id.NestBox105),
                (Button) findViewById(R.id.NestBox200),
                (Button) findViewById(R.id.NestBox201),
                (Button) findViewById(R.id.NestBox202),
                (Button) findViewById(R.id.NestBox203),
                (Button) findViewById(R.id.NestBox204),
                (Button) findViewById(R.id.NestBox205),
                (Button) findViewById(R.id.NestBox206),
                (Button) findViewById(R.id.NestBox207),
                (Button) findViewById(R.id.NestBox208),
                (Button) findViewById(R.id.NestBox209),
                (Button) findViewById(R.id.NestBox210),
                (Button) findViewById(R.id.NestBox211),
                (Button) findViewById(R.id.NestBox212),
                (Button) findViewById(R.id.NestBox213),
                (Button) findViewById(R.id.NestBox214),
                (Button) findViewById(R.id.NestBox215),
                (Button) findViewById(R.id.NestBox216),
                (Button) findViewById(R.id.NestBox217),
                (Button) findViewById(R.id.NestBox218),
                (Button) findViewById(R.id.NestBox219),
                (Button) findViewById(R.id.NestBox220),
                (Button) findViewById(R.id.NestBox221),
                (Button) findViewById(R.id.NestBox222),
                (Button) findViewById(R.id.NestBox223),
                (Button) findViewById(R.id.NestBox224),
                (Button) findViewById(R.id.NestBox225),
                (Button) findViewById(R.id.NestBox226),
                (Button) findViewById(R.id.NestBox227),
                (Button) findViewById(R.id.NestBox228),
                (Button) findViewById(R.id.NestBox229),
                (Button) findViewById(R.id.NestBox230),
                (Button) findViewById(R.id.NestBox231),
                (Button) findViewById(R.id.NestBox232),
                (Button) findViewById(R.id.NestBox233),
                (Button) findViewById(R.id.NestBox234),
                (Button) findViewById(R.id.NestBox235),
                (Button) findViewById(R.id.NestBox236),
                (Button) findViewById(R.id.NestBox237),
                (Button) findViewById(R.id.NestBox238),
                (Button) findViewById(R.id.NestBox239),
                (Button) findViewById(R.id.NestBox240),
                (Button) findViewById(R.id.NestBox241),
                (Button) findViewById(R.id.NestBox242),
                (Button) findViewById(R.id.NestBox243),
                (Button) findViewById(R.id.NestBox244),
                (Button) findViewById(R.id.NestBox245),
                (Button) findViewById(R.id.NestBox246),
                (Button) findViewById(R.id.NestBox247),
                (Button) findViewById(R.id.NestBox248),
                (Button) findViewById(R.id.NestBox249),
                (Button) findViewById(R.id.NestBox250),
                (Button) findViewById(R.id.NestBox251)
        };

        //Test Part 1
        ConstraintLayout layout = findViewById(R.id.main_menu);
        //Test Part 1 End
        //fill nestboxButtonArrayChosen with the buttons we want to use.
        boolean[] tempArray = new boolean[nestboxButtonArray.length];

        for(int a = 0; a < listNestboxMap.size(); a++){ //itterates trough my List of Nestboxes to check
            for(int c = 0; c < nestboxButtonArray.length; c++){ //retrieves ID Number of all the Buttons
                String buttonIDString = getResources().getResourceEntryName(nestboxButtonArray[c].getId());
                String buttonNestboxNummberString = buttonIDString.replaceAll("[^0-9]", "");
                int buttonNestboxNummberInt = Integer.parseInt(buttonNestboxNummberString);
                if(buttonNestboxNummberInt == listNestboxMap.get(a).getNestboxNumber()){ //compares ID to the Nestbox number. On match writes assigns button to buttonarraychosen
                    nestboxButtonArrayChosen[a] = nestboxButtonArray[c];
                    tempArray[c] = true;
                    break;
                }
            }
        }
        //clean up unused buttons
        for(int a = 0; a < nestboxButtonArray.length; a++){
            if(!tempArray[a]){
                layout.removeView(nestboxButtonArray[a]);
            }
        }
    }
}