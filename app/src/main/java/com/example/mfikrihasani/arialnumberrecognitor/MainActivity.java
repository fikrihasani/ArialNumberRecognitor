package com.example.mfikrihasani.arialnumberrecognitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int PICK_IMAGE = 1;
    Uri imageURI;
    Bitmap bitmap;
    Bitmap scaledBitmap;
    Bitmap scaledBitmapCopy;
    ImageView imageView;
    ImageView bwImageView;
    TextView textView;
    ChainCode[] chainCodes;
    String chainCodeResult;
    String[] chainCodeList = new String[10];
    Integer[] chainCodeDif = new Integer[10];
    List<ChainCode> cd = new ArrayList<ChainCode>();
    int xImg, yImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set view to class
        Button load = findViewById(R.id.loadImage);
        Button process = findViewById(R.id.process);
        textView = findViewById(R.id.textResult);
        imageView = findViewById(R.id.imageView);
        bwImageView = findViewById(R.id.bwImageView);

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    //cek apakah image ada atau tidak
    private boolean hasImage(@NonNull ImageView img){
        Drawable drawable = img.getDrawable();
        boolean hasImage = (drawable != null);
        if(hasImage && (drawable instanceof BitmapDrawable)){
            hasImage = ((BitmapDrawable) drawable).getBitmap()!=null;
        }
        return hasImage;
    }

    //buka gallery
    private void openGallery(){
        Intent gallery =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            // Load Image File
            imageURI = data.getData();
            bitmap = null;
            scaledBitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                if (bitmap.getHeight() == bitmap.getWidth()) {
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 380, 380);
                } else if (bitmap.getHeight() > bitmap.getWidth()) {
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 380, 260);
                } else {
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 260, 380);
                }

                scaledBitmapCopy = scaledBitmap.copy(Bitmap.Config.ARGB_8888,true);
                int height = scaledBitmap.getHeight();
                int width = scaledBitmap.getWidth();
//                scaledBitmapCopy = toBW(scaledBitmap, height, width);

                //make a list of chain code for dynamic arra

                ChainCode chainCode = new ChainCode(1,2);
                cd.add(chainCode);

                imageView.setImageBitmap(scaledBitmap);
//                if(toBW(0,0, height, width)){
//                }
//                toBW(0,0,height,width,0);
                chainCodeResult = "";
                toBW();
                findImage();
//                xImg = yImg = 0;
//                xImg = Color.red(scaledBitmapCopy.getPixel(xImg+1,yImg));
                getChainCode(xImg,yImg);
                String textX = Integer.toString(xImg);
                String textY = Integer.toString(yImg);
                //init chain code
                initChainCodeArray();
                //compare result
                for (int i = 0; i<10; i++){
//                    Log.d("cekString", "onActivityResult: "+chainCodeList[i].charAt(i));
                    checkDif(chainCodeResult, chainCodeList[i],i);
                }

                //get max
                int position = getMin();
                String angka = Integer.toString(position);
                Log.d("Print Chain Code Result", "Chain Code Result: "+chainCodeResult);
                textView.setText("ChainCode: "+angka);
//                textView.setText("Color: "+textX);
//

                bwImageView.setImageBitmap(scaledBitmapCopy);
