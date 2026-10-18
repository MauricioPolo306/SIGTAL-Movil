package com.example.SIGTAL;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class ClientesVehiculosActivity extends AppCompatActivity {

    private EditText edtNombre, edtDocumento, edtTelefono;
    private EditText edtCorreo, edtDireccion, edtCiudad;
    private EditText edtPlaca, edtMarca, edtModelo, edtAnio;
    private EditText edtColor, edtTipo, edtKilometraje;

    private LinearLayout contenedorRegistros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes_vehiculos);

        // Campos del cliente
        edtNombre = findViewById(R.id.edtNombreCliente);
        edtDocumento = findViewById(R.id.edtDocumento);
        edtTelefono = findViewById(R.id.edtTelefonoCliente);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtDireccion = findViewById(R.id.edtDireccion);
        edtCiudad = findViewById(R.id.edtCiudad);

        // Campos del vehículo
        edtPlaca = findViewById(R.id.edtPlaca);
        edtMarca = findViewById(R.id.edtMarca);
        edtModelo = findViewById(R.id.edtModelo);
        edtAnio = findViewById(R.id.edtAnio);
        edtColor = findViewById(R.id.edtColor);
        edtTipo = findViewById(R.id.edtTipoVehiculo);
        edtKilometraje = findViewById(R.id.edtKilometraje);

        // Botones principales
        Button btnGuardar =
                findViewById(R.id.btnGuardarClienteVehiculo);

        Button btnLimpiar =
                findViewById(R.id.btnLimpiarFormulario);

        Button btnVolver =
                findViewById(R.id.btnVolverDashboard);

        // Contenedor de tarjetas de clientes y vehículos
        contenedorRegistros =
                findViewById(R.id.txtListaRegistros);

        btnGuardar.setOnClickListener(v -> guardarRegistro());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        btnVolver.setOnClickListener(v -> finish());

        actualizarLista();
    }

    // =====================================================
    // GUARDAR CLIENTE Y VEHÍCULO
    // =====================================================

    private void guardarRegistro() {

        String nombre = valor(edtNombre);
        String documento = valor(edtDocumento);
        String telefono = valor(edtTelefono);
        String correo = valor(edtCorreo);
        String direccion = valor(edtDireccion);
        String ciudad = valor(edtCiudad);

        String placa = valor(edtPlaca).toUpperCase();
        String marca = valor(edtMarca);
        String modelo = valor(edtModelo);
        String anio = valor(edtAnio);
        String color = valor(edtColor);
        String tipo = valor(edtTipo);
        String kilometraje = valor(edtKilometraje);

        // Validar campos obligatorios
        if (nombre.isEmpty() || documento.isEmpty()
                || telefono.isEmpty() || placa.isEmpty()
                || marca.isEmpty()) {

            Toast.makeText(
                    this,
                    "Completa los campos obligatorios (*).",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validar correo electrónico
        if (!correo.isEmpty()
                && !android.util.Patterns.EMAIL_ADDRESS
                .matcher(correo).matches()) {

            edtCorreo.setError("Ingresa un correo válido.");
            edtCorreo.requestFocus();
            return;
        }

        // Validar año del vehículo
        if (!anio.isEmpty()) {
            try {
                int anioVehiculo = Integer.parseInt(anio);

                int anioActual = Calendar.getInstance()
                        .get(Calendar.YEAR);

                if (anioVehiculo < 1886
                        || anioVehiculo > anioActual + 1) {

                    edtAnio.setError(
                            "Verifica el año del vehículo."
                    );

                    edtAnio.requestFocus();
                    return;
                }

            } catch (NumberFormatException e) {
                edtAnio.setError("El año debe ser numérico.");
                edtAnio.requestFocus();
                return;
            }
        }

        // Validar kilometraje
        if (!kilometraje.isEmpty()) {
            try {
                long km = Long.parseLong(kilometraje);

                if (km < 0) {
                    edtKilometraje.setError(
                            "El kilometraje no puede ser negativo."
                    );

                    edtKilometraje.requestFocus();
                    return;
                }

            } catch (NumberFormatException e) {
                edtKilometraje.setError(
                        "Ingresa un kilometraje válido."
                );

                edtKilometraje.requestFocus();
                return;
            }
        }

        // Guardar usando la clase RegistroLocal
        boolean guardado = RegistroLocal.guardar(
                this,
                nombre,
                documento,
                telefono,
                correo,
                direccion,
                ciudad,
                placa,
                marca,
                modelo,
                anio,
                color,
                tipo,
                kilometraje
        );

        if (guardado) {

            Toast.makeText(
                    this,
                    "Cliente y vehículo guardados correctamente.",
                    Toast.LENGTH_LONG
            ).show();

            limpiarFormulario();
            actualizarLista();

        } else {

            Toast.makeText(
                    this,
                    "No se guardó. La placa o el documento "
                            + "pueden estar registrados.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // UTILIDADES DEL FORMULARIO
    // =====================================================

    private String valor(EditText campo) {
        return campo.getText().toString().trim();
    }

    private void limpiarFormulario() {

        edtNombre.setText("");
        edtDocumento.setText("");
        edtTelefono.setText("");
        edtCorreo.setText("");
        edtDireccion.setText("");
        edtCiudad.setText("");

        edtPlaca.setText("");
        edtMarca.setText("");
        edtModelo.setText("");
        edtAnio.setText("");
        edtColor.setText("");
        edtTipo.setText("");
        edtKilometraje.setText("");

        edtNombre.requestFocus();
    }

    // =====================================================
    // LISTAR CLIENTES Y VEHÍCULOS
    // =====================================================

    private void actualizarLista() {

        JSONArray registros = RegistroLocal.consultar(this);

        contenedorRegistros.removeAllViews();

        if (registros.length() == 0) {

            TextView vacio = new TextView(this);

            vacio.setText("Todavía no hay registros.");
            vacio.setTextColor(0xFF334155);
            vacio.setTextSize(14);
            vacio.setPadding(16, 16, 16, 16);

            contenedorRegistros.addView(vacio);
            return;
        }

        // Mostrar primero los registros más recientes
        for (int i = registros.length() - 1; i >= 0; i--) {

            JSONObject registro = registros.optJSONObject(i);

            if (registro == null) {
                continue;
            }

            final String placa =
                    registro.optString("placa", "").trim();

            final String nombre =
                    registro.optString("nombre", "");

            // Tarjeta del registro
            LinearLayout tarjeta = new LinearLayout(this);

            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(16, 16, 16, 16);
            tarjeta.setBackgroundColor(0xFFF3F6FA);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametros.setMargins(0, 0, 0, 20);

            tarjeta.setLayoutParams(parametros);

            // Información del cliente y vehículo
            TextView informacion = new TextView(this);

            informacion.setTextSize(14);
            informacion.setTextColor(0xFF334155);

            String texto =
                    "CLIENTE\n"
                            + "Nombre: " + nombre
                            + "\nDocumento: "
                            + registro.optString("documento", "")
                            + "\nTeléfono: "
                            + registro.optString("telefono", "")
                            + "\nCorreo: "
                            + registro.optString("correo", "")
                            + "\nDirección: "
                            + registro.optString("direccion", "")
                            + "\nCiudad: "
                            + registro.optString("ciudad", "")
                            + "\n\nVEHÍCULO"
                            + "\nPlaca: " + placa
                            + "\nMarca: "
                            + registro.optString("marca", "")
                            + "\nModelo: "
                            + registro.optString("modelo", "")
                            + "\nAño: "
                            + registro.optString("anio", "")
                            + "\nColor: "
                            + registro.optString("color", "")
                            + "\nTipo: "
                            + registro.optString("tipo", "")
                            + "\nKilometraje: "
                            + registro.optString("kilometraje", "")
                            + " km";

            informacion.setText(texto);
            tarjeta.addView(informacion);

            // Botón para eliminar el registro
            Button btnEliminar = new Button(this);

            btnEliminar.setText(
                    "Eliminar cliente y vehículo"
            );

            btnEliminar.setAllCaps(false);

            tarjeta.addView(btnEliminar);

            btnEliminar.setOnClickListener(v ->
                    confirmarEliminacion(placa, nombre)
            );

            contenedorRegistros.addView(tarjeta);
        }
    }

    // =====================================================
    // ELIMINAR CLIENTE Y VEHÍCULO
    // =====================================================

    private void confirmarEliminacion(
            String placa,
            String nombre
    ) {

        if (placa.isEmpty()) {

            Toast.makeText(
                    this,
                    "El registro no tiene una placa válida.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage(
                        "¿Deseas eliminar el registro de "
                                + nombre
                                + " y el vehículo de placa "
                                + placa
                                + "?\n\n"
                                + "Esta acción no se puede deshacer. "
                                + "Las facturas históricas se conservarán."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    boolean eliminado =
                            RegistroLocal.eliminar(this, placa);

                    if (eliminado) {

                        Toast.makeText(
                                this,
                                "Registro eliminado correctamente.",
                                Toast.LENGTH_LONG
                        ).show();

                        actualizarLista();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo eliminar el registro.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .show();
    }

    // =====================================================
    // ACTUALIZAR AL VOLVER A LA PANTALLA
    // =====================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (contenedorRegistros != null) {
            actualizarLista();
        }
    }
}
