package com.example.parcial_1_am_acn4bv_napoli_oliva;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Pelicula> peliculas;
    private LinearLayout listaPeliculas;
    private EditText inputBusqueda;
    private FirebaseAuth mAuth;
    private Button btnIrFavoritos;
    private Button btnCerrarSesion;
    private Button btnUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Se inicia con Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        checkAuthentication();
    }

    private void checkAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // si el usuario no está logueado, redirigir a LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            // Si el usuario está logueado, inicia
            initializeAppLogic();
        }
    }

    private void initializeAppLogic() {
        try {
            // obtener referencias de UI de forma segura
            listaPeliculas = findViewById(R.id.listaPeliculas);
            inputBusqueda = findViewById(R.id.inputBusqueda);
            btnIrFavoritos = findViewById(R.id.btnIrFavoritos);
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
            btnUbicacion = findViewById(R.id.btnUbicacion);

            peliculas = new ArrayList<>();

            // Listeners
            btnIrFavoritos.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FavoritosActivity.class);
                startActivity(intent);
            });

            btnCerrarSesion.setOnClickListener(v -> logoutUser());

            btnUbicacion.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=Cine+Boedo+Buenos+Aires");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    //si  el usuario no tiene maps entra a este bloque y lo redirige con internet
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=Cine+Boedo+Buenos+Aires")));
                }
            });

            // filtro de busqueda
            inputBusqueda.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarPeliculas(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) { }
            });

            cargarCartelera();

        } catch (NullPointerException e) {
            Toast.makeText(this, "Error de inicialización de vistas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void logoutUser() {
        mAuth.signOut(); // cierra la sesin en Firebase

        // limpia el historial de actividades
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void cargarCartelera (){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Toast.makeText(this, "Cargando estrenos", Toast.LENGTH_SHORT).show();

        db.collection("cartelera")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        peliculas.clear();

                        for (QueryDocumentSnapshot document : task.getResult()){
                            Pelicula p = document.toObject(Pelicula.class);
                            peliculas.add(p);
                        }

                        mostrarPeliculas(peliculas);
                    } else {
                        Toast.makeText(this, "Error al cargar cartelera", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarPeliculas(ArrayList<Pelicula> lista) {
        listaPeliculas.removeAllViews();

        for (Pelicula p : lista) {
            // layout de peliculas
            View tarjetaView = getLayoutInflater().inflate(R.layout.item_pelicula, listaPeliculas, false);

            // referencias de las peliculas
            ImageView img = tarjetaView.findViewById(R.id.imgPelicula);
            TextView titulo = tarjetaView.findViewById(R.id.txtTituloPelicula);
            TextView desc = tarjetaView.findViewById(R.id.txtSubtituloPelicula);

            // completar la info de las peliculas
            Glide.with(this)
                    .load(p.getUrlImagen())
                    .into(img);

            titulo.setText(p.getTitulo());
            desc.setText(p.getGenero() + " • " + p.getAnio());

            tarjetaView.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetallePeliculaActivity.class);
                intent.putExtra("PELICULA_SELECCIONADA", p);
                startActivity(intent);
            });

            listaPeliculas.addView(tarjetaView);
        }
    }

    private void filtrarPeliculas(String texto) {
        ArrayList<Pelicula> filtradas = new ArrayList<>();
        if (peliculas != null) {
            for (Pelicula p : peliculas) {
                if (p.getTitulo() != null && p.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                    filtradas.add(p);
                }
            }
            mostrarPeliculas(filtradas);
        }
    }
}