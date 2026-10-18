package com.example.SIGTAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    private TextView txtTotalClientes;
    private TextView txtTotalVehiculos;
    private TextView txtTotalProductos;
    private TextView txtTotalFacturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener el rol del usuario
        String rol = getIntent().getStringExtra(
                LoginActivity.CLAVE_ROL
        );

        if (rol == null) {
            rol = getSharedPreferences(
                    LoginActivity.SESION_PREFS,
                    MODE_PRIVATE
            ).getString(LoginActivity.CLAVE_ROL, null);
        }

        // Validar la sesión
        if (rol == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        // Indicadores del dashboard
        txtTotalClientes = findViewById(R.id.txtTotalClientes);
        txtTotalVehiculos = findViewById(R.id.txtTotalVehiculos);
        txtTotalProductos = findViewById(R.id.txtTotalProductos);
        txtTotalFacturas = findViewById(R.id.txtTotalFacturas);

        // Botones
        Button btnClientesVehiculos =
                findViewById(R.id.btnClientesVehiculos);

        Button btnInventario =
                findViewById(R.id.btnInventario);

        Button btnFacturacion =
                findViewById(R.id.btnFacturacion);

        Button btnReportes =
                findViewById(R.id.btnReportes);

        Button btnConfiguracion =
                findViewById(R.id.btnConfiguracion);

        Button btnCerrarSesion =
                findViewById(R.id.btnCerrarSesion);

        // Navegación
        btnClientesVehiculos.setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        ClientesVehiculosActivity.class
                ))
        );

        btnInventario.setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        InventarioActivity.class
                ))
        );

        btnFacturacion.setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        FacturacionActivity.class
                ))
        );

        btnReportes.setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        ReportesActivity.class
                ))
        );

        btnConfiguracion.setOnClickListener(v ->
                startActivity(new Intent(
                        this,
                        ConfiguracionActivity.class
                ))
        );

        // Permisos por rol
        if (rol.equalsIgnoreCase(DemoAuth.ADMINISTRADOR)) {

            btnFacturacion.setVisibility(View.VISIBLE);
            btnReportes.setVisibility(View.VISIBLE);
            btnConfiguracion.setVisibility(View.VISIBLE);

        } else {

            btnFacturacion.setVisibility(View.VISIBLE);
            btnReportes.setVisibility(View.GONE);
            btnConfiguracion.setVisibility(View.GONE);
        }

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {

            getSharedPreferences(
                    LoginActivity.SESION_PREFS,
                    MODE_PRIVATE
            ).edit().clear().apply();

            Intent intent = new Intent(
                    this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });

        actualizarDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualizar al regresar de otros módulos
        actualizarDashboard();
    }

    private void actualizarDashboard() {

        // Clientes y vehículos
        JSONArray registros = RegistroLocal.consultar(this);

        int totalRegistros = registros.length();

        // Cada registro contiene un cliente y un vehículo.
        // Este cálculo cuenta registros, no clientes únicos.
        txtTotalClientes.setText(
                String.valueOf(totalRegistros)
        );

        txtTotalVehiculos.setText(
                String.valueOf(totalRegistros)
        );

        // Inventario
        SharedPreferences inventario = getSharedPreferences(
                "SIGTAL_INVENTARIO",
                MODE_PRIVATE
        );

        String productosJson = inventario.getString(
                "productos",
                "[]"
        );

        int totalProductos = contarRegistros(productosJson);

        txtTotalProductos.setText(
                String.valueOf(totalProductos)
        );

        // Facturación
        SharedPreferences facturacion = getSharedPreferences(
                "SIGTAL_FACTURACION",
                MODE_PRIVATE
        );

        String facturasJson = facturacion.getString(
                "facturas",
                "[]"
        );

        int totalFacturas = contarRegistros(facturasJson);

        txtTotalFacturas.setText(
                String.valueOf(totalFacturas)
        );
    }

    private int contarRegistros(String json) {
        try {
            return new JSONArray(json).length();
        } catch (Exception e) {
            android.util.Log.e(
                    "SIGTAL",
                    "Error al contar registros",
                    e
            );
            return 0;
        }
    }
}
