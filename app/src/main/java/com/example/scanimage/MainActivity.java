package com.example.scanimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.vision.text.TextRecognizer.Builder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button nbutton_capture,nbutton_copy;
    TextView ntext_data;
    Bitmap bitmap;
    private static final int REQUEST_CAMERA_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(ContextCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMERA_CODE);
        };

        ntext_data = findViewById(R.id.text_data);
        nbutton_capture = findViewById(R.id.button_capture);
        nbutton_copy = findViewById(R.id.button_copy);

        nbutton_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);

            }
        });

        nbutton_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scanned_text = ntext_data.getText().toString();
                copyToClipboard(scanned_text);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                    getTextfrmImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void getTextfrmImage(Bitmap bitmap){

        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        if (!textRecognizer.isOperational()){

            Toast.makeText(this, "error occurred.text", Toast.LENGTH_SHORT).show();

        }else{

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textblockSparseArray = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();

            for (int i =0;i<textblockSparseArray.size();i++){
                TextBlock textBlock = textblockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            ntext_data.setText(stringBuilder.toString());
            nbutton_capture.setText("Retake");
            nbutton_copy.setVisibility(View.VISIBLE);
        }

    }
    private void copyToClipboard(String text){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clipData = ClipData.newPlainText("copied Data",text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "copied to clipBoard", Toast.LENGTH_SHORT).show();

    }
}