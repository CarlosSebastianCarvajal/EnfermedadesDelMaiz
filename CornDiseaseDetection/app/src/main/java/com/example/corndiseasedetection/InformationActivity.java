package com.example.corndiseasedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.text.util.Linkify;
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

    private ImageView imagen01, imagen02, imagen03;
    private int id;
    private TextView nombre_enfermedad;
    private TextView nombre_ingles;
    private TextView otros_nombres;
    private TextView teleomorfo;
    private TextView anamorfo;
    private TextView descripcion;
    private TextView sintomas;
    private TextView causa;
    private TextView comentario;
    private TextView administracion;
    private TextView fuente01, fuente02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        imagen01 = findViewById(R.id.img_ClasImg_01);
        imagen02 = findViewById(R.id.img_ClasImg_02);
        imagen03 = findViewById(R.id.img_ClasImg_03);
        nombre_enfermedad = findViewById(R.id.txtNombreEnfermedad);
        nombre_ingles = findViewById(R.id.txtNombreIngles);
        otros_nombres = findViewById(R.id.txtOtrosNombres);
        teleomorfo = findViewById(R.id.txtTeleomorfo);
        anamorfo = findViewById(R.id.txtAnamorfo);
        descripcion = findViewById(R.id.txtDescripcion);
        sintomas = findViewById(R.id.txtSintomas);
        causa = findViewById(R.id.txtCausa);
        comentario = findViewById(R.id.txtComentarios);
        administracion = findViewById(R.id.txtAdministracion);
        fuente01 = findViewById(R.id.txtFuente01);
        fuente02 = findViewById(R.id.txtFuente02);

        Bundle parametros = this.getIntent().getExtras();
        if(parametros !=null){
            id = parametros.getInt("id_class");

            try { extraerInfoJson(id);}
            catch (IOException | JSONException e) { e.printStackTrace(); }
        }
    }

    private void extraerInfoJson(int id) throws IOException, JSONException{
        String jsonFileContent = readFile("maize_information.json");
        JSONArray jsonArray = new JSONArray(jsonFileContent);
        JSONObject jsonObject = jsonArray.getJSONObject(id);

        String img01 = jsonObject.getJSONArray("nombre_imagen").getString(0);
        imagen01.setImageResource(getResources().getIdentifier(img01, "mipmap", getPackageName()));
        String img02 = jsonObject.getJSONArray("nombre_imagen").getString(1);
        imagen02.setImageResource(getResources().getIdentifier(img02, "mipmap", getPackageName()));
        String img03 = jsonObject.getJSONArray("nombre_imagen").getString(2);
        imagen03.setImageResource(getResources().getIdentifier(img03, "mipmap", getPackageName()));

        nombre_enfermedad.setText(jsonObject.getString("nombre_enfermedad"));
        nombre_ingles.setText(jsonObject.getString("nombre_ingles"));
        otros_nombres.setText(jsonObject.getString("otros_nombres"));
        teleomorfo.setText(jsonObject.getString("teleomorfo"));
        anamorfo.setText(jsonObject.getString("anamorfo"));
        descripcion.setText(jsonObject.getString("descripcion"));

        JSONArray jsonArraySintomas = jsonObject.getJSONArray("sintomas");
        String sintoma = "";
        for (int i = 0; i < jsonArraySintomas.length(); i++)
            sintoma = sintoma + jsonArraySintomas.getString(i) + "\n";
        sintomas.setText(sintoma);

        causa.setText(jsonObject.getString("causa"));
        comentario.setText(jsonObject.getString("comentarios"));
        administracion.setText(jsonObject.getString("administracion"));

        JSONArray jsonFuentes = jsonObject.getJSONArray("fuentes");

        fuente01.setText(jsonFuentes.getJSONObject(0).getString("titulo_fuente"));
        Linkify.addLinks(fuente01, Linkify.WEB_URLS);
        String enlace01 = jsonFuentes.getJSONObject(0).getString("enlace");
        fuente01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirEnlace(Uri.parse(enlace01));
            }
        });
        fuente02.setText(jsonFuentes.getJSONObject(1).getString("titulo_fuente"));
        Linkify.addLinks(fuente02, Linkify.WEB_URLS);
        String enlace02 = jsonFuentes.getJSONObject(1).getString("enlace");
        fuente02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirEnlace(Uri.parse(enlace02));
            }
        });
    }

    private String readFile(String filename) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));
        String content = "", line = "";

        while ( (line = reader.readLine()) != null){
            content = content+line;
        }
        return content;
    }

    private void abrirEnlace(Uri uri){
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}