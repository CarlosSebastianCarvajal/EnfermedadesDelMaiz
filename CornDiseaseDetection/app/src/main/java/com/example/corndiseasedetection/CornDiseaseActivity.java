package com.example.corndiseasedetection;

import androidx.activity.result.ActivityResultLauncher;
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
    private TextView txtInfoAd, txtMasInformacion;
    private Bitmap imageBitmap;
    private ActivityResultLauncher<Intent> mStartForResult;
    private int imageSize = 224;

    private int id_class_mayor_precision;

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

        txtInfoAd = findViewById(R.id.txtInfoAd);
        txtMasInformacion = findViewById(R.id.txtMasInformacion);

        btnCapturarImg.setOnClickListener(btnAbrirCapturaImg);
        btnSubirImg.setOnClickListener(btnAbrirSubirImg);
        txtMasInformacion.setOnClickListener(txtVermasEnlace);

        txtMasInformacion.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener txtVermasEnlace = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Bundle extras = new Bundle();
            extras.putInt("id_class", id_class_mayor_precision);
            Intent intent;
            intent = new Intent(getApplicationContext(),
                    InformationActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
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
            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //mStartForResult.launch(intent);
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
            Toast.makeText(this, "Clasificando imagen", Toast.LENGTH_SHORT).show();
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for referen
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
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

            String[] classes = {"Roya común",
                                "Tizón de la hoja del norte",
                                "Mancha gris de la hoja",
                                "Saludable",
                                "Otra planta"};

            int[] ids = {0,1,2,3,4};
            float[] confidences = outputFeature0.getFloatArray();

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

            model.close();

            float temp = 0;
            for (int i = 0; i < classes.length; i++){
                if (temp < confidences[i]){
                    temp = confidences[i];
                    id_class_mayor_precision = i;
                }
            }
            extraerInfoJson(id_class_mayor_precision);
            txtMasInformacion.setVisibility(View.VISIBLE);
        }
        catch (IOException | JSONException e) {}
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

    private void extraerInfoJson(int id) throws IOException, JSONException {
        String jsonFileContent = readFile("maize_information.json");
        JSONArray jsonArray = new JSONArray(jsonFileContent);

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