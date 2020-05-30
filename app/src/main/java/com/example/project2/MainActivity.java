package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button openCamera, openGallery, getText, translate;
    TextView textWord, textTranslate;
    ImageView image;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    Uri selectedImage;
    boolean flat = true, ok = false;
    String result;
    String translateWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openCamera = findViewById(R.id.button_open_camera);
        openGallery = findViewById(R.id.button_open_gallery);
        getText = findViewById(R.id.button_get_text);
        textWord = findViewById(R.id.text_word);
        textWord.setMovementMethod(new ScrollingMovementMethod());
        image = findViewById(R.id.image);
        textTranslate = findViewById(R.id.text_translate);
        textTranslate.setMovementMethod(new ScrollingMovementMethod());
        translate = findViewById(R.id.translate);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ok = true;
                dispatchTakePictureIntent();
                flat = true;


            }
        });
        getText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = "";
                detechTextFromImage();

            }
        });
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ok = true;
                dispatchTakePictureIntent1();
                flat = false;

            }
        });
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result == null|| result.equals("")) {
                    Toast.makeText(MainActivity.this, "Chưa có từ nhận diện", Toast.LENGTH_LONG).show();
                } else {
                    translate();
                }
            }
        });

    }

    private void translate() {
        TranslateAPI translateAPI = new TranslateAPI(Language.AUTO_DETECT,
                Language.VIETNAMESE,
                result
        );
        translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
            @Override
            public void onSuccess(String s) {
                textTranslate.setText(s);
            }

            @Override
            public void onFailure(String s) {
                textTranslate.setText("Not Found!");
            }
        });

    }

    private void dispatchTakePictureIntent1() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 0);
    }

    private void detechTextFromImage() {
        if (ok == false) {
            Toast.makeText(MainActivity.this, "Bạn chưa chọn ảnh", Toast.LENGTH_LONG).show();
        } else {

            FirebaseVisionImage firebaseVisionImage = null;
            if (flat) {
                firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
            } else {
                try {
                    firebaseVisionImage = FirebaseVisionImage.fromFilePath(getBaseContext(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
            firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    displayTextFromImage(firebaseVisionText);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("Error: ", e.getMessage());
                }
            });
        }
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blocks = firebaseVisionText.getBlocks();
        if (blocks.size() == 0) {
            textWord.setText("Not text found in image!");
            textWord.setTextColor(Color.RED);
        } else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                String text = block.getText();
                result += text + " \n";
            }
            textWord.setText(result);
        }

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(imageBitmap);
        }
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Log.d("BacNT", "OK");
//            imageBitmap= (Bitmap)data.getExtras().get("data");
//            image.setImageBitmap(imageBitmap);
            selectedImage = data.getData();
            image.setImageURI(selectedImage);
        }
    }
}
