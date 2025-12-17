package com.example.parcial_1_am_acn4bv_napoli_oliva;

import android.content.Intent; // Importante
import android.net.Uri;       // Importante
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;     // Importante
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

public class DetallePeliculaActivity extends AppCompatActivity {
    // Precio por entrada ($12000)
    private static final double PRECIO_ENTRADA = 12000;
    private EditText inputCantidadEntradas;
    private TextView txtCostoTotal;
    private Button btnConfirmarReserva;
    private Button btnTrailer;
    private Pelicula peliculaActual;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String favoriteDocId = null;
    private android.widget.RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pelicula);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null){
            Toast.makeText(this, "Debe estar logueado para ver detalles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = user.getUid();

        TextView txtTitulo = findViewById(R.id.detalleTitulo);
        TextView txtDatos = findViewById(R.id.detalleDatos);
        TextView txtDescripcion = findViewById(R.id.detalleDescripcion);
        ImageView imgPoster = findViewById(R.id.detalleImagen);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnFavorito = findViewById(R.id.btnFavorito);

        //estrellas review
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(DetallePeliculaActivity.this,
                        "Tu calificacion fue de: " + rating + " estrellas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // btn trailer
        btnTrailer = findViewById(R.id.btnTrailer);

        peliculaActual = (Pelicula) getIntent().getSerializableExtra("PELICULA_SELECCIONADA");

        if (peliculaActual != null) {

            txtTitulo.setText(peliculaActual.getTitulo());
            txtDatos.setText(peliculaActual.getGenero() + " (" + peliculaActual.getAnio() + ")");
            txtDescripcion.setText(peliculaActual.getUrlDescripcion());

            Glide.with(this).load(peliculaActual.getUrlImagen()).into(imgPoster);

            btnVolver.setOnClickListener(v -> finish());

            checkIfFavoriteAndSetButton();
            btnFavorito.setOnClickListener(v -> toggleFavoriteStatus());

            // nueva verificacion de urlTrailer en FB
            if (peliculaActual.getUrlTrailer() != null && !peliculaActual.getUrlTrailer().isEmpty()) {
                btnTrailer.setVisibility(View.VISIBLE);
                btnTrailer.setOnClickListener(v -> {
                    try {
                        Uri uri = Uri.parse(peliculaActual.getUrlTrailer());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(DetallePeliculaActivity.this, "No se pudo abrir el enlace.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnTrailer.setVisibility(View.GONE);
            }

            // Reservar entradas
            inputCantidadEntradas = findViewById(R.id.inputCantidadEntradas);
            txtCostoTotal = findViewById(R.id.txtCostoTotal);
            btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva);

            calcularCostoTotal(1);

            inputCantidadEntradas.addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) { }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String strCantidad = s.toString();
                    int cantidad = strCantidad.isEmpty() ? 0 : Integer.parseInt(strCantidad);
                    calcularCostoTotal(cantidad);
                }
            });
            btnConfirmarReserva.setOnClickListener(v -> confirmarReserva());

        } else {
            Toast.makeText(this, "Error: No se pudo cargar la información de la película.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void guardarEnFirebase(FirebaseFirestore db, Pelicula p) {
        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setText("Guardando...");

        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("titulo", p.getTitulo());
        data.put("genero", p.getGenero());
        data.put("anio", p.getAnio());
        data.put("urlImagen", p.getUrlImagen());
        data.put("urlDescripcion", p.getUrlDescripcion());
        data.put("urlTrailer", p.getUrlTrailer());
        data.put("userId", currentUserId);

        db.collection("favoritos")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Agregado a Favoritos!", Toast.LENGTH_SHORT).show();
                    checkIfFavoriteAndSetButton();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnFavorito.setEnabled(true);
                });
    }
    private void calcularCostoTotal (int cantidad){
        if (cantidad < 0 || cantidad > 99) cantidad = 0;
        double costoTotal = cantidad * PRECIO_ENTRADA;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        txtCostoTotal.setText("Costo Total: " + currencyFormat.format(costoTotal));
    }

    private void confirmarReserva (){
        String strCantidad = inputCantidadEntradas.getText().toString();
        int cantidad = strCantidad.isEmpty() ? 0 : Integer.parseInt(strCantidad);
        if (cantidad <= 0) {
            Toast.makeText(this, "Selecciona al menos una entrada.", Toast.LENGTH_SHORT).show();
            return;
        }
        String costoTotal = txtCostoTotal.getText().toString().replace("Costo Total: ", "");
        String mensaje = "¡Reserva confirmada! Película: '" + peliculaActual.getTitulo() + "' (" + cantidad + " entradas). Total a pagar: " + costoTotal;
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void checkIfFavoriteAndSetButton (){
        if (currentUserId == null || peliculaActual == null) return;
        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setText("Verificando...");
        btnFavorito.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("favoritos")
                .whereEqualTo("id", peliculaActual.getId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    btnFavorito.setEnabled(true);
                    if (task.isSuccessful() && !task.getResult().isEmpty()){
                        favoriteDocId = task.getResult().getDocuments().get(0).getId();
                        btnFavorito.setText("QUITAR de Favoritos");
                        btnFavorito.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorInteractivo)));
                    } else {
                        favoriteDocId = null;
                        btnFavorito.setText("AGREGAR a Favoritos");
                        btnFavorito.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAcento)));
                    }
                });
    }

    private void toggleFavoriteStatus (){
        if (currentUserId == null || peliculaActual == null) return;
        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (favoriteDocId != null){
            db.collection("favoritos").document(favoriteDocId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Eliminado de favoritos.", Toast.LENGTH_SHORT).show();
                        checkIfFavoriteAndSetButton();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnFavorito.setEnabled(true);
                    });
        } else {
            guardarEnFirebase(db, peliculaActual);
        }
    }
}