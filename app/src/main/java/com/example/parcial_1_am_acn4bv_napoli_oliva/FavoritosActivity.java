package com.example.parcial_1_am_acn4bv_napoli_oliva;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritosActivity extends AppCompatActivity {
    private LinearLayout listaFavoritos;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // datos del front
        listaFavoritos = findViewById(R.id.listaFavoritos);
        Button btnVolver = findViewById(R.id.btnVolverFavoritos);

        btnVolver.setOnClickListener(v -> finish());

        // Chequea y carga datos de favoritos
        checkAuthenticationAndLoadFavorites();
    }

    private void checkAuthenticationAndLoadFavorites(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            Toast.makeText(this, "Debe iniciar sesion.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        cargarFavoritos();
    }

    private void cargarFavoritos(){
        Toast.makeText(this, "Cargando tus favoritos...", Toast.LENGTH_SHORT).show();
        listaFavoritos.removeAllViews(); // limpia y carga

        // filtrar el userID
        db.collection("favoritos")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        List<Pelicula> favoritos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()){
                            // trae el objeto
                            Pelicula p = document.toObject(Pelicula.class);
                            favoritos.add(p);
                        }

                        // si favoritos no encuentra nada, no muestra que no hay nada, sino muestra los favoritos
                        if (favoritos.isEmpty()){
                            mostrarMensajeVacio();
                        } else {
                            mostrarPeliculas(favoritos);
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar favoritos: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarMensajeVacio() {
        TextView txtVacio = new TextView(this);
        txtVacio.setText("No tienes peliculas favoritas aun. Podes agregarlas desde la cartelera!");
        txtVacio.setTextColor(getResources().getColor(R.color.colorTextoSecundario));
        txtVacio.setTextSize(18);
        txtVacio.setPadding(32, 32, 32, 32);
        listaFavoritos.addView(txtVacio);
    }

    private void mostrarPeliculas (List<Pelicula> lista){
        listaFavoritos.removeAllViews();

        for (Pelicula p : lista){
            View tarjetaView = getLayoutInflater().inflate(R.layout.item_pelicula, listaFavoritos, false);

            // info de peliculas
            ImageView img = tarjetaView.findViewById(R.id.imgPelicula);
            TextView titulo = tarjetaView.findViewById(R.id.txtTituloPelicula);
            TextView desc = tarjetaView.findViewById(R.id.txtSubtituloPelicula);

            // Completar la info de las peliculas
            Glide.with(this)
                    .load(p.getUrlImagen())
                    .into(img);

            titulo.setText(p.getTitulo());
            desc.setText(p.getGenero() + " • " + p.getAnio());

            // datos de los favoritos
            tarjetaView.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetallePeliculaActivity.class);
                intent.putExtra("PELICULA_SELECCIONADA", p);
                startActivity(intent);
            });

            listaFavoritos.addView(tarjetaView);
        }
    }

    // recargar
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            cargarFavoritos();
        }
    }

}