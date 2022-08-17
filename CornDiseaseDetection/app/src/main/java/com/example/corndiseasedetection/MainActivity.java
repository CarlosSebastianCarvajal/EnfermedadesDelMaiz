package com.example.corndiseasedetection;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.corndiseasedetection.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button captureImageBtn, detectDiseaseBtn;
    private ImageView imageView;
    private TextView textResult;
    private Bitmap imageBitmap;

    private ActivityResultLauncher<Intent> mStartForResult;

    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageBtn = findViewById(R.id.capture_image);
        detectDiseaseBtn = findViewById(R.id.detect_didease);

        imageView = findViewById(R.id.image_view);
        textResult = findViewById(R.id.text_result0);

        captureImageBtn.setOnClickListener(btnCapturarImg);
        detectDiseaseBtn.setOnClickListener(btnDetectarEnf);

        this.registrarActivityResult();
    }

    private View.OnClickListener btnCapturarImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mStartForResult.launch(intent);
        }
    };

    private View.OnClickListener btnDetectarEnf = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            classifyImage(imageBitmap);
        }
    };

    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            //inputFeature0.loadBuffer(byteBuffer);
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4* imageSize* imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0,0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/1));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Manchas", "Royas", "Tizón", "Sanas"};

            textResult.setText(classes[0] + " : " + formatear(confidences[0]) + "%" + "\n"+
                    classes[1]+" : " + formatear(confidences[1])+"%"+ "\n"+
                    classes[2]+" : " + formatear(confidences[2])+"%"+ "\n"+
                    classes[3]+" : " + formatear(confidences[3])+"%");

            //txtalpha2Code = classes[maxPos];
            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] *100);

            }
            ///confidence.setText(s);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {}
    }

    private String formatear(float valor){
        valor = valor *100;
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(2); //Define 2 decimales.
        return format.format(valor);
    }

    private void registrarActivityResult(){
        mStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Bundle extras = result.getData().getExtras();
                        imageBitmap = (Bitmap) extras.get("data");

                        int dimension = Math.min(imageBitmap.getWidth(), imageBitmap.getHeight());
                        imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                        imageView.setImageBitmap(imageBitmap);
                        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize,false);
                        //Luego de esto, el btn Detectar enfermedad se encarga de clasificar
                    }
                }
        );
    }
}