package com.nestcheck_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;


import com.dropbox.core.android.Auth;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {
    //METHOD FOR ENTERIN DATE
    public int year = 2023;
    public int month = 5; // starts at 0!!!!!!!!
    public int day = 7;
    ///
    private int nestboxRowNumber = 0;
    private int dateOfChoiceRowNumber = 0;

    public File excelFile = null;
    private String accessToken = "";
    private List<Nestbox> listNestboxResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        LocalDate currentDate = LocalDate.now();
        year = currentDate.getYear();
        month = currentDate.getMonthValue() - 1;
        day = currentDate.getDayOfMonth();


        Intent intent = getIntent();
        listNestboxResults = intent.getParcelableArrayListExtra("NestboxesToResult");

        GridView gridView = findViewById(R.id.gridViewID);
        List<String> gridList = new ArrayList<>();


        // Logick for filling the Results based on the NestboxData according to the conventions of the Excel File
        // TODO: If i find time, clean up this spagethi code - maybe after masterthesis is finsihed?

        for (int i = 0; i < listNestboxResults.size(); i++) {
            gridList.add(String.valueOf(listNestboxResults.get(i).getNestboxNumber()));
            if (listNestboxResults.get(i).getNestProgress().equals("Eggs AND/OR Sampling")) {
                if (listNestboxResults.get(i).isSampled()) {
                    gridList.add(listNestboxResults.get(i).getEggsOrChics() + "eggs(X+1)" + " [" + listNestboxResults.get(i).getEggCoordinateCharacter() + listNestboxResults.get(i).getEggCoordinateNumber() + "]");
                } else {
                    gridList.add(listNestboxResults.get(i).getEggsOrChics() + "eggs(X)");
                }
            } else if (listNestboxResults.get(i).getNestProgress().equals("CC") || listNestboxResults.get(i).getNestProgress().equals("CU") || listNestboxResults.get(i).getNestProgress().equals("WU") || listNestboxResults.get(i).getNestProgress().equals("Chics")) {
                gridList.add(listNestboxResults.get(i).getEggsOrChics() + "(" + listNestboxResults.get(i).getNestProgress() + ")");
            } else {
                gridList.add(listNestboxResults.get(i).getNestProgress());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gridList);
        gridView.setAdapter(adapter);
    }
    public void login(View v){
        Auth.startOAuth2Authentication(this, getString(R.string.APP_KEY));
    }

    @Override
    protected void onResume(){
        super.onResume();
        Button uploadButton = (Button)findViewById(R.id.button_upload);
        uploadButton.setVisibility(View.INVISIBLE);
        accessToken = Auth.getOAuth2Token();
        if(accessToken != null){
            //If accesstoken then you can upload stuff
            uploadButton.setVisibility(View.VISIBLE);
        }
    }

    // Create Cellstyle
    private CellStyle createCellStyle(Workbook wb, byte[] color){
        XSSFColor myXssfColor = new XSSFColor(color);
        CellStyle customCellStyle = wb.createCellStyle();
        customCellStyle.setFillForegroundColor(myXssfColor);
        customCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        customCellStyle.setAlignment(HorizontalAlignment.CENTER);
        return customCellStyle;
    }

    // Connection To Dropbox, Downloading of ExcelFile and Filling BASED ON PREVIOUS DAY RESULTS!
    public void dropboxThreadStart(View v){

        DropboxRunnable myDropRun = new DropboxRunnable(accessToken, this);
        Thread myThread = new Thread(myDropRun);
        myThread.start();
        try {
            // Wait for the thread to finish
            myThread.join();
            excelFile = myDropRun.getMyFile();
            if(excelFile.exists()){
                //ALL THE MAGIC HERE
                try(FileInputStream myFIS = new FileInputStream(excelFile)){

                    Workbook workbook = new XSSFWorkbook(myFIS);
                    Sheet sheet = workbook.getSheetAt(0);

                    //Look for the Row corresponding to your Date
                    for(Row row : sheet) {
                        Cell cell = row.getCell(0);
                        CellType cellType = cell.getCellType();

                        //Finding the row with nestbox numbers
                        if (cellType == CellType.STRING && "Datum / Nestbox".equals(cell.getStringCellValue())) {
                            nestboxRowNumber = row.getRowNum();
                        }
                        //Finding the row
                        if (cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            Calendar desiredDate = Calendar.getInstance();
                            desiredDate.set(2023, 4, 17);

                            Calendar cellDate = Calendar.getInstance();
                            cellDate.setTime(cell.getDateCellValue());

                            if (cellDate.get(Calendar.YEAR) == year && cellDate.get(Calendar.MONTH) == month && cellDate.get(Calendar.DAY_OF_MONTH) == day) {
                                dateOfChoiceRowNumber = row.getRowNum();
                                break; //Leave For loop(Row row: sheet)
                            }
                        }
                    }

                    //EXCEL FORMATING ACCORDING TO STANDARD
                    byte[] rgbYellow = {(byte) (255-256), (byte) (255-256), (byte) (0)};
                    byte[] rgbLightGreen = {(byte) (146-256), (byte) (208-256), (byte) 80};
                    byte[] rgbDarkGreen = {(byte) 55, (byte) 86, (byte) 35};
                    byte[] rgbOrange = {(byte) (255-256), (byte) (192-256), (byte) 0};
                    byte[] rgbBlue = {(byte) 0, (byte) (176-256), (byte) (240-256)};
                    byte[] rgbPink = {(byte) (230-256), (byte) 99, (byte) (247-256)};

                    CellStyle cellStyleYellow = createCellStyle(workbook, rgbYellow);
                    CellStyle cellStyleLightGreen = createCellStyle(workbook, rgbLightGreen);
                    CellStyle cellStyleDarkGreen = createCellStyle(workbook, rgbDarkGreen);
                    CellStyle cellStyleOrange = createCellStyle(workbook, rgbOrange);
                    CellStyle cellStyleBlue = createCellStyle(workbook, rgbBlue);
                    CellStyle cellStylePink = createCellStyle(workbook, rgbPink);

                    CellStyle cellStyleOther = workbook.createCellStyle();
                    cellStyleOther.setAlignment(HorizontalAlignment.CENTER);


                    Row RowBoxnumber = sheet.getRow(nestboxRowNumber);
                    Row RowDate = sheet.getRow(dateOfChoiceRowNumber);
                    for(int i = 0; i < listNestboxResults.size(); i++){
                        Nestbox box = listNestboxResults.get(i);
                        int number = box.getNestboxNumber();
                        for(Cell cell : RowBoxnumber){
                            if(cell.getCellType() == CellType.NUMERIC){
                                if(cell.getNumericCellValue() == number){
                                    Cell currentCell = RowDate.getCell(cell.getColumnIndex());
                                    if(currentCell == null){
                                        currentCell = RowDate.createCell(cell.getColumnIndex(), CellType.STRING);
                                    }
                                    switch (box.getNestProgress()) {
                                        case "Eggs AND/OR Sampling":
                                            if (box.isSampled()) {
                                                //extract previous number of eggs.
                                                Cell tempCell = sheet.getRow(dateOfChoiceRowNumber - 1).getCell(cell.getColumnIndex()); //Gettet cell of previous day
                                                if (tempCell == null) {
                                                    tempCell = sheet.getRow(dateOfChoiceRowNumber - 1).createCell(cell.getColumnIndex(), CellType.STRING);
                                                }
                                                String tempString = tempCell.getStringCellValue();
                                                //Check how many eggs have already been sampled
                                                String pattern = "\\(\\d\\)";
                                                Pattern patternObj = Pattern.compile(pattern);
                                                Matcher matcherObj = patternObj.matcher(tempString);
                                                if (matcherObj.find()) {
                                                    String foundString = matcherObj.group();
                                                    String extractedNumber = foundString.replaceAll("\\(|\\)", "");
                                                    int previousEggs = Integer.parseInt(extractedNumber);
                                                    currentCell.setCellValue(box.getEggsOrChics() + "eggs(" + (previousEggs + 1) + ")");
                                                    //5 eggs total -> Orange
                                                    if ((previousEggs + 1) == 5) {
                                                        currentCell.setCellStyle(cellStyleOrange);
                                                    } else {
                                                        currentCell.setCellStyle(cellStyleYellow);
                                                    }
                                                } else {
                                                    //Logik für no sampled eggs
                                                    String pattern_two = "egg";
                                                    Pattern patternObj_two = Pattern.compile(pattern_two);
                                                    Matcher matcherObj_two = patternObj_two.matcher(tempString);
                                                    if (matcherObj_two.find()) {
                                                        currentCell.setCellValue(box.getEggsOrChics() + "eggs(1)");
                                                    } else { //default, if someone didn't stick to the format for the Excel Entry
                                                        currentCell.setCellValue(box.getEggsOrChics() + "eggs(X+1)");
                                                        currentCell.setCellStyle(cellStyleYellow);
                                                    }
                                                }
                                            } else { //No sampled eggs
                                                currentCell.setCellValue(box.getEggsOrChics() + "eggs");
                                                currentCell.setCellStyle(cellStyleYellow);
                                            }
                                            break;
                                        case "CC":
                                        case "CU":
                                        case "WU":
                                        case "Chics":
                                            currentCell.setCellValue(box.getEggsOrChics() + "(" + box.getNestProgress() + ")");
                                            if (box.getNestProgress().equals("CC") || box.getNestProgress().equals("CU")) {
                                                currentCell.setCellStyle(cellStyleOrange);
                                            } else if (box.getNestProgress().equals("WU")) {
                                                currentCell.setCellStyle(cellStyleBlue);
                                            } else {
                                                currentCell.setCellStyle(cellStylePink);
                                            }
                                            break;
                                        case "default":  //do nothing if Default
                                            break;
                                        case "I":
                                        case "U":
                                        case "L":
                                            currentCell.setCellValue(box.getNestProgress());
                                            currentCell.setCellStyle(cellStyleLightGreen);
                                            break;
                                        case "O":
                                            currentCell.setCellValue(box.getNestProgress());
                                            currentCell.setCellStyle(cellStyleDarkGreen);
                                            break;
                                        default:
                                            currentCell.setCellValue(box.getNestProgress());
                                            currentCell.setCellStyle(cellStyleOther);
                                            break;
                                    }
                                    if(box.getSpottedBird().equals("BT") || box.getSpottedBird().equals("GT")){
                                        String currentCellString = currentCell.getStringCellValue();
                                        String newCellString = currentCellString + "   " + box.getSpottedBird();
                                        currentCell.setCellValue(newCellString);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    //Save the file
                    try(FileOutputStream fos = new FileOutputStream(excelFile)){
                        workbook.write(fos);
                    }
                    workbook.close();
                }
                catch (IOException e){
                }
            }
            else if(excelFile == null){
                Toast.makeText(this, "ERROR during Download", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "ERROR during Download", Toast.LENGTH_SHORT).show();
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        myDropRun.setMyFile(excelFile);
        Thread newMyThread = new Thread(myDropRun);
        newMyThread.start();
    }
}
