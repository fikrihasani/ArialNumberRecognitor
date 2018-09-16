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
    String[] chainCodeResult;
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

    private boolean hasImage(@NonNull ImageView img){
        Drawable drawable = img.getDrawable();
        boolean hasImage = (drawable != null);
        if(hasImage && (drawable instanceof BitmapDrawable)){
            hasImage = ((BitmapDrawable) drawable).getBitmap()!=null;
        }
        return hasImage;
    }

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
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 500, 500);
                } else if (bitmap.getHeight() > bitmap.getWidth()) {
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 500, 460);
                } else {
                    scaledBitmap = getResizedBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth(), 460, 500);
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
                toBW();
                findImage();
//                xImg = yImg = 0;
//                xImg = Color.red(scaledBitmapCopy.getPixel(312,357));
                getChainCode(xImg,yImg);
                String textX = Integer.toString(xImg);
                String textY = Integer.toString(yImg);
//                textView.setText("X: "+textX+", Y: "+textY);
                textView.setText("Color: "+textX);


                bwImageView.setImageBitmap(scaledBitmapCopy);
//                if(toBW(0,0, height, width)){
//                    bwImageView.setImageBitmap(scaledBitmapCopy);
//                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    //change to bw
//    private Boolean toBW(int x, int y, int heightMax, int widthMax, int count) {
//
//        if (x<0 | y<0 | x>=widthMax | y>=heightMax | count == 10){
//            System.out.println("X = "+x+", Y = "+y+", count: "+count);
//            return true;
//        }else{
//            int pixel = scaledBitmapCopy.getPixel(x,y);
//            int red = Color.red(pixel);
//            int green = Color.green(pixel);
//            int blue = Color.blue(pixel);
//            int bw = (red+blue+green)/3;
//            if(bw >= 128){
//                bw = 255;
//            }else{
//                bw = 0;
//            }
//            scaledBitmapCopy.setPixel(x,y,Color.rgb(bw,bw,bw));
////            return true;
//            return (toBW(x,y+1,heightMax,widthMax, count+1) | toBW(x+1,y,heightMax,widthMax, count+1));
//        }
//    }
//        Bitmap bitCopy = bitmap.copy(Bitmap.Config.ARGB_8888,true);
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
        int x,y,xBefore,yBefore, xSide, ySide;
        String direction;
        x = xSide = xBefore = xAwal;
        y = ySide = yBefore = yAwal;
        direction = "t";
        while(bukanAwal){
            switch (direction){
                case "t":
                    x = xBefore+1;
                    y = yBefore;
                    xSide = x;
                    ySide = y-1;
                    break;
                case "tt":
                    x = xBefore+1;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y-1;
                    break;
                case "s":
                    x = xBefore;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y;
                    break;
                case "bd":
                    x = xBefore-1;
                    y = yBefore+1;
                    xSide = x+1;
                    ySide = y+1;
                    break;
                case "b":
                    x = xBefore-1;
                    y = yBefore;
                    xSide = x;
                    ySide = y+1;
                    break;
                case "bl":
                    x = xBefore-1;
                    y = yBefore-1;
                    xSide = x-1;
                    ySide = y+1;
                    break;
                case "u":
                    x = xBefore;
                    y = yBefore-1;
                    xSide = x-1;
                    ySide = y;
                    break;
                case "tl":
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
            System.out.println("X: "+xBefore+", Y: "+yBefore+", Direction: "+direction+", Warna: "+Color.red(pixelBefore));

            //check colour
            if (Color.red(pixelSide) != Color.red(pixel)){
                ChainCode subCd = new ChainCode(x,y);
                cd.add(subCd);
                if (x == xAwal && y == yAwal){
                    bukanAwal = false;
                }else if(Color.red(pixel) != Color.red(pixelBefore)){
                    //cek sekitar jika warna tidak sama.
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore)) == Color.red(pixelBefore)){
                        direction = "t";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore+1,yBefore+1)) == Color.red(pixelBefore)){
                        direction = "tt";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore+1)) ==  Color.red(pixelBefore)){
                        direction = "s";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore+1)) == Color.red(pixelBefore)){
                        direction = "bd";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore)) == Color.red(pixelBefore)){
                        direction = "b";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore-1,yBefore-1)) == Color.red(pixelBefore)){
                        direction = "bl";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore,yBefore-1)) == Color.red(pixelBefore)){
                        direction = "u";
                    }else
                    if (Color.red(scaledBitmapCopy.getPixel(xBefore+1, yBefore-1)) == Color.red(pixelBefore)){
                        direction = "tl";
                    }
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
