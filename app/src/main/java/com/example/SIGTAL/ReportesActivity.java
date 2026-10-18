package com.example.SIGTAL;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class ReportesActivity extends Activity {

    private TextView txtTotalFacturas;
    private TextView txtIngresos;
    private TextView txtTotalProductos;
    private TextView txtStockBajo;
    private TextView txtHistorialFacturas;

    private SharedPreferences preferenciasFacturacion;
    private SharedPreferences preferenciasInventario;

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        txtTotalFacturas = findViewById(R.id.txtTotalFacturas);
        txtIngresos = findViewById(R.id.txtIngresos);
        txtTotalProductos = findViewById(R.id.txtTotalProductos);
        txtStockBajo = findViewById(R.id.txtStockBajo);
        txtHistorialFacturas = findViewById(R.id.txtHistorialFacturas);

        preferenciasFacturacion =
                getSharedPreferences("SIGTAL_FACTURACION", MODE_PRIVATE);

        preferenciasInventario =
                getSharedPreferences("SIGTAL_INVENTARIO", MODE_PRIVATE);

        Button btnActualizar =
                findViewById(R.id.btnActualizarReportes);

        btnActualizar.setOnClickListener(v -> cargarReportes());

        cargarReportes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (txtTotalFacturas != null) {
            cargarReportes();
        }
    }

    private void cargarReportes() {
        cargarReporteFacturacion();
        cargarReporteInventario();
    }

    // =====================================================
    // REPORTE DE FACTURACIÓN
    // =====================================================

    private void cargarReporteFacturacion() {

        String datos = preferenciasFacturacion.getString(
                "facturas", "[]");

        try {
            JSONArray facturas = new JSONArray(datos);

            int cantidadFacturas = facturas.length();
            double totalFacturado = 0;

            StringBuilder historial = new StringBuilder();

            for (int i = facturas.length() - 1; i >= 0; i--) {

                JSONObject factura = facturas.optJSONObject(i);

                if (factura == null) {
                    continue;
                }

                double total = leerImporte(factura, "total");
                totalFacturado += total;

                String numero = factura.optString(
                        "numero", "Sin número");

                String fecha = factura.optString(
                        "fecha", "Sin fecha");

                String cliente = factura.optString(
                        "cliente", "Sin cliente");

                historial.append("Factura: ")
                        .append(numero)
                        .append("\nFecha: ")
                        .append(fecha)
                        .append("\nCliente: ")
                        .append(cliente)
                        .append("\nTotal: ")
                        .append(formatoMoneda.format(total))
                        .append("\n\n");
            }

            txtTotalFacturas.setText(
                    String.valueOf(cantidadFacturas));

            txtIngresos.setText(
                    formatoMoneda.format(totalFacturado));

            if (cantidadFacturas == 0) {
                txtHistorialFacturas.setText(
                        "Todavía no hay facturas registradas.");
            } else {
                txtHistorialFacturas.setText(
                        historial.toString().trim());
            }

        } catch (Exception e) {

            txtTotalFacturas.setText("—");
            txtIngresos.setText("—");

            txtHistorialFacturas.setText(
                    "No se pudieron leer las facturas. " +
                            "Verifica el formato de los datos guardados.");

            Toast.makeText(
                    this,
                    "Error al cargar facturación",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // REPORTE DE INVENTARIO
    // =====================================================

    private void cargarReporteInventario() {

        String datos = preferenciasInventario.getString(
                "productos", "[]");

        try {
            JSONArray productos = new JSONArray(datos);

            int totalProductos = productos.length();
            int productosStockBajo = 0;

            // Leer la configuración una sola vez.
            SharedPreferences preferenciasConfiguracion =
                    getSharedPreferences(
                            "SIGTAL_CONFIGURACION",
                            MODE_PRIVATE
                    );

            int umbralStock = 5;

            try {
                umbralStock = Integer.parseInt(
                        preferenciasConfiguracion.getString(
                                "umbral_stock", "5"
                        )
                );

                if (umbralStock < 0) {
                    umbralStock = 5;
                }

            } catch (NumberFormatException e) {
                umbralStock = 5;
            }

            // Contar los productos con stock igual o inferior
            // al umbral definido en Configuración.
            for (int i = 0; i < productos.length(); i++) {

                JSONObject producto = productos.optJSONObject(i);

                if (producto == null) {
                    continue;
                }

                int cantidad = producto.optInt("cantidad", 0);

                if (cantidad <= umbralStock) {
                    productosStockBajo++;
                }
            }

            txtTotalProductos.setText(
                    String.valueOf(totalProductos));

            txtStockBajo.setText(
                    "Productos con stock bajo: " +
                            productosStockBajo);

        } catch (Exception e) {

            txtTotalProductos.setText("—");

            txtStockBajo.setText(
                    "No se pudo leer el inventario.");

            Toast.makeText(
                    this,
                    "Error al cargar inventario",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // LECTURA SEGURA DE IMPORTES
    // =====================================================

    private double leerImporte(JSONObject objeto, String clave) {

        Object valor = objeto.opt(clave);

        if (valor == null || valor == JSONObject.NULL) {
            return 0;
        }

        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }

        try {
            return Double.parseDouble(valor.toString());

        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
