package com.example.SIGTAL;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class InventarioActivity extends AppCompatActivity {

    private static final String PREFS = "SIGTAL_INVENTARIO";
    private static final String CLAVE_PRODUCTOS = "productos";
    private static final int STOCK_MINIMO = 5;

    private EditText edtCodigo;
    private EditText edtNombre;
    private EditText edtCategoria;
    private EditText edtCantidad;
    private EditText edtCosto;
    private EditText edtPrecio;

    private TextView txtResumen;
    private LinearLayout contenedorProductos;

    private JSONArray productos;
    private NumberFormat formatoMoneda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        edtCodigo = findViewById(R.id.edtCodigoProducto);
        edtNombre = findViewById(R.id.edtNombreProducto);
        edtCategoria = findViewById(R.id.edtCategoriaProducto);
        edtCantidad = findViewById(R.id.edtCantidadProducto);
        edtCosto = findViewById(R.id.edtCostoProducto);
        edtPrecio = findViewById(R.id.edtPrecioProducto);

        txtResumen = findViewById(R.id.txtResumenInventario);
        contenedorProductos = findViewById(R.id.contenedorProductos);

        Button btnGuardar = findViewById(R.id.btnGuardarProducto);
        Button btnLimpiar = findViewById(R.id.btnLimpiarProducto);
        Button btnVolver = findViewById(R.id.btnVolverInventario);

        formatoMoneda = NumberFormat.getCurrencyInstance(
                new Locale("es", "CO")
        );

        cargarProductos();
        mostrarProductos();

        btnGuardar.setOnClickListener(v -> guardarProducto());

        btnLimpiar.setOnClickListener(v -> limpiarFormulario());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarProductos() {
        SharedPreferences preferencias =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        String datos = preferencias.getString(CLAVE_PRODUCTOS, "[]");

        try {
            productos = new JSONArray(datos);
        } catch (Exception e) {
            productos = new JSONArray();
            Toast.makeText(
                    this,
                    "No se pudieron leer los productos guardados",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void guardarProductos() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(CLAVE_PRODUCTOS, productos.toString())
                .apply();
    }

    private void guardarProducto() {
        String codigo = edtCodigo.getText().toString().trim();
        String nombre = edtNombre.getText().toString().trim();
        String categoria = edtCategoria.getText().toString().trim();
        String textoCantidad = edtCantidad.getText().toString().trim();
        String textoCosto = edtCosto.getText().toString().trim();
        String textoPrecio = edtPrecio.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty()
                || categoria.isEmpty() || textoCantidad.isEmpty()
                || textoCosto.isEmpty() || textoPrecio.isEmpty()) {
            Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int cantidad;
        double costo;
        double precio;

        try {
            cantidad = Integer.parseInt(textoCantidad);
            costo = Double.parseDouble(textoCosto.replace(",", "."));
            precio = Double.parseDouble(textoPrecio.replace(",", "."));

            if (cantidad < 0 || costo < 0 || precio < 0
                    || !Double.isFinite(costo)
                    || !Double.isFinite(precio)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Revisa la cantidad y los valores numéricos",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        int posicion = buscarPorCodigo(codigo);

        try {
            JSONObject producto;

            if (posicion >= 0) {
                producto = productos.getJSONObject(posicion);
            } else {
                producto = new JSONObject();
            }

            producto.put("codigo", codigo);
            producto.put("nombre", nombre);
            producto.put("categoria", categoria);
            producto.put("cantidad", cantidad);
            producto.put("costo", costo);
            producto.put("precio", precio);

            if (posicion >= 0) {
                productos.put(posicion, producto);
                Toast.makeText(
                        this,
                        "Producto actualizado",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                productos.put(producto);
                Toast.makeText(
                        this,
                        "Producto registrado",
                        Toast.LENGTH_SHORT
                ).show();
            }

            guardarProductos();
            limpiarFormulario();
            mostrarProductos();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "No fue posible guardar el producto",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private int buscarPorCodigo(String codigo) {
        for (int i = 0; i < productos.length(); i++) {
            JSONObject producto = productos.optJSONObject(i);

            if (producto != null
                    && codigo.equalsIgnoreCase(
                    producto.optString("codigo"))) {
                return i;
            }
        }

        return -1;
    }

    private void mostrarProductos() {
        contenedorProductos.removeAllViews();

        int unidadesTotales = 0;
        int productosBajos = 0;
        int productosAgotados = 0;

        for (int i = 0; i < productos.length(); i++) {
            JSONObject producto = productos.optJSONObject(i);

            if (producto == null) {
                continue;
            }

            int cantidad = producto.optInt("cantidad", 0);
            unidadesTotales += cantidad;

            if (cantidad == 0) {
                productosAgotados++;
            } else if (cantidad <= STOCK_MINIMO) {
                productosBajos++;
            }

            agregarTarjetaProducto(producto);
        }

        txtResumen.setText(
                "Resumen del inventario\n"
                        + "Productos registrados: " + productos.length()
                        + "\nUnidades disponibles: " + unidadesTotales
                        + "\nExistencias bajas (1-" + STOCK_MINIMO + "): "
                        + productosBajos
                        + "\nProductos agotados: " + productosAgotados
        );

        if (productos.length() == 0) {
            TextView vacio = new TextView(this);
            vacio.setText("Todavía no hay productos registrados.");
            vacio.setPadding(0, 12, 0, 12);
            contenedorProductos.addView(vacio);
        }
    }

    private void agregarTarjetaProducto(JSONObject producto) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        parametros.setMargins(0, 8, 0, 8);
        tarjeta.setLayoutParams(parametros);
        tarjeta.setBackgroundColor(Color.rgb(240, 244, 248));

        String codigo = producto.optString("codigo");
        String nombre = producto.optString("nombre");
        String categoria = producto.optString("categoria");
        int cantidad = producto.optInt("cantidad", 0);
        double costo = producto.optDouble("costo", 0);
        double precio = producto.optDouble("precio", 0);

        TextView informacion = new TextView(this);
        informacion.setText(
                nombre + "\n"
                        + "Código: " + codigo
                        + "\nCategoría: " + categoria
                        + "\nExistencias: " + cantidad
                        + "\nCosto unitario: " + formatoMoneda.format(costo)
                        + "\nPrecio de venta: " + formatoMoneda.format(precio)
        );
        informacion.setTextSize(15);
        informacion.setTextColor(Color.DKGRAY);

        if (cantidad == 0) {
            informacion.setText(
                    informacion.getText() + "\nESTADO: AGOTADO"
            );
            informacion.setTextColor(Color.RED);
        } else if (cantidad <= STOCK_MINIMO) {
            informacion.setText(
                    informacion.getText() + "\nALERTA: EXISTENCIAS BAJAS"
            );
            informacion.setTextColor(Color.rgb(180, 100, 0));
        }

        tarjeta.addView(informacion);

        LinearLayout filaBotones = new LinearLayout(this);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);

        Button btnEditar = new Button(this);
        btnEditar.setText("Editar");
        btnEditar.setOnClickListener(v -> cargarFormulario(producto));

        Button btnEliminar = new Button(this);
        btnEliminar.setText("Eliminar");
        btnEliminar.setOnClickListener(
                v -> confirmarEliminacion(codigo)
        );

        filaBotones.addView(
                btnEditar,
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );
        filaBotones.addView(
                btnEliminar,
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );

        tarjeta.addView(filaBotones);
        contenedorProductos.addView(tarjeta);
    }

    private void cargarFormulario(JSONObject producto) {
        edtCodigo.setText(producto.optString("codigo"));
        edtNombre.setText(producto.optString("nombre"));
        edtCategoria.setText(producto.optString("categoria"));
        edtCantidad.setText(String.valueOf(producto.optInt("cantidad")));
        edtCosto.setText(String.valueOf(producto.optDouble("costo")));
        edtPrecio.setText(String.valueOf(producto.optDouble("precio")));

        Toast.makeText(
                this,
                "Modifica los datos y pulsa Guardar producto",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void confirmarEliminacion(String codigo) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage(
                        "¿Deseas eliminar el producto con código "
                                + codigo + "?"
                )
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int posicion = buscarPorCodigo(codigo);

                    if (posicion >= 0) {
                        productos.remove(posicion);
                        guardarProductos();
                        mostrarProductos();
                        limpiarFormulario();

                        Toast.makeText(
                                this,
                                "Producto eliminado",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private void limpiarFormulario() {
        edtCodigo.setText("");
        edtNombre.setText("");
        edtCategoria.setText("");
        edtCantidad.setText("");
        edtCosto.setText("");
        edtPrecio.setText("");
        edtCodigo.requestFocus();
    }
}

