package com.example.corndiseasedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InformationActivity extends AppCompatActivity {

    private ImageView imagen;
    private TextView nombreenfermedad, nombreingles, otronombre, telemorfo, anemorfo, descripcione, informacion, enfermedades;
    private String enlaceinformacion, enlaceenfermedades;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        imagen=findViewById(R.id.imageView_maiz);
        nombreenfermedad=findViewById(R.id.txt_nomb_enfermedad);
        nombreingles=findViewById(R.id.txt_nomb_ingles);
        otronombre=findViewById(R.id.txt_otros_nombres);
        telemorfo=findViewById(R.id.txt_telomorfo);
        anemorfo=findViewById(R.id.txt_anamorfo);
        descripcione=findViewById(R.id.txt_descripcion);
        informacion=findViewById(R.id.txt_informacion_obt);
        enfermedades=findViewById(R.id.txt_cons_enfermedades);
        Integer id;
        Bundle parametros = this.getIntent().getExtras();
        if(parametros !=null){
            id = parametros.getInt("codigo");
            try {
                extraerInfoJson(id);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        informacion.setOnClickListener(informacionenlace);
        enfermedades.setOnClickListener(enfermedadesenlace);

    }

    public String readFile(String filename) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));
        String content = "";
        String line;
        while((line=reader.readLine())!=null){
            content=content+line;
        }
        return content;
    }

    private void extraerInfoJson(int id) throws IOException, JSONException{
        String jsonFileContent = readFile("maize_information.json");
        JSONArray jsonArray = new JSONArray(jsonFileContent);
        for(int i =0;i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Integer idmaiz = jsonObject.getInt("id");
            if(id==idmaiz){
                String nombre_imagen= jsonObject.getString("nombre_imagen");
                imagen.setImageResource(this.getResources().getIdentifier(nombre_imagen, null, this.getPackageName()));
                String nombre_enfermedad= jsonObject.getString("nombre_enfermedad");
                nombreenfermedad.setText(nombre_enfermedad);
                String nombre_ingles= jsonObject.getString("nombre_ingles");
                nombreingles.setText(nombre_ingles);
                String otros_nombres= jsonObject.getString("otros_nombres");
                otronombre.setText(otros_nombres);
                String telomorfo= jsonObject.getString("telomorfo");
                telemorfo.setText(telomorfo);
                String anamorfo= jsonObject.getString("anamorfo");
                anemorfo.setText(anamorfo);
                String descripcion= jsonObject.getString("descripcion");
                descripcione.setText(descripcion);
                String nombre_fuente= jsonObject.getString("nombre_fuente");
                informacion.setText(nombre_fuente);
                String enlace_01= jsonObject.getString("enlace_01");
                enlaceinformacion=enlace_01;
                String mas_informacion= jsonObject.getString("mas_informacion");
                enfermedades.setText(mas_informacion);
                String enlace_02= jsonObject.getString("enlace_02");
                enlaceenfermedades=enlace_02;
            }
        }
    }

    private View.OnClickListener informacionenlace = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Uri uri= Uri.parse(enlaceinformacion);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };

    private View.OnClickListener enfermedadesenlace = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Uri uri= Uri.parse(enlaceenfermedades);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };
}