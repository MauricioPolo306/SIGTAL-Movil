package com.example.SIGTAL;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfiguracionActivity extends Activity {

    private EditText edtNombreTaller, edtRazonSocial, edtNit;
    private EditText edtActividadEconomica, edtDescripcion;
    private EditText edtDireccionTaller, edtCiudadTaller;
    private EditText edtDepartamentoTaller, edtCodigoPostal;
    private EditText edtTelefonoTaller, edtWhatsAppTaller;
    private EditText edtCorreoTaller, edtSitioWeb, edtResponsable;
    private EditText edtHorario, edtMoneda;
    private EditText edtPorcentajeImpuesto, edtUmbralStock;

    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        enlazarCampos();

        preferencias = getSharedPreferences(
                "SIGTAL_CONFIGURACION",
                MODE_PRIVATE
        );

        cargarConfiguracion();

        Button btnGuardar = findViewById(
                R.id.btnGuardarConfiguracion
        );

        Button btnCerrarSesion = findViewById(
                R.id.btnCerrarSesionConfiguracion
        );

        btnGuardar.setOnClickListener(v -> guardarConfiguracion());

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void enlazarCampos() {
        edtNombreTaller = findViewById(R.id.edtNombreTaller);
        edtRazonSocial = findViewById(R.id.edtRazonSocial);
        edtNit = findViewById(R.id.edtNit);
        edtActividadEconomica = findViewById(R.id.edtActividadEconomica);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        edtDireccionTaller = findViewById(R.id.edtDireccionTaller);
        edtCiudadTaller = findViewById(R.id.edtCiudadTaller);
        edtDepartamentoTaller = findViewById(R.id.edtDepartamentoTaller);
        edtCodigoPostal = findViewById(R.id.edtCodigoPostal);
        edtTelefonoTaller = findViewById(R.id.edtTelefonoTaller);
        edtWhatsAppTaller = findViewById(R.id.edtWhatsAppTaller);
        edtCorreoTaller = findViewById(R.id.edtCorreoTaller);
        edtSitioWeb = findViewById(R.id.edtSitioWeb);
        edtResponsable = findViewById(R.id.edtResponsable);
        edtHorario = findViewById(R.id.edtHorario);
        edtMoneda = findViewById(R.id.edtMoneda);
        edtPorcentajeImpuesto = findViewById(R.id.edtPorcentajeImpuesto);
        edtUmbralStock = findViewById(R.id.edtUmbralStock);
    }

    private void cargarConfiguracion() {
        edtNombreTaller.setText(leer("nombre", ""));
        edtRazonSocial.setText(leer("razon_social", ""));
        edtNit.setText(leer("nit", ""));
        edtActividadEconomica.setText(leer("actividad", ""));
        edtDescripcion.setText(leer("descripcion", ""));
        edtDireccionTaller.setText(leer("direccion", ""));
        edtCiudadTaller.setText(leer("ciudad", ""));
        edtDepartamentoTaller.setText(leer("departamento", ""));
        edtCodigoPostal.setText(leer("codigo_postal", ""));
        edtTelefonoTaller.setText(leer("telefono", ""));
        edtWhatsAppTaller.setText(leer("whatsapp", ""));
        edtCorreoTaller.setText(leer("correo", ""));
        edtSitioWeb.setText(leer("sitio_web", ""));
        edtResponsable.setText(leer("responsable", ""));
        edtHorario.setText(leer("horario", "Lunes a sábado"));
        edtMoneda.setText(leer("moneda", "COP"));
        edtPorcentajeImpuesto.setText(leer("impuesto", ""));
        edtUmbralStock.setText(leer("umbral_stock", "5"));
    }

    private String leer(String clave, String valorPredeterminado) {
        return preferencias.getString(clave, valorPredeterminado);
    }

    private void guardarConfiguracion() {
        String correo = edtCorreoTaller.getText().toString().trim();
        String impuesto = edtPorcentajeImpuesto.getText().toString().trim();
        String umbral = edtUmbralStock.getText().toString().trim();

        if (!correo.isEmpty() &&
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreoTaller.setError("Ingresa un correo válido");
            edtCorreoTaller.requestFocus();
            return;
        }

        if (!impuesto.isEmpty()) {
            try {
                double valorImpuesto = Double.parseDouble(impuesto);

                if (valorImpuesto < 0 || valorImpuesto > 100) {
                    edtPorcentajeImpuesto.setError(
                            "El porcentaje debe estar entre 0 y 100"
                    );
                    edtPorcentajeImpuesto.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                edtPorcentajeImpuesto.setError(
                        "Ingresa un porcentaje válido"
                );
                return;
            }
        }

        if (umbral.isEmpty()) {
            edtUmbralStock.setError("Ingresa el umbral de stock");
            edtUmbralStock.requestFocus();
            return;
        }

        try {
            int valorUmbral = Integer.parseInt(umbral);

            if (valorUmbral < 0) {
                edtUmbralStock.setError(
                        "El umbral no puede ser negativo"
                );
                edtUmbralStock.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtUmbralStock.setError(
                    "Ingresa un número entero válido"
            );
            edtUmbralStock.requestFocus();
            return;
        }

        boolean guardado = preferencias.edit()
                .putString("nombre", texto(edtNombreTaller))
                .putString("razon_social", texto(edtRazonSocial))
                .putString("nit", texto(edtNit))
                .putString("actividad", texto(edtActividadEconomica))
                .putString("descripcion", texto(edtDescripcion))
                .putString("direccion", texto(edtDireccionTaller))
                .putString("ciudad", texto(edtCiudadTaller))
                .putString("departamento", texto(edtDepartamentoTaller))
                .putString("codigo_postal", texto(edtCodigoPostal))
                .putString("telefono", texto(edtTelefonoTaller))
                .putString("whatsapp", texto(edtWhatsAppTaller))
                .putString("correo", correo)
                .putString("sitio_web", texto(edtSitioWeb))
                .putString("responsable", texto(edtResponsable))
                .putString("horario", texto(edtHorario))
                .putString("moneda", texto(edtMoneda))
                .putString("impuesto", impuesto)
                .putString("umbral_stock", umbral)
                .commit();

        if (guardado) {
            Toast.makeText(
                    this,
                    "Configuración guardada correctamente",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "No se pudo guardar la configuración",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String texto(EditText campo) {
        return campo.getText().toString().trim();
    }

    private void cerrarSesion() {
        getSharedPreferences(
                LoginActivity.SESION_PREFS,
                MODE_PRIVATE
        ).edit().clear().apply();

        Intent intent = new Intent(
                ConfiguracionActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}

