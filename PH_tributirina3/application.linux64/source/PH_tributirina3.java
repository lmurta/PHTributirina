import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import grafica.*; 
import java.util.Random; 
import controlP5.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class PH_tributirina3 extends PApplet {






boolean DEBUG = false;
Serial myPort; // porta serial
String myString = null;
int lf = 10;    // Linefeed in ASCII

PrintWriter foutput;
boolean outputInitialized = false;
boolean logScale;

PFont fontLight, fontBold, fontBig, fontCp5;
int colorDark, colorGray;
int dia;
int mes;
int ano;
int hora;
int min;
int sec;
String sData, sHora, sDataHora;
boolean firstRead = true;
boolean serialEvent = false;

ControlP5 cp5;
Toggle toggle1;
int col = color(255);
boolean toggleValue = false;
int sliderXLim = 180;

GPlot[] plot = new GPlot[1];
GPlot plot2;
int lastY =0;
int plotColor[] = new int[8];
int baseColor[] = new int[6];

int analogRead[] = new int[6];
public Random r;
int numPoints = 0;
int px, py, pw, ph, pd, p5x, p5y;
int samples_y ;

float pHfactor =1.02f;
float quickanddirty = 0;
float average=0;
float sum = 0;
float[] storedValues;
int count = 0;
int oldPoint =0;
float phAverage =0;

ListBox list_Control, list_Test;
ListBox[] list_Samples = new ListBox[8];
int numSamples =8;
int[] totSamples = new int[8];
int currentSample =1;
boolean saveSample;
int maxSamples = 600;

int mlNaoh = 1;
int count_ml_C =0;
int count_ml_T =0;
int maxMl = 60*6;

//float random_ph;

float enzimas =0.0f;
float find_pH =10.0f;
float molaridade_Naoh =25;
float fator_diluicao =2;

float[][] regression = new float[9][6];
/* Valores de inclina\u00e7\u00e3o da EC para \u00e1gua tamponada 1:100 e 200ml e some metade inicial
 do gr\u00e1fico
 1:1 = -0.0060
 1:2 = -0.0020
 1:4 = -0.0010
 1:8 = -0.0002
 
 Valores para 1:100 e 500ml
 1:1 = -0.0120
 1:2 = -0.0060
 1:4 = -0.0030
 1:8 = -0.0020 (arredondar para -0.0015?)
 
 T100 -T10
 2:1 = -0.024
 1:1 = -0.013
 1:2 = -0.006
 1:4 = -0.003
 1:8 = -0.002
 
 */
float regression_11 = -0.0130f;
int reg_min =60;
int reg_max= reg_min +100;
GPointsArray points_C = new GPointsArray();
public void setup() {
  size(900, 650);
  noStroke();
  smooth();

  saveSample = true;
  for (int i=0; i<totSamples.length; i++) {
    totSamples[i]=0;
  }
  Table table = loadTable("base_dados.csv", "header");
  table.setColumnType("Row", Table.INT);

  table.setColumnType("1:1", Table.FLOAT);
  table.setColumnType("1:2", Table.FLOAT);
  table.setColumnType("1:4", Table.FLOAT);
  table.setColumnType("1:8", Table.FLOAT);
  table.setColumnType("2:1", Table.FLOAT);


  // Save the data in two GPointsArrays
  

  GPointsArray points_base_1 = new GPointsArray();
  GPointsArray points_base_2 = new GPointsArray();
  GPointsArray points_base_4 = new GPointsArray();
  GPointsArray points_base_8 = new GPointsArray();
  GPointsArray points_base_21 = new GPointsArray();


  GPointsArray points_CM = new GPointsArray();
  GPointsArray points_TM = new GPointsArray();

  storedValues = new float[10];

  fontLight = loadFont("OpenSans-CondensedLight-24.vlw"); 
  fontBold = loadFont("OpenSans-CondensedBold-24.vlw");
  fontBig = loadFont("OpenSans-CondensedBold-48.vlw");
  fontCp5 = loadFont("OpenSans-CondensedBold-12.vlw");

  colorDark= color(0xff000000);
  colorGray= color(0xffCCCCCC);

  if (!DEBUG) {
    //myPort = new Serial(this, "/dev/ttyACM0", 115200);
    myPort = new Serial(this, "/dev/ttyUSB0", 115200);
    myPort.clear();
    // Throw out the first reading, in case we started reading 
    // in the middle of a string from the sender.
    myString = myPort.readStringUntil(lf);
    myString = null;
  }
  plotColor[0] =  color(0xffB276B2);// (purple)
  plotColor[1] =  color(0xff5DA5DA);//  (blue)
  plotColor[2] =  color(0xffFAA43A);//  (orange)
  plotColor[3] =  color(0xff60BD68);//  (green)
  plotColor[4] =  color(0xffF17CB0);//  (pink)
  plotColor[5] =  color(0xffB2912F);//  (brown)
  plotColor[6] =  color(0xffDECF3F);//  (brown)
  plotColor[7] =  color(0xffF15854);//  (brown)

  baseColor[0] =  color(100, 100, 100, 100);//  (orange)
  baseColor[1] =  color(100, 100, 100, 100);//  (orange)
  baseColor[2] =  color(100, 100, 100, 100);//  (orange)
  baseColor[3] =  color(100, 100, 100, 100);//  (orange)
  baseColor[4] =  color(100, 100, 100, 100);//  (orange)


  /*
B276B2 (purple)
   DECF3F (yellow)
   F15854 (red)
   */

  px =0;
  py = 10;
  pw = width/6*3;
  ph = height/8*3;
  pd = 15;

  p5x = 60;
  p5y = 20;

  cp5 = new ControlP5(this);
  cp5.setControlFont(fontCp5);
  // change the original colors
  cp5.setColorForeground(color(150, 150, 255));
  cp5.setColorBackground(color(199, 199, 199));
  cp5.setColorLabel(color(0, 0, 0));
  cp5.setColorValue(color(0, 0, 0));
  cp5.setColorActive(color(100, 100, 250));

  samples_y = 320;

  for (int i=0; i<list_Samples.length; i++) {
    int sample =i+1;
    list_Samples[i] = cp5.addListBox("Sample_"+sample)
      .setLabel(""+sample)
        .setPosition(width - 320 +(40 *i), samples_y + 15)
          .setSize(35, 300)
            .setItemHeight(15)
              .setBarHeight(15)
                //              .setColorLabel(plotColor[i])
                .setColorBackground(plotColor[i])
                  ;
  }

  cp5.addButton("Start")
    .setValue(0)
      .setLabel("Start")
        .setPosition(width -70, 240 -40)
          .setSize(40, 15)
            ;

  cp5.addButton("saveFile")
    .setValue(0)
      .setLabel("Save")
        .setPosition(width -120, 240 -40)
          .setSize(40, 15)
            ;

  float n=0; 
  for (int row = 0; row < table.getRowCount (); row++) {
    int base_row = table.getInt(row, "Row");
    float base_1 = table.getFloat(row, "1:1");
    float base_2 = table.getFloat(row, "1:2");
    float base_4 = table.getFloat(row, "1:4");
    float base_8 = table.getFloat(row, "1:8");
    float base_21 = table.getFloat(row, "2:1");
    //println(row +" "+ base_C);
    points_base_1.add(base_row, base_1, ""+base_1);
    points_base_2.add(base_row, base_2, ""+base_2);
    points_base_4.add(base_row, base_4, ""+base_4);
    points_base_8.add(base_row, base_8, ""+base_8);
    points_base_21.add(base_row, base_21, ""+base_21);

    regression[1][0] += (float) base_row; //sx
    regression[1][1] += base_1;    //sy
    regression[1][2] += pow(base_row, 2); //sx2
    regression[1][3] += base_row * base_1; //sxy

    regression[2][0] += (float) base_row; //sx
    regression[2][1] += base_2;    //sy
    regression[2][2] += pow(base_row, 2); //sx2
    regression[2][3] += base_row * base_2; //sxy

    regression[3][0] += (float) base_row; //sx
    regression[3][1] += base_4;    //sy
    regression[3][2] += pow(base_row, 2); //sx2
    regression[3][3] += base_row * base_4; //sxy

    regression[4][0] += (float) base_row; //sx
    regression[4][1] += base_8;    //sy
    regression[4][2] += pow(base_row, 2); //sx2
    regression[4][3] += base_row * base_8; //sxy




    n++;
  }                
  //println(regression[1]);
  for (int i =1; i<=4; i++) {
    regression[i][4] = (regression[i][3] - (regression[i][0] * regression[i][1])/n)/
      (regression[i][2] - (pow(regression[i][0], 2)/n)); 
    // println(regression[i][4]);
  }
  regression = new float[9][6];

  for (int i = 0; i < plot.length; i++) {
    plot[i] = new GPlot(this);
    plot[i].setPos(px, py);
    plot[i].setDim(pw, ph );
    //plot[i].getTitle().setText("Analog "+i);
    plot[i].getYAxis().getAxisLabel().setText("pH");
    //plot[i].activateZooming(1.5);
    plot[i].setFixedXLim(true);
    plot[i].setXLim(0.0f, 300.0f);

    plot[i].setLineColor(plotColor[i]);
    plot[i].setBoxBgColor(color(250));
    plot[i].setLineWidth(1.5f);
    plot[i].setYLim(0.0f, 1024.0f);
    plot[i].setMar(5.0f, 60.0f, 5.0f, 5.0f);
    plot[i].getYAxis().setRotateTickLabels(false);
    plot[i].activatePointLabels();
    py = py +ph + pd;
  }
  py = py + 20;

  plot2 = new GPlot(this);

  if (logScale) {
    plot2.setLogScale("y");
    plot2.getYAxis().setAxisLabelText("log y");
  } else {
    plot2.setLogScale("");
    plot2.getYAxis().setAxisLabelText("y");
  }

  plot2.setPos(px, py);
  plot2.setDim(pw, ph + 60);
  //plot[i].getTitle().setText("Analog "+i);
  plot2.getYAxis().getAxisLabel().setText("pH");
  //plot[i].activateZooming(1.5);
  plot2.setFixedXLim(true);
  plot2.setFixedYLim(false);
  plot2.setXLim(0.0f, maxMl);

  plot2.setLineColor(plotColor[2]);
  plot2.setBoxBgColor(color(250));
  plot2.setLineWidth(4.0f);
  plot2.setYLim(0.0f, 10.0f);
  plot2.setMar(5.0f, 60.0f, 5.0f, 5.0f);
  plot2.getYAxis().setRotateTickLabels(false);
  plot2.getXAxis().setNTicks(10); 

  plot2.activatePointLabels();


  for (int i=1; i<=numSamples; i++) {
    String name = "sample_" + i;
    plot2.addLayer(name, points_C);
    plot2.getLayer(name).setLineColor(plotColor[i-1]);
    plot2.getLayer(name).setLineWidth(1.5f);
    // plot2.getLayer(name).drawLegend(name,100.0,100.0);
  }
  plot2.addLayer("Base 1", points_base_1); 
  plot2.getLayer("Base 1").setLineColor(baseColor[0]); 
  plot2.getLayer("Base 1").setLineWidth(1.0f); 
  plot2.drawAnnotation("1100",10.0f,10.0f,RIGHT,TOP);
  
  plot2.addLayer("Base 2", points_base_2); 
  plot2.getLayer("Base 2").setLineColor(baseColor[1]); 
  plot2.getLayer("Base 2").setLineWidth(1.0f); 

  plot2.addLayer("Base 4", points_base_4); 
  plot2.getLayer("Base 4").setLineColor(baseColor[2]); 
  plot2.getLayer("Base 4").setLineWidth(1.0f); 

  plot2.addLayer("Base 8", points_base_8); 
  plot2.getLayer("Base 8").setLineColor(baseColor[3]); 
  plot2.getLayer("Base 8").setLineWidth(1.0f); 

  plot2.addLayer("Base 21", points_base_21); 
  plot2.getLayer("Base 21").setLineColor(baseColor[4]); 
  plot2.getLayer("Base 21").setLineWidth(1.0f); 


  plot2.addLayer("Base CM", points_CM);
  plot2.getLayer("Base CM").setLineWidth(2.0f);
  plot2.getLayer("Base CM").setLineColor(plotColor[3]);

  plot2.addLayer("Base TM", points_TM);
  plot2.getLayer("Base TM").setLineWidth(2.0f);
  plot2.getLayer("Base TM").setLineColor(plotColor[4]);

  py = py +ph + pd;

  //pH on 0
  plot[0].setYLim(0.0f, 14.0f);
  py = py + 20;
}


public void draw() {
  background(255);
  fill(0);

  textFont(fontCp5);
  textAlign(RIGHT);
  // text("Last?", p5x+45, p5y+5);

  textAlign(RIGHT);
  dia = day();
  mes = month();
  ano = year();
  hora = hour();
  min = minute();
  sec = second();

  sData = nf(dia, 2) +"/"+ nf(mes, 2) +"/"+ ano;
  sHora = nf(hora, 2)  +":"+ nf(min, 2) +":"+ nf(sec, 2);
  sDataHora = ano + nf(mes, 2) + nf(dia, 2) + nf(hora, 2)  + nf(min, 2) + nf(sec, 2);
  outputInitialized = true;
  int fontSize=24;
  int py=20;
  int rx=60;

  textFont(fontBold);
  fill(colorDark);
  textAlign(RIGHT);
  text(sData, width-rx, py);

  fill(colorGray);
  textAlign(LEFT);
  text(":D", width-rx, py);

  py+=fontSize;
  textFont(fontBold);
  fill(colorDark);
  textAlign(RIGHT);

  text(sHora, width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":H", width-rx, py);

  textAlign(RIGHT);
  fill(colorDark);
  py+=fontSize;
  py+=fontSize;
  text(numPoints, width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":i", width-rx, py);


  //pHmeter on pin0
  // float phValue=(float)avgValue*5.0/1024/6; //convert the analog into millivolt
  // phValue=3.5*phValue;  //convert the millivolt into pH value
  float phValue=(float)analogRead[0] * 5.0f / 1024;
  phValue=3.5f*phValue;
  phValue = phValue * pHfactor;




  if (numPoints != oldPoint) {
    //float lerp = 0.55; //must be between 0-1
    //quickanddirty = lerp * quickanddirty + (1.0-lerp) * phValue;

    AddNewValue(phValue);
    phAverage = 0;
    if (count > 0) {

      phAverage = sum / count;
    }
    if (totSamples[currentSample-1] >  maxSamples) {
      saveSample=false;
    }
    if (saveSample==true) {
      list_Samples[currentSample -1].addItem(""+nf(phAverage, 1, 2), count_ml_T);
      plot2.getLayer("sample_"+currentSample).addPoint(totSamples[currentSample-1], phAverage, ""+nf(phAverage, 1, 2));

      if (totSamples[currentSample-1] == reg_min) {
        regression[currentSample - 1][0] = phAverage;
      }
      if ((totSamples[currentSample-1] > reg_min) && (totSamples[currentSample-1] <= reg_max)) {
        regression[currentSample - 1][1] = phAverage;

        //regression[currentSample - 1][0] = 7.7;
        //regression[currentSample - 1][1] = 7.7 + regression_11 *10;

        regression[currentSample - 1][4] = (regression[currentSample - 1][1] - regression[currentSample - 1][0]) 
          /( totSamples[currentSample-1] - reg_min);
        //          /(reg_max - reg_min);
        regression[currentSample - 1][4] = regression[currentSample - 1][4] *1135 / regression_11;
        regression[currentSample - 1][5] = regression[currentSample - 1][4] *100 / 1135;
      }
      totSamples[currentSample-1]++;
    }

    oldPoint = numPoints;
  }

  textFont(fontCp5);
  textAlign(RIGHT);
  fill(colorDark);

  text("U/ml:", width - 285 -40, samples_y -20);
  for (int i=0; i < list_Samples.length; i++) {
    text(PApplet.parseInt(regression[i][4]), width - 290 +(40 *i), samples_y -20);
  }
  text(" %EC:", width - 285 -40, samples_y -5);
  for (int i=0; i < list_Samples.length; i++) {
    text(PApplet.parseInt(regression[i][5]), width - 290 +(40 *i), samples_y -5);
  }

  textFont(fontBold);





  plot[0].addPoint(numPoints, phValue, "pH:"+nf(phValue, 1, 2));

  for (int i = 1; i < plot.length; i++) {
    plot[i].addPoint(numPoints, analogRead[i], "");
  }
  for (int i = 0; i < plot.length; i++) {

    plot[i].beginDraw();
    plot[i].drawBackground();
    plot[i].drawBox();
    plot[i].drawXAxis();
    plot[i].drawYAxis();
    plot[i].drawTitle();
    plot[i].drawGridLines(GPlot.BOTH);
    plot[i].drawLines();
    plot[i].drawLabels();

    plot[i].endDraw();
  }

  GPoint reg1 = new GPoint(reg_min, 0.0f);
  GPoint reg2 = new GPoint(reg_min, 14.0f);
  GPoint reg3 = new GPoint(reg_max, 0.0f);
  GPoint reg4 = new GPoint(reg_max, 14.0f);


  plot2.beginDraw();
  plot2.drawBackground();
  plot2.drawBox();
  plot2.drawXAxis();
  plot2.drawYAxis();
  plot2.drawTitle();
  plot2.drawGridLines(GPlot.BOTH);
  plot2.drawLines();
  plot2.drawLabels();
  //plot2.drawFilledContours(GPlot.HORIZONTAL, 0);
  plot2.drawLine(reg1, reg2, color(00,99,00), 1.0f);
  plot2.drawLine(reg3, reg4, color(00,99,99), 1.0f);
  plot2.endDraw();

  GPoint lastPoint = plot[0].getPointsRef().getLastPoint();

  float limX = plot[0].getXLim()[1];
  if ((lastPoint.getX() >  limX)) {
    lastY = lastY + 10;
    for (int i = 0; i < plot.length; i++) {
      //plot[i].setFixedXLim(true);
      plot[i].setXLim(limX -0, limX + sliderXLim);
    }
  }
  //  py+=fontSize; 
  //pH 
  textAlign(RIGHT);
  fill(colorDark);
  py+=fontSize;
  text(nf(phValue, 1, 2), width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":pH", width-rx, py);

  textAlign(RIGHT);
  fill(colorDark);
  py+=fontSize;
  text(nf(phAverage, 1, 2), width-rx, py);
  //text(nf(quickanddirty, 1, 2), width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":pH", width-rx, py);

  textAlign(RIGHT);
  fill(colorDark);
  py+=fontSize;
  text(nf(enzimas, 1, 2), width-rx, py);
  //text(nf(quickanddirty, 1, 2), width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":U/ml", width-rx, py);

  textAlign(RIGHT);
  fill(colorDark);
  py+=fontSize;
  text(currentSample, width-rx, py);
  //text(nf(quickanddirty, 1, 2), width-rx, py);
  fill(colorGray);
  textAlign(LEFT);
  text(":S", width-rx, py);


  fill(colorDark);
  textFont(fontLight);
}

public void serialEvent(Serial myPort) {

  serialEvent = true;
  myString = null;
  myString =myPort.readStringUntil(lf);
  if ((myString != null)) {
    int asterisk = myString.indexOf('*');
println(myString + asterisk);
    //if (asterisk ==36) {
      if (asterisk >0) {
      println(myString + asterisk);
      String[] vals = split(myString, ",");
      //println("commas="+vals.length);
      //println(vals);
      for (int i=0; i<6; i++) {
        //println(i +" "+vals[i]);
        String strAnalog[] =split(vals[i], ":");
        analogRead[i] =  PApplet.parseInt(strAnalog[1]);
      }
      numPoints++;
    }
  }
}

public void keyPressed() {
  //println("a key event. key:"+ key +" keyCode:"+ keyCode);
  if (key == CODED) {
    if (keyCode == RETURN) {
      println("Enter pressed");
    }
  }
  if (key=='1') { 
    currentSample =1;
  }
  if (key=='2') { 
    currentSample =2;
  }
  if (key=='3') { 
    currentSample =3;
  }
  if (key=='4') { 
    currentSample =4;
  }
  if (key=='5') { 
    currentSample =5;
  }
  if (key=='6') { 
    currentSample =6;
  }
  if (key=='7') { 
    currentSample =7;
  }  
  if (key=='8') { 
    currentSample =8;
  }  
  if (key=='s') { 
    Start(0);
  }
  if (key=='x') { 
    totSamples[currentSample-1] = 0;
    list_Samples[currentSample -1].clear();
    String name = "sample_" + currentSample;
    plot2.removeLayer(name);
    
    plot2.addLayer(name, points_C);
    plot2.getLayer(name).setLineColor(plotColor[currentSample-1]);
    plot2.getLayer(name).setLineWidth(1.5f);

}
  if (key=='l') { 
    // Change the log scale
    logScale = !logScale;

    if (logScale) {
      plot2.setLogScale("y");
      plot2.getYAxis().setAxisLabelText("log y");
    } else {
      plot2.setLogScale("");
      plot2.getYAxis().setAxisLabelText("y");
    }
  }
}
public void AddNewValue(float val)
{
  if (count < storedValues.length) {
    //array is not full yet
    storedValues[count++] = val;
    sum += val;
  } else {
    sum += val; 
    sum -= storedValues[0];
    //shift all of the values, drop the first one (oldest) 
    for (int i = 0; i < storedValues.length-1; i++)
    {
      storedValues[i] = storedValues[i+1] ;
    }
    //the add the new one
    storedValues[storedValues.length-1] = val;
  }
}

public void Start(int theValue) {
  if (saveSample ==true) {
    saveSample=false;
  } else {
    saveSample=true;
  }
}

public void saveFile(int theValue) {
  //String[][][] myArray = new String[][][];
  if (!outputInitialized) {
    return;
  }
  int largest = 0;
  for (int i=0; i<totSamples.length; i++) {
    if (totSamples[i]> largest) {
      largest = totSamples[i];
    }
    //myArray[i] = list_Samples[i].getListBoxItems();
  }
  String fname = ano + nf(mes, 2)+ nf(dia, 2) + nf(hora, 2) + nf(min, 2) + nf(sec, 2);
  foutput = createWriter("dados_"+fname +".csv");



  for (int i=0; i<largest; i++) {
    String line ="";
    String[] val = new String[6];
    for (int j=0; j < numSamples; j++) {
      if (i < totSamples[j]) {
        ListBoxItem lb1 = list_Samples[j].getItem(i);
        val[j]=lb1.getText();
      } else {
        val[j]="";
      }
      line += val[j]+",";
    }    
    if (outputInitialized) {
      foutput.println(i+","+line);
    }
  }
  foutput.flush();  // Writes the remaining data to the file
  foutput.close();
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PH_tributirina3" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