//                if(toBW(0,0, height, width)){
//                    bwImageView.setImageBitmap(scaledBitmapCopy);
//                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //get max
    private Integer getMin(){
        int min = chainCodeDif[0];
        int position = 0;
        for (int i = 0; i<10;i++){
            if (chainCodeDif[i] <= min){
                min = chainCodeDif[i];
                Log.d("minimum", "getMin: "+min);
                position = i;
            }
        }
        return position;
    }

    //check the diff value of chaincode
    private void checkDif(String a, String b, Integer chain){
        int lengthA = a.length();
        int lengthB = b.length();
        int lengthLoop;
        if (lengthA >= lengthB){
            lengthLoop = lengthA;
        }else{
            lengthLoop = lengthB;
        }

        int sumError = 0;
        for (int i = 0; i < lengthLoop; i++){
            char aChar,bChar;

            if (i >= lengthA){
                bChar = b.charAt(i);
                sumError += Math.pow(Character.getNumericValue(bChar),2);
            }else if(i >= lengthB){
                aChar = a.charAt(i);
                sumError += Math.pow(Character.getNumericValue(aChar),2);
            }else{
                aChar = a.charAt(i);
                bChar = b.charAt(i);
                sumError += Math.pow(Character.getNumericValue(aChar) - Character.getNumericValue(bChar),2);
            }
        }
        chainCodeDif[chain] = sumError;
    }


    //init array for chain code
    private void initChainCodeArray(){
        chainCodeList[0] = "000000000000000000000000000000000000000000210000000210000002210002100002100022100002111121000211100221111222111121002221122211112221122222112221002222221122222112222211222222221122222222210022222222222222222211222222222222222222222222222222222222222222222222222222222222222222222222222222222222224322222222222222222244322222222222224322222222432222222224322222243222222443222432222432222432222432224322443332222433322433433322433334432244334433443224444334433444444432244444444444433444444444444444444444444654444444444665444444446544465444444665554446544446655555565554465565555655665446655665565566556665566666544666655666666655666665566666666665566666666665446666666666666666556666666666666666666666666666666666666666666666666666666666666666666666666666666666666666660066666666666666666660006666666666666600666666600666666666006666666006666600066666660066666006666600666600666006666000666006600666660066006660006600006600666006600660000066600660000000660000666000000066000000006";
        chainCodeList[1] = "0000000000000000000222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222444444444444444444444444444666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666654332243343333224433334333343343343333433433433433433444334446666666666666666666666666666666006606600066000066000660006600660006600066066006600066006600066066006600660660066000660660066006606600666606600660600666600660660066660666";
        chainCodeList[2] = "000000000000221000000000000000000000000222100000000000221000000022210002221000000022100022210002222221000221000222222210000222222222100022222222222222222222222222222222222444322222222224444322222244432224443222222444322444322244432222224443222444322444322244432224443224443222444444432244432224443222444432244432224443224444444322244432244432224443222444322444322244444443224443222444322244432222224443224444322210000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002222222222222222222222222222444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444666666666666666660000666666660000666666600006666666000066600006666666600006660000666600006660000666600006666000066600006666000006660000666600000000666000066660000666600006660000666600000000666000066660000666600006660000666600006660000666600000666600006660000666600006660000666600006666666000066666660000666666600006666666666666666666666666666666665444666666544466544466654446665444665444444466654444444444444444444444444444444444444444444444443222444444432244432224443222444322444432222224443222222222222224443222222444444444444444444444444444444466666666666666666666600006666666666000066666666000066600006666666000066660000666000000006666000066660000000006660000000000006666000000000000000000000000000066";
        chainCodeList[3] = "00000000000000222100000000000000000000002210000000000002221000000002221000000000222100022210000222100022210000222222210002221000022222222222100002222222222222222222222222222222222244443222222244443222222444322244443222444322244443222444322244443222100002221000000002221000000002221000222100002221000022210002222222100002222222100022222222222222222222222222222222222222222222222222444322222222222444432222222444322244443222444432224443222444432224443222444432224444444432224444444432224444444444444322244444444444444444444444444444444444444444444444444444444444444466654444444444446665444444446665444444444666544466654444666544466654444666544466666665444466666665444466666665444666666666666666666600000000000000666600000000000000000000022210000222222222222222100002222222100022210000222222210002221000000002221000000002221000000000000000000000000000000000000000000000066660000000006666000000000666600000666600006666000006666666600000666666660000666666666666666666666666666666666666666544466666665444466666665444466654446665444444446665444444446665444444444444444444666544444444444432224444444444444444446666666666666666666666600000666600000000000000000000000000066660000000000666600000000066660000000006666000066600000666666666666000066666666666666666666666666666666544466666665444466654446665444466654444444466654444444444444444444444444444444444444444444443222444444443222444444443222444322222224444322222224443222222222224444322222224444444444444666544444444444444444444446666666666666666666000006666666600000666666660000666666660000066660000666600000666600006666000000000066660000000006666000000000666000000000000000000000000000666";
        chainCodeList[4] = "00000000000000000000000000000022222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222210000222100000000000000000000000000000000000000222222222222222222222222222244443222444444444444444444444444444444444444443222222222222222222222222222222222222222222222222222222222222222444444444444444444444444444444444666666666666666666666666666666666666666666666666666666666666666544444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444466654444666666666666666666666666666600000666666660000666600006666666600000666600006666666600006666000006666000066666666000066660000066666666000066660000666600000666666660000666660000666666660000066660000666666660000666600000666600006666666600006666000006666666600006666000066660000066666666000066660000666666660000066660000666600006666666600000666660000666";
      chainCodeList[5] = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000022222222222222222222222222222224444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444322244432222222222222222222444322222222222222222224444322222222222222222222444322210002221000006666000000006666000000000666600000000000000000666660000000000000000000000000000002222100000000000000002221000000002221000000022210000222100000002221000022210002221000222222210000222100022222222222100022222222222100002222222222222222222222222222222222222222222222224444322222222222444322222224443222222244443222222244432224443222444432224443222444322244443222444444432224444444432224444444444443222444444444444444444444444444444444444444444444444444444444444666544444444444466654444444666544444444666544466654444666544466654446665444466666665444666666654446666666544446666666666666666666000000000000000000000000006666000000002221000222222222221000022222221000222222210002221000022210002221000222100000000222100000000000000000000000000000000000000000006666000000000666600000000066660000666600006666000006666666600006666000066666666666600000666666666666666666666666666666666666666665444466666666666544466666665444666544446665444666544466654444666544444444444466654444444444444444444444444444444444444444443222444444443222444444432224444322244432224443222444444444444444444444444444444444466654446666666000066666666666666666666000066666666666666666666000006666666666666666666660000666666666666666666660000666666666666666666666666000006666666666666666666600006666666";
      chainCodeList[6] = "0000000002221000000000000000000000000000222100000000000022100002221000000002221000022100022210000222100022222210000222222210000222222222210002222224443222444444444444444444444444444444444444666666666654444666666654446654444666544466654444666544444444665444444444444444444444444444444444444444444444322444322244444444322244443222444322444432222222444322222244443222222444432222222222222244432222222222222222222222222444432222222222100000000066660000066600000666600006666000006660000000006666000000000666600000000000000000066600000000000000000000000000000002210000000000000222100000000000022210000221000000002221000022210002210000222100022222210000222100002222221000222222222222221000022222222222222222222222222222222222222222224444322222222222444322222244443222222444432222222444322444432224443222444432244432224444444443222444444443222444444443224444444444444444444444444444444444444444444444444444444444444446654444444444446665444444446665444466654444665444444446665444666544446666665444666544446666665444466666666665444666666666654444666666666666666666544466666666666666666666666666666666666666666666666666666666666666666666666666660000666666666666666666666600000666666666666666000066666660000066666666000006666666000066666666000006660000666600000666600006660000066660000000006666000000000666000000000000006666000000000000000000000000000666";
      chainCodeList[7] = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002222222222222222222224443222222444432224443222444432244432222222444322444432224443222222444432224443222222444432224443222222444322222244443222222244432222224444322222244432222222444322222244443222222444322222222224444322222224443222222222244432222222222444432222222222222444322222222222222444432222222222222222222222224443222222222222222222444444444444444444444444444444444444444666666666666666666000006666666666666666666666000066666666666666666600000666666666666660000666666666660000666666666660000066666666666000066666666666000006666666600006666666000066666666666000006666000066666660000066666660000666666600006666666600000666000066666666000006660000666666660000666000006666000066666660000066660000666654444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444446654444666666666666666666666666";
      chainCodeList[8] = "0000000000000022210000000000000000000002221000000000000222100000000222100022210000000022210002221000022222221000222100002222222210002222222222222222222222222222222222222222222222224443222222244443222222244432224444322244444444322244432224444444432221000022221000000022210000000022210000222100022210000222100022210002222222100002222222100022222222222222222222222210000222444432222222222222222222222224443222222244443222222244432222222444322244443222444322244443222444444443222444322224444444432224444444444443222444444444444444444444444444444444444444444444444444444444444444446665444444444444666544444444666654444444466654446665444466654446665444466654446665444666666654444666666654446666666666666666544446666666666666666666666666660000066666666666666666000066666666000006666666600006666000066660000066660000666600000000066660000000006666600006666544466654444444466654446665444466654446665444466666665444666666666666544466666666666666666666666666666660000666666666666000066666666600000666600006666000006666000066660000066660000666600000000066660000000000000666600000000000000000000000000666";
      chainCodeList[9] = "000000000000022221000000000000000000000000002222100000002221000000002222100000000222100022221000022221000222100002222100022210000222222221000222210002222222222222100002222222222221000222222222222222222222222222100002222222222222222222222222222222222222222222222222222222222222222222222222222222222222244443222222222222222222222222224443222222222222222222444432222222222224443222222222444322222222444432224443222244443222222224443222244444444322244432222444444443222444444443222244444444322224444444444444444444444444444444444444444444444444444444444446666544444444444446666544444446665444466665444666544446666544466665444466666666544466666666544466666666666666666544446666000006666000000000000000000000000000000000022222222222222222100022221000022222222100022210000222210000000222100000000000000000000000000000000000000000000666600000000066666000000000666600006666600000666666666000066666000006666666660000666666666666600006666666666666666666000006666666666666666666666666666666666666666654444322224443222444322224444322244444444322224443222444444444444322224444444444444444432222444444444444444444444444466665444444444444444446666544444444666544444446666544446665444666654444666544466665444466666666544466665444666666666666544446666666666666544466666666666666666666666666666666666666666666666666666600006666666666666666660000066666666660000666666666000066660000066666666600006666600000000066666000006666000066666000000000666600000000000006666600000000000000000000006666";
    }
    //floodfill method
    private Boolean floodfill(int xMin, int xMax, int yMin, int yMax, int colour, int x, int y){
        int pixel = scaledBitmapCopy.getPixel(x,y);
        int currColour = Color.red(pixel);
        if (x == xMin | x==xMax | y == yMax | y == yMin | colour == currColour){
            return true;
        }else{
            scaledBitmapCopy.setPixel(x,y,colour);
            return (floodfill(xMin,xMax,yMin,yMax,colour,x+1,y) | floodfill(xMin,xMax,yMin,yMax,colour,x+1,y+1) | floodfill(xMin,xMax,yMin,yMax,colour,x,y+1) | floodfill(xMin,xMax,yMin,yMax,colour,x-1,y+1) | floodfill(xMin,xMax,yMin,yMax,colour,x-1,y) | floodfill(xMin,xMax,yMin,yMax,colour,x-1,y-1) | floodfill(xMin,xMax,yMin,yMax,colour,x,y-1) | floodfill(xMin,xMax,yMin,yMax,colour,x+1,y-1));
        }
//        return ;
    }

    //mengubah warna jadi hitam putih
    private void toBW(){
        int height = scaledBitmapCopy.getHeight();
        int width = scaledBitmapCopy.getWidth();
        int pixel, red, green, blue, bw, bwColour;
        for(int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                pixel = scaledBitmapCopy.getPixel(x,y);
                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);
                bw = (red+blue+green)/3;
                if(bw >= 128){
                    bw = 255;
                }else{
                    bw = 0;
                }
                scaledBitmapCopy.setPixel(x,y,Color.rgb(bw,bw,bw));
            }
        }
    }

    //find object
    private void findImage(){
//        int x,y;
        Boolean find = false;
        int y = 0;
        while (y < scaledBitmapCopy.getHeight() && !find){
            int x = 1;
            while (x < scaledBitmapCopy.getWidth() && !find){
                if (Color.red(scaledBitmapCopy.getPixel(x,y)) != Color.red(scaledBitmapCopy.getPixel(x-1,y))){
                    xImg = x;
                    yImg = y;
                    find = true;
                }else{
                    x++;
                }
            }
            y++;
        }

    }

    //get chain code
    private void getChainCode(int xAwal, int yAwal){
        Boolean bukanAwal = true;
        System.out.println("XAwal: "+xAwal+", YAwal: "+yAwal);

        int x,y,xBefore,yBefore, xSide, ySide;
        String direction;

        x = xAwal+1;
        y = yAwal;

        ChainCode subCd = new ChainCode(x,y);
        cd.add(subCd);

        xBefore = xAwal;
        yBefore = yAwal;

        xSide = xAwal;
        ySide = yAwal-1;

        direction = "0";
        Boolean out = false;

        while(bukanAwal){
            chainCodeResult += direction;
            switch (direction){
                case "0":
                    x = xBefore+1;
                    y = yBefore;
                    xSide = x;
                    ySide = y-1;
                    break;
                case "1":
                    x = xBefore+1;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y-1;
                    break;
                case "2":
                    x = xBefore;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y;
                    break;
                case "3":
                    x = xBefore-1;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y+1;
                    break;
                case "4":
                    x = xBefore-1;
                    y = yBefore;
                    xSide = x;
                    ySide = y+1;
                    break;
                case "5":
                    x = xBefore-1;
                    y = yBefore-1;
                    xSide = x-1;
                    ySide = y+1;
                    break;
                case "6":
                    x = xBefore;
                    y = yBefore-1;
                    xSide = x-1;
                    ySide = y;
                    break;
                case "7":
                    x = xBefore+1;
                    y = yBefore-1;
                    xSide = x-1;
                    ySide = y-1;
                    break;
            }

            //mendapatkan value pixel sekarang, pixel sebelum, dan pixel samping.
            int pixel= scaledBitmapCopy.getPixel(x,y);
            int pixelSide = scaledBitmapCopy.getPixel(xSide,ySide);
            int pixelBefore = scaledBitmapCopy.getPixel(xBefore,yBefore);
            Log.d("chainmovement", "XBefore: "+xBefore+", YBefore: "+yBefore+", X: "+x+", Y: "+y+", XSide: "+xSide+", YSide: "+ySide+", Direction: "+direction+" Warna: "+Color.red(pixelBefore)+", Warna X: "+Color.red(pixel)+"Warna Samping: "+Color.red((pixelSide)));
            System.out.println();

            //check colour
            if (Color.red(pixelSide) != Color.red(pixel)){
                subCd = new ChainCode(x,y);
                cd.add(subCd);
                if (x == xAwal && y==yAwal){
                    bukanAwal = false;
                }
                xBefore = x;
                yBefore = y;
            }else if(Color.red(pixel) != Color.red(pixelBefore)){
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore)) == Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore))&&(direction.equals("0") | direction.equals("2") | direction.equals("6") | direction.equals("7") | direction.equals("1")))){
                    direction = "0";
                    x = xBefore + 1;
                    y = yBefore;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1)) == Color.red(pixelBefore))&& (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1)) != Color.red(scaledBitmapCopy.getPixel(xBefore+2,yBefore)))&&(direction.equals("0") | direction.equals("2") | direction.equals("7") | direction.equals("1") | direction.equals("5"))){
                    direction = "1";
                    x = xBefore+1;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+1)) ==  Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+1)) !=  Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1))) &&(direction.equals("0") | direction.equals("2") | direction.equals("4") | direction.equals("1") | direction.equals("3"))){
                    direction = "2";
                    x = xBefore;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1)) != Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+2))) && (direction.equals("2") | direction.equals("4") | direction.equals("1") | direction.equals("3") | direction.equals("5"))){
                    direction = "3";
                    x = xBefore-1;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore)) != Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1))) && (direction.equals("2") | direction.equals("6") | direction.equals("4") | direction.equals("3") | direction.equals("5"))){
                    direction = "4";
                    x = xBefore-1;
                    y = yBefore;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore-2,yBefore))) && (direction.equals("6") | direction.equals("4") | direction.equals("7") | direction.equals("3") | direction.equals("5"))){
                    direction = "5";
                    x = xBefore-1;
                    y = yBefore-1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore-1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1))) && (direction.equals("0") | direction.equals("6") | direction.equals("4") | direction.equals("7") | direction.equals("5"))){
                    direction = "6";
                    x = xBefore;
                    y = yBefore-1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1, yBefore-1)) == Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore+1, yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore, yBefore-2))) && (direction.equals("0") | direction.equals("6") | direction.equals("7") | direction.equals("1") | direction.equals("5"))){
                    direction = "7";
                    x = xBefore+1;
                    y = yBefore-1;
                }
                xBefore = x;
                yBefore = y;

            }else if(Color.red(pixel) == Color.red(pixelBefore)){
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore)) == Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore))&&(direction.equals("0") | direction.equals("2") | direction.equals("6") | direction.equals("7") | direction.equals("1")))){
                    direction = "0";
                    x = xBefore + 1;
                    y = yBefore;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1)) == Color.red(pixelBefore))&& (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1)) != Color.red(scaledBitmapCopy.getPixel(xBefore+2,yBefore)))&&(direction.equals("0") | direction.equals("2") | direction.equals("7") | direction.equals("1") | direction.equals("5"))){
                    direction = "1";
                    x = xBefore+1;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+1)) ==  Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+1)) !=  Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1))) &&(direction.equals("0") | direction.equals("2") | direction.equals("4") | direction.equals("1") | direction.equals("3"))){
                    direction = "2";
                    x = xBefore;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1)) != Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+2))) && (direction.equals("2") | direction.equals("4") | direction.equals("1") | direction.equals("3") | direction.equals("5"))){
                    direction = "3";
                    x = xBefore-1;
                    y = yBefore+1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore)) != Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1))) && (direction.equals("2") | direction.equals("6") | direction.equals("4") | direction.equals("3") | direction.equals("5"))){
                    direction = "4";
                    x = xBefore-1;
                    y = yBefore;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore-2,yBefore))) && (direction.equals("6") | direction.equals("4") | direction.equals("7") | direction.equals("3") | direction.equals("5"))){
                    direction = "5";
                    x = xBefore-1;
                    y = yBefore-1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore-1)) == Color.red(pixelBefore)) &&(Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore-1)) != Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1))) && (direction.equals("0") | direction.equals("6") | direction.equals("4") | direction.equals("7") | direction.equals("5"))){
                    direction = "6";
                    x = xBefore;
                    y = yBefore-1;
                }else
                if ((Color.red(scaledBitmapCopy.getPixel(xBefore+1, yBefore-1)) == Color.red(pixelBefore)) && (Color.red(scaledBitmapCopy.getPixel(xBefore+1, yBefore-1)) == Color.red(scaledBitmapCopy.getPixel(xBefore, yBefore-2))) && (direction.equals("0") | direction.equals("6") | direction.equals("7") | direction.equals("1") | direction.equals("5"))){
                    direction = "7";
                    x = xBefore+1;
                    y = yBefore-1;
                }
                xBefore = x;
                yBefore = y;

            }
        }
    }

    //recognize number from image
//    private String getNumFromImage(Bitmap bitmap, int xAwal, int xAkhir, int backColour){
//        int pixel = pixel.
//    }

    //resize bitmap biar ga kegedean
    private Bitmap getResizedBitmap(Bitmap bm, int height, int width, int newHeight, int newWidth){
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // crate matrix for manipulation
        Matrix matrix = new Matrix();
//        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);
//        // recreate the new bitmap;
        Bitmap newBitmap = Bitmap.createBitmap(bm, 0,0,width,height,matrix, false);
        bm.recycle();

        return newBitmap;
    }
}
