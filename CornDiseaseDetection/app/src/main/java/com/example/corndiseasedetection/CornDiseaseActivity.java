package com.example.corndiseasedetection;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.corndiseasedetection.ml.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.jar.Attributes;

public class CornDiseaseActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_FILE = 2;
    private Button btnCapturarImg, btnSubirImg;
    private ImageView imgCamara;
    private TextView txtEnfer_1, txtProb_1;
    private TextView txtEnfer_2, txtProb_2;
    private TextView txtEnfer_3, txtProb_3;
    private TextView txtEnfer_4, txtProb_4;
    private TextView txtEnfer_5, txtProb_5;
    private TextView txtEnfer_6, txtProb_6;
    private TextView txtEnfer_7, txtProb_7;
    private TextView txtEnfer_8, txtProb_8;
    private TextView txtEnfer_9, txtProb_9;
    private TextView txtEnfer_10, txtProb_10;
    private TextView txtInfoAd, txtVermas;
    private Bitmap imageBitmap;
    private ActivityResultLauncher<Intent> mStartForResult;
    private int imageSize = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corn_disease);
        //this.registrarActivityResult();

        btnCapturarImg = findViewById(R.id.btnCapturarImg);
        btnSubirImg = findViewById(R.id.btnCargarImg);
        imgCamara = findViewById(R.id.imgDeCamara);

        txtEnfer_1 = findViewById(R.id.txtEnfermedad_1);
        txtProb_1 = findViewById(R.id.txtProbabilidad_1);

        txtEnfer_2 = findViewById(R.id.txtEnfermedad_2);
        txtProb_2 = findViewById(R.id.txtProbabilidad_2);

        txtEnfer_3 = findViewById(R.id.txtEnfermedad_3);
        txtProb_3 = findViewById(R.id.txtProbabilidad_3);

        txtEnfer_4 = findViewById(R.id.txtEnfermedad_4);
        txtProb_4 = findViewById(R.id.txtProbabilidad_4);

        txtEnfer_5 = findViewById(R.id.txtEnfermedad_5);
        txtProb_5 = findViewById(R.id.txtProbabilidad_5);

        txtEnfer_6 = findViewById(R.id.txtEnfermedad_6);
        txtProb_6 = findViewById(R.id.txtProbabilidad_6);

        txtEnfer_7 = findViewById(R.id.txtEnfermedad_7);
        txtProb_7 = findViewById(R.id.txtProbabilidad_7);

        txtEnfer_8 = findViewById(R.id.txtEnfermedad_8);
        txtProb_8 = findViewById(R.id.txtProbabilidad_8);

        txtEnfer_9 = findViewById(R.id.txtEnfermedad_9);
        txtProb_9 = findViewById(R.id.txtProbabilidad_9);

        txtEnfer_10 = findViewById(R.id.txtEnfermedad_10);
        txtProb_10 = findViewById(R.id.txtProbabilidad_10);

        txtInfoAd = findViewById(R.id.txtInfoAd);
        txtVermas = findViewById(R.id.txtVermas);

        btnCapturarImg.setOnClickListener(btnAbrirCapturaImg);
        btnSubirImg.setOnClickListener(btnAbrirSubirImg);
        txtVermas.setOnClickListener(txtVermasEnlace);

        txtVermas.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener txtVermasEnlace = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //Aqui debe enviar al Activity con toda la informacion...
            Toast.makeText(CornDiseaseActivity.this, "Se debe ir al otro activity", Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener btnAbrirCapturaImg = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                //Request camera permission if we don't have it.
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //mStartForResult.launch(intent);
        }
    };

    private View.OnClickListener btnAbrirSubirImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Seleccione una imagen"),
                    REQUEST_SELECT_FILE);
            //mStartForResult.launch(intent);

            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //mStartForResult1.launch(intent);
        }
    };

    private View.OnClickListener btnIniciarDetectorEnf = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            classifyImage(imageBitmap);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri selectedImageUri = null;
        Uri selectedImage;

        String filePath = null;
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == CornDiseaseActivity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath = selectedImage.getPath();
                    if (requestCode == REQUEST_SELECT_FILE) {

                        if (selectedPath != null) {
                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            imageBitmap = BitmapFactory.decodeStream(imageStream);
                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                            //imageView.setImageBitmap(imageBitmap);
                            //imagenCargada = true;

                            int dimension = Math.min(imageBitmap.getWidth(), imageBitmap.getHeight());
                            imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                            imgCamara.setImageBitmap(imageBitmap);
                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, false);

                            classifyImage(imageBitmap);
                        }
                    }
                }

                break;

            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == CornDiseaseActivity.RESULT_OK){
                    imageBitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    int dimension = Math.min(imageBitmap.getWidth(), imageBitmap.getHeight());
                    imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
                    imgCamara.setImageBitmap(imageBitmap);
                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize,false);

                    classifyImage(imageBitmap);
                }

                break;

        }
    }

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

            String[] classes = {"Tizón de la hoja por antracnosis",
                                "Roya común",
                                "Tizón de la hoja del norte",
                                "Mancha foliar por Phaeosphaeria",
                                "Fisoderma mancha marron",
                                "Tizón de la hoja del sur",
                                "Roya del maíz del sur",
                                "Roya tropical",
                                "Sana",
                                "Objeto"};
            int[] ids = {0,1,2,3,4,5,6,7,8,9};
            float[] confidences = outputFeature0.getFloatArray();

            for (int x = 0; x < confidences.length; x++){
                for (int i = 0; i < confidences.length -x -1; i++){
                    if( confidences[i] < confidences[i + 1]){
                        float f_temp = confidences[i + 1];
                        confidences[i + 1] = confidences[i];
                        confidences[i] = f_temp;

                        String c_temp = classes[i + 1];
                        classes[i + 1] = classes[i];
                        classes[i] = c_temp;

                        int i_temp = ids[i + 1];
                        ids[i + 1] = ids[i];
                        ids[i] = i_temp;
                    }
                }
            }

            txtEnfer_1.setText(classes[0]);
            txtProb_1.setText(formatear(confidences[0]) + "%");

            txtEnfer_2.setText(classes[1]);
            txtProb_2.setText(formatear(confidences[1]) + "%");

            txtEnfer_3.setText(classes[2]);
            txtProb_3.setText(formatear(confidences[2]) + "%");

            txtEnfer_4.setText(classes[3]);
            txtProb_4.setText(formatear(confidences[3]) + "%");

            txtEnfer_5.setText(classes[4]);
            txtProb_5.setText(formatear(confidences[4]) + "%");

            txtEnfer_6.setText(classes[5]);
            txtProb_6.setText(formatear(confidences[5]) + "%");

            txtEnfer_7.setText(classes[6]);
            txtProb_7.setText(formatear(confidences[6]) + "%");

            txtEnfer_8.setText(classes[7]);
            txtProb_8.setText(formatear(confidences[7]) + "%");

            txtEnfer_9.setText(classes[8]);
            txtProb_9.setText(formatear(confidences[8]) + "%");

            txtEnfer_10.setText(classes[9]);
            txtProb_10.setText(formatear(confidences[9]) + "%");

            //txtalpha2Code = classes[maxPos];
            /*String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] *100);
            }*/
            ///confidence.setText(s);
            // Releases model resources if no longer used.
            model.close();

            mostrarInfo(ids[0]);
            if(ids[0] != 8 && ids[0] != 9){
                txtVermas.setVisibility(View.VISIBLE);    
            }
        } catch (IOException | JSONException e) {}
    }

    private void mostrarInfo(int id) throws IOException, JSONException {
        String jsonFileContent = readFile("maize_information.json");
        JSONArray jsonArray = new JSONArray(jsonFileContent);
        //List<Person> persons = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++)
        {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            Integer idjs = jsonObj.getInt("id");
            if(id == idjs){
                String descripcion = jsonObj.getString("descripcion");
                txtInfoAd.setText(descripcion);
            }
        }
    }

    public String readFile(String fileName) throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName), "UTF-8"));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null)
        {
            content = content + line;
        }
        return content;
    }



    private String formatear(float valor){
        valor = valor *100;
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(2); //Define 2 decimales.
        return format.format(valor);
    }
    /*
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
                        imgCamara.setImageBitmap(imageBitmap);
                        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize,false);
                        //Luego de esto, el btn Detectar enfermedad se encarga de clasificar
                    }
                }
        );
    }*/
}