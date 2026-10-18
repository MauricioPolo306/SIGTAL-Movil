package com.example.SIGTAL;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FacturacionActivity extends AppCompatActivity {

    private static final String PREF_INVENTARIO = "SIGTAL_INVENTARIO";
    private static final String CLAVE_PRODUCTOS = "productos";

    private static final String PREF_FACTURAS = "SIGTAL_FACTURACION";
    private static final String CLAVE_FACTURAS = "facturas";

    private static final String PREF_CONFIGURACION = "SIGTAL_CONFIGURACION";
    private static final String CLAVE_IMPUESTO = "impuesto";

    private TextView txtCliente;
    private TextView txtTotal;
    private LinearLayout contenedorDetalle;
    private LinearLayout contenedorHistorial;

    private JSONArray productos;
    private JSONArray facturas;

    private JSONObject clienteSeleccionado;

    private final JSONArray detalle = new JSONArray();

    private NumberFormat moneda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturacion);

        // Referencias de la pantalla.
        txtCliente = findViewById(R.id.txtClienteFactura);
        txtTotal = findViewById(R.id.txtTotalFactura);

        contenedorDetalle =
                findViewById(R.id.contenedorDetalleFactura);

        contenedorHistorial =
                findViewById(R.id.contenedorHistorialFacturas);

        Button btnSeleccionarCliente =
                findViewById(R.id.btnSeleccionarClienteFactura);

        Button btnAgregarProducto =
                findViewById(R.id.btnAgregarProductoFactura);

        Button btnAgregarManoObra =
                findViewById(R.id.btnAgregarManoObra);

        Button btnGuardar =
                findViewById(R.id.btnGuardarFactura);

        Button btnLimpiar =
                findViewById(R.id.btnLimpiarFactura);

        Button btnVolver =
                findViewById(R.id.btnVolverFacturacion);

        moneda = NumberFormat.getCurrencyInstance(
                new Locale("es", "CO")
        );

        cargarInventario();
        cargarFacturas();

        mostrarDetalle();
        mostrarHistorial();

        btnSeleccionarCliente.setOnClickListener(
                v -> seleccionarCliente()
        );

        btnAgregarProducto.setOnClickListener(
                v -> seleccionarProducto()
        );

        btnAgregarManoObra.setOnClickListener(
                v -> agregarManoObra()
        );

        btnGuardar.setOnClickListener(
                v -> guardarFactura()
        );

        btnLimpiar.setOnClickListener(
                v -> nuevaFactura()
        );

        btnVolver.setOnClickListener(v -> finish());
    }

    // =====================================================
    // INVENTARIO
    // =====================================================

    private void cargarInventario() {
        String datos = getSharedPreferences(
                PREF_INVENTARIO,
                MODE_PRIVATE
        ).getString(CLAVE_PRODUCTOS, "[]");

        try {
            productos = new JSONArray(datos);
        } catch (Exception e) {
            productos = new JSONArray();
        }
    }

    private void cargarFacturas() {
        String datos = getSharedPreferences(
                PREF_FACTURAS,
                MODE_PRIVATE
        ).getString(CLAVE_FACTURAS, "[]");

        try {
            facturas = new JSONArray(datos);
        } catch (Exception e) {
            facturas = new JSONArray();
        }
    }

    private boolean guardarInventario(JSONArray lista) {
        return getSharedPreferences(
                PREF_INVENTARIO,
                MODE_PRIVATE
        ).edit()
                .putString(CLAVE_PRODUCTOS, lista.toString())
                .commit();
    }

    private boolean guardarHistorial(JSONArray lista) {
        return getSharedPreferences(
                PREF_FACTURAS,
                MODE_PRIVATE
        ).edit()
                .putString(CLAVE_FACTURAS, lista.toString())
                .commit();
    }

    private int buscarProducto(String codigo) {
        return buscarProductoEn(productos, codigo);
    }

    private int buscarProductoEn(
            JSONArray lista,
            String codigo
    ) {
        for (int i = 0; i < lista.length(); i++) {
            JSONObject producto = lista.optJSONObject(i);

            if (producto != null
                    && codigo.equalsIgnoreCase(
                    producto.optString("codigo"))) {
                return i;
            }
        }

        return -1;
    }

    private String texto(
            JSONObject objeto,
            String... claves
    ) {
        if (objeto == null) {
            return "";
        }

        for (String clave : claves) {
            String valor = objeto.optString(clave, "");

            if (!valor.isEmpty() && !"null".equals(valor)) {
                return valor;
            }
        }

        return "";
    }

    // =====================================================
    // SELECCIONAR CLIENTE Y VEHICULO
    // =====================================================

    private void seleccionarCliente() {
        JSONArray registros;

        try {
            registros = RegistroLocal.consultar(this);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "No fue posible consultar los registros",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (registros == null || registros.length() == 0) {
            Toast.makeText(
                    this,
                    "Primero registra un cliente y su vehículo",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String[] opciones = new String[registros.length()];

        for (int i = 0; i < registros.length(); i++) {
            JSONObject registro = registros.optJSONObject(i);

            String nombre = texto(registro, "nombre", "cliente");
            String documento = texto(registro, "documento");
            String placa = texto(registro, "placa");
            String marca = texto(registro, "marca");
            String modelo = texto(registro, "modelo");

            opciones[i] = nombre
                    + (documento.isEmpty()
                    ? ""
                    : " - " + documento)
                    + "\nVehículo: "
                    + placa + " " + marca + " " + modelo;
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar cliente y vehículo")
                .setItems(opciones, (dialog, posicion) -> {
                    clienteSeleccionado =
                            registros.optJSONObject(posicion);

                    mostrarClienteSeleccionado();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarClienteSeleccionado() {
        if (clienteSeleccionado == null) {
            return;
        }

        txtCliente.setText(
                "Cliente: "
                        + texto(
                        clienteSeleccionado,
                        "nombre",
                        "cliente"
                )
                        + "\nDocumento: "
                        + texto(
                        clienteSeleccionado,
                        "documento"
                )
                        + "\nTeléfono: "
                        + texto(
                        clienteSeleccionado,
                        "telefono"
                )
                        + "\nVehículo: "
                        + texto(
                        clienteSeleccionado,
                        "placa"
                )
                        + " - "
                        + texto(
                        clienteSeleccionado,
                        "marca"
                )
                        + " "
                        + texto(
                        clienteSeleccionado,
                        "modelo"
                )
        );
    }

    // =====================================================
    // AGREGAR PRODUCTOS Y REPUESTOS
    // =====================================================

    private void seleccionarProducto() {
        cargarInventario();

        if (productos.length() == 0) {
            Toast.makeText(
                    this,
                    "No hay productos registrados en el inventario",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String[] opciones = new String[productos.length()];

        for (int i = 0; i < productos.length(); i++) {
            JSONObject producto = productos.optJSONObject(i);

            if (producto == null) {
                opciones[i] = "Producto no válido";
                continue;
            }

            opciones[i] = producto.optString("nombre")
                    + " | Código: "
                    + producto.optString("codigo")
                    + " | Stock: "
                    + producto.optInt("cantidad")
                    + " | Precio: "
                    + moneda.format(
                    producto.optDouble("precio")
            );
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar producto")
                .setItems(opciones, (dialog, posicion) -> {
                    JSONObject producto =
                            productos.optJSONObject(posicion);

                    if (producto == null) {
                        return;
                    }

                    if (producto.optInt("cantidad") <= 0) {
                        Toast.makeText(
                                this,
                                "El producto está agotado",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    pedirCantidad(producto);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void pedirCantidad(JSONObject producto) {
        EditText entrada = new EditText(this);

        entrada.setInputType(InputType.TYPE_CLASS_NUMBER);
        entrada.setHint("Cantidad");
        entrada.setText("1");

        new AlertDialog.Builder(this)
                .setTitle(producto.optString("nombre"))
                .setMessage(
                        "Existencias disponibles: "
                                + producto.optInt("cantidad")
                )
                .setView(entrada)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    try {
                        int cantidad = Integer.parseInt(
                                entrada.getText()
                                        .toString()
                                        .trim()
                        );

                        if (cantidad <= 0) {
                            throw new NumberFormatException();
                        }

                        String codigo =
                                producto.optString("codigo");

                        int posicion = buscarProducto(codigo);

                        if (posicion < 0) {
                            Toast.makeText(
                                    this,
                                    "Producto no encontrado",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        int solicitado =
                                cantidadTotalDelCodigo(codigo);

                        int stock = productos
                                .optJSONObject(posicion)
                                .optInt("cantidad");

                        if (solicitado + cantidad > stock) {
                            Toast.makeText(
                                    this,
                                    "Cantidad superior al stock disponible",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        double precio =
                                producto.optDouble("precio");

                        JSONObject linea = new JSONObject();

                        linea.put("tipo", "PRODUCTO");
                        linea.put("codigo", codigo);
                        linea.put(
                                "nombre",
                                producto.optString("nombre")
                        );
                        linea.put("cantidad", cantidad);
                        linea.put("precio", precio);
                        linea.put(
                                "subtotal",
                                cantidad * precio
                        );

                        detalle.put(linea);

                        mostrarDetalle();

                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Escribe una cantidad válida",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    // =====================================================
    // AGREGAR MANO DE OBRA
    // =====================================================

    private void agregarManoObra() {
        LinearLayout formulario = new LinearLayout(this);

        formulario.setOrientation(LinearLayout.VERTICAL);
        formulario.setPadding(32, 8, 32, 8);

        EditText nombre = new EditText(this);

        nombre.setHint("Descripción del servicio");
        nombre.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        );

        EditText precio = new EditText(this);

        precio.setHint("Valor de la mano de obra");
        precio.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        formulario.addView(nombre);
        formulario.addView(precio);

        new AlertDialog.Builder(this)
                .setTitle("Agregar mano de obra")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String descripcion = nombre.getText()
                            .toString()
                            .trim();

                    String valor = precio.getText()
                            .toString()
                            .trim()
                            .replace(",", ".");

                    try {
                        double importe = Double.parseDouble(valor);

                        if (descripcion.isEmpty()
                                || !Double.isFinite(importe)
                                || importe <= 0) {
                            throw new IllegalArgumentException();
                        }

                        JSONObject linea = new JSONObject();

                        linea.put("tipo", "MANO_OBRA");
                        linea.put("codigo", "");
                        linea.put("nombre", descripcion);
                        linea.put("cantidad", 1);
                        linea.put("precio", importe);
                        linea.put("subtotal", importe);

                        detalle.put(linea);

                        mostrarDetalle();

                    } catch (Exception e) {
                        Toast.makeText(
                                this,
                                "Escribe una descripción y un valor válido",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .show();
    }

    // =====================================================
    // DETALLE Y CALCULOS
    // =====================================================

    private int cantidadTotalDelCodigo(String codigo) {
        int total = 0;

        for (int i = 0; i < detalle.length(); i++) {
            JSONObject linea = detalle.optJSONObject(i);

            if (linea != null
                    && "PRODUCTO".equals(
                    linea.optString("tipo"))
                    && codigo.equalsIgnoreCase(
                    linea.optString("codigo"))) {

                total += linea.optInt("cantidad");
            }
        }

        return total;
    }

    // Suma los productos, repuestos y servicios antes de impuestos.
    private double calcularSubtotal() {
        double subtotal = 0;

        for (int i = 0; i < detalle.length(); i++) {
            JSONObject linea = detalle.optJSONObject(i);
            if (linea != null) {
                subtotal += linea.optDouble("subtotal", 0);
            }
        }
        return subtotal;
    }

    // Lee el porcentaje configurado. Si está vacío o no es válido, utiliza 0 %.
    private double obtenerPorcentajeImpuesto() {
        String valor = getSharedPreferences(
                PREF_CONFIGURACION, MODE_PRIVATE
        ).getString(CLAVE_IMPUESTO, "0");

        try {
            double porcentaje = Double.parseDouble(
                    valor.trim().replace(",", ".")
            );
            if (!Double.isFinite(porcentaje) || porcentaje < 0 || porcentaje > 100) {
                return 0;
            }
            return porcentaje;
        } catch (Exception e) {
            return 0;
        }
    }

    private double calcularValorImpuesto() {
        return calcularSubtotal() * obtenerPorcentajeImpuesto() / 100.0;
    }

    private double calcularTotal() {
        return calcularSubtotal() + calcularValorImpuesto();
    }

    private void mostrarDetalle() {
        contenedorDetalle.removeAllViews();

        for (int i = 0; i < detalle.length(); i++) {
            final int posicion = i;

            JSONObject linea = detalle.optJSONObject(i);

            if (linea == null) {
                continue;
            }

            TextView item = new TextView(this);

            item.setText(
                    linea.optString("nombre")
                            + "\nTipo: "
                            + linea.optString("tipo")
                            + " | Cantidad: "
                            + linea.optInt("cantidad")
                            + "\nPrecio unitario: "
                            + moneda.format(
                            linea.optDouble("precio")
                    )
                            + "\nSubtotal: "
                            + moneda.format(
                            linea.optDouble("subtotal")
                    )
            );

            item.setTextSize(15);
            item.setPadding(8, 12, 8, 4);

            contenedorDetalle.addView(item);

            Button quitar = new Button(this);
            quitar.setText("Quitar de la factura");

            quitar.setOnClickListener(v -> {
                detalle.remove(posicion);
                mostrarDetalle();
            });

            contenedorDetalle.addView(quitar);
        }

        txtTotal.setText(
                "Subtotal: " + moneda.format(calcularSubtotal())
                        + "\nImpuesto (" + NumberFormat.getNumberInstance(Locale.getDefault()).format(obtenerPorcentajeImpuesto())
                        + "%): " + moneda.format(calcularValorImpuesto())
                        + "\nTOTAL: " + moneda.format(calcularTotal())
        );

        if (detalle.length() == 0) {
            TextView vacio = new TextView(this);

            vacio.setText(
                    "No hay productos ni servicios agregados."
            );

            contenedorDetalle.addView(vacio);
        }
    }

    // =====================================================
    // GUARDAR FACTURA Y DESCONTAR INVENTARIO
    // =====================================================

    private void guardarFactura() {
        if (clienteSeleccionado == null) {
            Toast.makeText(
                    this,
                    "Selecciona un cliente y vehículo",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (detalle.length() == 0) {
            Toast.makeText(
                    this,
                    "Agrega al menos un producto o servicio",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        cargarInventario();

        // Conservar el inventario original para una posible restauración.
        final String inventarioOriginal = productos.toString();

        try {
            JSONArray inventarioActualizado =
                    new JSONArray(inventarioOriginal);

            // Validar y descontar cada código una sola vez.
            for (int i = 0; i < detalle.length(); i++) {
                JSONObject linea = detalle.getJSONObject(i);

                if (!"PRODUCTO".equals(
                        linea.optString("tipo"))) {
                    continue;
                }

                String codigo = linea.optString("codigo");

                boolean yaProcesado = false;

                for (int j = 0; j < i; j++) {
                    JSONObject anterior =
                            detalle.optJSONObject(j);

                    if (anterior != null
                            && "PRODUCTO".equals(
                            anterior.optString("tipo"))
                            && codigo.equalsIgnoreCase(
                            anterior.optString("codigo"))) {

                        yaProcesado = true;
                        break;
                    }
                }

                if (yaProcesado) {
                    continue;
                }

                int posicion = buscarProductoEn(
                        inventarioActualizado,
                        codigo
                );

                if (posicion < 0) {
                    Toast.makeText(
                            this,
                            "Producto no encontrado: " + codigo,
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                JSONObject producto =
                        inventarioActualizado.getJSONObject(posicion);

                int solicitado = cantidadTotalDelCodigo(codigo);
                int disponible = producto.optInt("cantidad", 0);

                if (solicitado <= 0
                        || solicitado > disponible) {

                    Toast.makeText(
                            this,
                            "Stock insuficiente para "
                                    + producto.optString("nombre")
                                    + ". Disponible: " + disponible,
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                producto.put(
                        "cantidad",
                        disponible - solicitado
                );
            }

            // Generar número y fecha de factura.
            String numero = "FAC-" + new SimpleDateFormat(
                    "yyyyMMdd-HHmmss-SSS",
                    Locale.getDefault()
            ).format(new Date());

            String fecha = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date());

            JSONObject factura = new JSONObject();

            factura.put("numero", numero);
            factura.put("fecha", fecha);

            factura.put(
                    "cliente",
                    texto(clienteSeleccionado, "nombre", "cliente")
            );

            factura.put(
                    "documento",
                    texto(clienteSeleccionado, "documento")
            );

            factura.put(
                    "telefono",
                    texto(clienteSeleccionado, "telefono")
            );

            factura.put(
                    "placa",
                    texto(clienteSeleccionado, "placa")
            );

            factura.put(
                    "marca",
                    texto(clienteSeleccionado, "marca")
            );

            factura.put(
                    "modelo",
                    texto(clienteSeleccionado, "modelo")
            );

            factura.put(
                    "detalle",
                    new JSONArray(detalle.toString())
            );

            double subtotalFactura = calcularSubtotal();
            double porcentajeImpuesto = obtenerPorcentajeImpuesto();
            double valorImpuesto = calcularValorImpuesto();
            double totalFactura = subtotalFactura + valorImpuesto;

            factura.put("subtotal", subtotalFactura);
            factura.put("porcentajeImpuesto", porcentajeImpuesto);
            factura.put("valorImpuesto", valorImpuesto);
            factura.put("total", totalFactura);

            // Preparar el nuevo historial sin modificar el actual.
            JSONArray historialNuevo =
                    new JSONArray(facturas.toString());

            historialNuevo.put(factura);

            // Guardar el inventario actualizado.
            boolean inventarioGuardado = guardarInventario(
                    inventarioActualizado
            );

            if (!inventarioGuardado) {
                Toast.makeText(
                        this,
                        "No se pudo actualizar el inventario",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Guardar la factura.
            boolean facturaGuardada =
                    guardarHistorial(historialNuevo);

            if (!facturaGuardada) {
                // Intentar restaurar el contenido original.
                boolean restaurado = getSharedPreferences(
                        PREF_INVENTARIO,
                        MODE_PRIVATE
                ).edit()
                        .putString(
                                CLAVE_PRODUCTOS,
                                inventarioOriginal
                        )
                        .commit();

                String mensaje = restaurado
                        ? "No se guardó la factura. El inventario fue restaurado."
                        : "Error crítico: verifica las existencias del inventario.";

                Toast.makeText(
                        this,
                        mensaje,
                        Toast.LENGTH_LONG
                ).show();

                cargarInventario();
                return;
            }

            // Actualizar los datos en memoria.
            productos = inventarioActualizado;
            facturas = historialNuevo;

            Toast.makeText(
                    this,
                    "Factura " + numero
                            + " guardada correctamente",
                    Toast.LENGTH_LONG
            ).show();

            nuevaFactura();
            mostrarHistorial();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Error al guardar la factura: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // NUEVA FACTURA
    // =====================================================

    private void nuevaFactura() {
        while (detalle.length() > 0) {
            detalle.remove(detalle.length() - 1);
        }

        clienteSeleccionado = null;

        txtCliente.setText(
                "No se ha seleccionado cliente"
        );

        mostrarDetalle();
    }

    // =====================================================
    // HISTORIAL
    // =====================================================

    private void mostrarHistorial() {
        contenedorHistorial.removeAllViews();

        for (int i = facturas.length() - 1; i >= 0; i--) {
            JSONObject factura = facturas.optJSONObject(i);

            if (factura == null) {
                continue;
            }

            TextView registro = new TextView(this);

            registro.setText(
                    factura.optString("numero")
                            + "\nFecha: "
                            + factura.optString("fecha")
                            + "\nCliente: "
                            + factura.optString("cliente")
                            + "\nDocumento: "
                            + factura.optString("documento")
                            + "\nVehículo: "
                            + factura.optString("placa")
                            + " - "
                            + factura.optString("marca")
                            + " "
                            + factura.optString("modelo")
                            + "\nSubtotal: "
                            + moneda.format(factura.optDouble("subtotal", factura.optDouble("total", 0)))
                            + "\nImpuesto ("
                            + NumberFormat.getNumberInstance(Locale.getDefault()).format(factura.optDouble("porcentajeImpuesto", 0))
                            + "%): "
                            + moneda.format(factura.optDouble("valorImpuesto", 0))
                            + "\nTOTAL: "
                            + moneda.format(factura.optDouble("total", 0))
            );

            registro.setTextSize(15);
            registro.setPadding(8, 12, 8, 12);

            contenedorHistorial.addView(registro);

            View separador = new View(this);
            separador.setBackgroundColor(0xFFDDDDDD);

            contenedorHistorial.addView(
                    separador,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    )
            );


            Button btnPdf = new Button(this);
            btnPdf.setText("Generar y compartir PDF");

            btnPdf.setOnClickListener(v -> {
                try {
                    File archivo = generarFacturaPDF(factura);

                    Uri uri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            archivo
                    );

                    Intent compartir = new Intent(Intent.ACTION_SEND);
                    compartir.setType("application/pdf");
                    compartir.putExtra(Intent.EXTRA_STREAM, uri);
                    compartir.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Factura " + factura.optString("numero")
                    );
                    compartir.putExtra(
                            Intent.EXTRA_TEXT,
                            "Factura del Taller Automotriz Londoño: "
                                    + factura.optString("numero")
                    );
                    compartir.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(
                            compartir,
                            "Compartir factura PDF"
                    ));

                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "No se pudo generar el PDF: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });

            contenedorHistorial.addView(btnPdf);



            Button btnWhatsApp = new Button(this);
            btnWhatsApp.setText("Enviar por WhatsApp");

            btnWhatsApp.setOnClickListener(v ->
                    enviarFacturaPorWhatsApp(factura)
            );

            contenedorHistorial.addView(btnWhatsApp);

            Button btnEliminar = new Button(this);
            btnEliminar.setText("Eliminar factura");
            btnEliminar.setAllCaps(false);
            btnEliminar.setOnClickListener(v ->
                    confirmarEliminarFactura(factura)
            );
            contenedorHistorial.addView(btnEliminar);

        }

        if (facturas.length() == 0) {
            TextView vacio = new TextView(this);

            vacio.setText(
                    "Todavía no hay facturas guardadas."
            );

            contenedorHistorial.addView(vacio);
        }
    }


    // =====================================================
    // ELIMINAR FACTURA Y RESTAURAR INVENTARIO
    // =====================================================

    private void confirmarEliminarFactura(JSONObject factura) {
        if (factura == null) {
            Toast.makeText(this,
                    "No se pudo identificar la factura.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String numero = factura.optString("numero", "").trim();

        if (numero.isEmpty()) {
            Toast.makeText(this,
                    "La factura no tiene un número válido y no se puede eliminar.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage(
                        "¿Deseas eliminar la factura " + numero + "?\n\n"
                                + "Las cantidades de los productos incluidos "
                                + "en esta factura se devolverán al inventario. "
                                + "Esta acción no se puede deshacer."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) ->
                        eliminarFacturaYRestaurarInventario(numero))
                .show();
    }

    private void eliminarFacturaYRestaurarInventario(String numero) {
        cargarInventario();
        cargarFacturas();

        final String inventarioOriginal = productos.toString();

        try {
            JSONArray inventarioActualizado =
                    new JSONArray(inventarioOriginal);

            JSONObject facturaAEliminar = null;
            JSONArray historialNuevo = new JSONArray();

            // Encontrar la factura y construir el historial sin ella.
            for (int i = 0; i < facturas.length(); i++) {
                JSONObject actual = facturas.optJSONObject(i);

                if (actual == null) {
                    continue;
                }

                if (numero.equals(actual.optString("numero", ""))) {
                    if (facturaAEliminar == null) {
                        facturaAEliminar = actual;
                    }
                    // Omitir la factura seleccionada del historial nuevo.
                } else {
                    historialNuevo.put(actual);
                }
            }

            if (facturaAEliminar == null) {
                Toast.makeText(this,
                        "La factura ya no existe en el historial.",
                        Toast.LENGTH_LONG).show();
                mostrarHistorial();
                return;
            }

            // Restaurar solo las líneas que corresponden a productos.
            // La mano de obra no afecta el inventario.
            JSONArray lineas = facturaAEliminar.optJSONArray("detalle");

            if (lineas != null) {
                for (int i = 0; i < lineas.length(); i++) {
                    JSONObject linea = lineas.optJSONObject(i);

                    if (linea == null
                            || !"PRODUCTO".equals(
                            linea.optString("tipo", ""))) {
                        continue;
                    }

                    String codigo = linea.optString("codigo", "").trim();
                    int cantidad = linea.optInt("cantidad", 0);

                    if (codigo.isEmpty() || cantidad <= 0) {
                        throw new IllegalStateException(
                                "La factura contiene un producto con datos inválidos. "
                                        + "No se realizaron cambios.");
                    }

                    int posicion = buscarProductoEn(inventarioActualizado, codigo);

                    if (posicion < 0) {
                        throw new IllegalStateException(
                                "No se encontró en el inventario el producto "
                                        + codigo + ". No se realizaron cambios.");
                    }

                    JSONObject producto =
                            inventarioActualizado.getJSONObject(posicion);

                    producto.put(
                            "cantidad",
                            producto.optInt("cantidad", 0) + cantidad
                    );
                }
            }

            // Guardar primero el inventario restaurado.
            boolean inventarioGuardado =
                    guardarInventario(inventarioActualizado);

            if (!inventarioGuardado) {
                Toast.makeText(this,
                        "No se pudo restaurar el inventario. "
                                + "La factura se conservó.",
                        Toast.LENGTH_LONG).show();
                cargarInventario();
                return;
            }

            // Guardar el historial sin la factura seleccionada.
            boolean historialGuardado = guardarHistorial(historialNuevo);

            if (!historialGuardado) {
                // Intentar restaurar el inventario a su estado anterior.
                boolean inventarioRestaurado = getSharedPreferences(
                        PREF_INVENTARIO, MODE_PRIVATE)
                        .edit()
                        .putString(CLAVE_PRODUCTOS, inventarioOriginal)
                        .commit();

                String mensaje = inventarioRestaurado
                        ? "No se pudo eliminar la factura. "
                        + "El inventario fue restaurado."
                        : "Error al guardar. Verifica manualmente el inventario "
                        + "antes de continuar.";

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

                cargarInventario();
                cargarFacturas();
                mostrarHistorial();
                return;
            }

            productos = inventarioActualizado;
            facturas = historialNuevo;

            Toast.makeText(this,
                    "Factura " + numero
                            + " eliminada y stock restaurado correctamente.",
                    Toast.LENGTH_LONG).show();

            mostrarHistorial();

        } catch (Exception e) {
            Toast.makeText(this,
                    "No se pudo eliminar la factura: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            cargarInventario();
            cargarFacturas();
            mostrarHistorial();
        }
    }

    private void enviarFacturaPorWhatsApp(JSONObject factura) {
        try {
            String telefono = factura.optString("telefono", "")
                    .replaceAll("[^0-9]", "");

            // Formato internacional de Colombia.
            if (telefono.length() == 10) {
                telefono = "57" + telefono;
            }

            if (telefono.length() < 10) {
                Toast.makeText(
                        this,
                        "El cliente no tiene un teléfono válido registrado.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            String numero = factura.optString("numero", "");
            String fecha = factura.optString("fecha", "");
            String cliente = factura.optString("cliente", "");
            String placa = factura.optString("placa", "");
            String marca = factura.optString("marca", "");
            String modelo = factura.optString("modelo", "");

            StringBuilder mensaje = new StringBuilder();

            mensaje.append("SISTEMA INTEGRAL DE GESTIÓN\n");
            mensaje.append("Taller Automotriz Londoño\n\n");
            mensaje.append("FACTURA DE SERVICIO\n");
            mensaje.append("Número: ").append(numero).append("\n");
            mensaje.append("Fecha: ").append(fecha).append("\n");
            mensaje.append("Cliente: ").append(cliente).append("\n");
            mensaje.append("Vehículo: ")
                    .append(placa).append(" - ")
                    .append(marca).append(" ")
                    .append(modelo).append("\n\n");

            mensaje.append("DETALLE DE LA FACTURA\n");

            JSONArray lineas = factura.optJSONArray("detalle");

            if (lineas != null) {
                for (int i = 0; i < lineas.length(); i++) {
                    JSONObject linea = lineas.optJSONObject(i);

                    if (linea == null) {
                        continue;
                    }

                    mensaje.append("- ")
                            .append(linea.optString("nombre"))
                            .append(" | Cantidad: ")
                            .append(linea.optInt("cantidad"))
                            .append(" | Subtotal: ")
                            .append(moneda.format(
                                    linea.optDouble("subtotal", 0)
                            ))
                            .append("\n");
                }
            }

            mensaje.append("\nSubtotal: ")
                    .append(moneda.format(factura.optDouble("subtotal", factura.optDouble("total", 0))))
                    .append("\nImpuesto (")
                    .append(NumberFormat.getNumberInstance(Locale.getDefault()).format(factura.optDouble("porcentajeImpuesto", 0)))
                    .append("%): ")
                    .append(moneda.format(factura.optDouble("valorImpuesto", 0)))
                    .append("\nTOTAL: ")
                    .append(moneda.format(factura.optDouble("total", 0)))
                    .append("\n\n");

            mensaje.append("Gracias por confiar en Taller Automotriz Londoño.");

            String url = "https://wa.me/"
                    + telefono
                    + "?text="
                    + Uri.encode(mensaje.toString());

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "No fue posible preparar la factura para WhatsApp.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private File generarFacturaPDF(JSONObject factura) throws IOException {
        PdfDocument pdf = new PdfDocument();

        final int ancho = 595;
        final int alto = 842;
        final int margen = 36;
        final int limiteInferior = 755;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PdfDocument.Page pagina = null;
        Canvas canvas = null;
        int numeroPagina = 0;
        int y = 0;

        try {
            JSONArray lineas = factura.optJSONArray("detalle");
            if (lineas == null) {
                lineas = new JSONArray();
            }

            // Iniciar primera página.
            numeroPagina++;
            pagina = pdf.startPage(
                    new PdfDocument.PageInfo.Builder(
                            ancho, alto, numeroPagina
                    ).create()
            );
            canvas = pagina.getCanvas();
            canvas.drawColor(Color.WHITE);

            dibujarEncabezadoFactura(canvas, p, factura);
            y = 300;
            dibujarCabeceraTabla(canvas, p, y);
            y += 30;

            for (int i = 0; i < lineas.length(); i++) {
                JSONObject linea = lineas.optJSONObject(i);
                if (linea == null) {
                    continue;
                }

                // Crear otra página antes de que la fila llegue al pie.
                if (y > limiteInferior) {
                    dibujarPiePagina(canvas, p, numeroPagina);
                    pdf.finishPage(pagina);
                    pagina = null;

                    numeroPagina++;
                    pagina = pdf.startPage(
                            new PdfDocument.PageInfo.Builder(
                                    ancho, alto, numeroPagina
                            ).create()
                    );
                    canvas = pagina.getCanvas();
                    canvas.drawColor(Color.WHITE);
                    dibujarEncabezadoFactura(canvas, p, factura);
                    y = 300;
                    dibujarCabeceraTabla(canvas, p, y);
                    y += 30;
                }

                String nombre = linea.optString("nombre", "Artículo");
                if (nombre.length() > 34) {
                    nombre = nombre.substring(0, 31) + "...";
                }

                p.setColor(Color.rgb(45, 55, 72));
                p.setTextSize(9);
                p.setTypeface(Typeface.DEFAULT);

                canvas.drawText(nombre, 40, y, p);
                canvas.drawText(
                        String.valueOf(linea.optInt("cantidad", 1)),
                        300, y, p
                );
                canvas.drawText(
                        moneda.format(linea.optDouble("precio", 0)),
                        350, y, p
                );
                canvas.drawText(
                        moneda.format(linea.optDouble("subtotal", 0)),
                        455, y, p
                );

                y += 25;
                p.setColor(Color.rgb(225, 230, 235));
                canvas.drawLine(36, y - 10, 559, y - 10, p);
            }

            // El total debe quedar completo en la última página.
            if (y > limiteInferior - 55) {
                dibujarPiePagina(canvas, p, numeroPagina);
                pdf.finishPage(pagina);
                pagina = null;

                numeroPagina++;
                pagina = pdf.startPage(
                        new PdfDocument.PageInfo.Builder(
                                ancho, alto, numeroPagina
                        ).create()
                );
                canvas = pagina.getCanvas();
                canvas.drawColor(Color.WHITE);
                dibujarEncabezadoFactura(canvas, p, factura);
                y = 300;
            }

            y += 15;
            p.setColor(Color.rgb(20, 45, 75));
            canvas.drawRoundRect(
                    315, y - 45, 559, y + 35,
                    8, 8, p
            );

            p.setColor(Color.WHITE);
            p.setTextSize(10);
            p.setTypeface(Typeface.DEFAULT);
            canvas.drawText(
                    "Subtotal: " + moneda.format(
                            factura.optDouble("subtotal", factura.optDouble("total", 0))
                    ),
                    328, y - 23, p
            );
            canvas.drawText(
                    "Impuesto (" + NumberFormat.getNumberInstance(Locale.getDefault()).format(
                            factura.optDouble("porcentajeImpuesto", 0)
                    ) + "%): " + moneda.format(factura.optDouble("valorImpuesto", 0)),
                    328, y - 5, p
            );
            p.setTextSize(13);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(
                    "TOTAL: " + moneda.format(factura.optDouble("total", 0)),
                    328, y + 20, p
            );

            y += 70;
            p.setColor(Color.rgb(80, 90, 100));
            p.setTextSize(10);
            p.setTypeface(Typeface.DEFAULT);
            canvas.drawText(
                    "Gracias por confiar en Taller Automotriz Londoño.",
                    margen, y, p
            );

            dibujarPiePagina(canvas, p, numeroPagina);
            pdf.finishPage(pagina);
            pagina = null;

            File carpeta = new File(getCacheDir(), "facturas");
            if (!carpeta.exists() && !carpeta.mkdirs()) {
                throw new IOException(
                        "No se pudo crear la carpeta de facturas."
                );
            }

            String numero = factura.optString("numero", "factura")
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
            File archivo = new File(carpeta, numero + ".pdf");

            try (FileOutputStream salida = new FileOutputStream(archivo)) {
                pdf.writeTo(salida);
            }

            return archivo;

        } finally {
            if (pagina != null) {
                pdf.finishPage(pagina);
            }
            pdf.close();
        }
    }

    private void dibujarEncabezadoFactura(
            Canvas canvas,
            Paint p,
            JSONObject factura
    ) {
        final int ancho = 595;
        final int margen = 36;

        // Leer los datos guardados en el módulo Configuración.
        android.content.SharedPreferences configuracion =
                getSharedPreferences("SIGTAL_CONFIGURACION", MODE_PRIVATE);

        String nombreTaller = configuracion.getString(
                "nombre", "Taller Automotriz Londoño");
        String razonSocial = configuracion.getString("razon_social", "");
        String nit = configuracion.getString("nit", "");
        String direccion = configuracion.getString("direccion", "");
        String ciudad = configuracion.getString("ciudad", "");
        String departamento = configuracion.getString("departamento", "");
        String telefonoTaller = configuracion.getString("telefono", "");
        String correo = configuracion.getString("correo", "");

        if (nombreTaller == null || nombreTaller.trim().isEmpty()) {
            nombreTaller = "Taller Automotriz Londoño";
        }

        // Evitar que los datos largos se salgan de la página.
        nombreTaller = limitarTexto(nombreTaller.trim(), 36);
        razonSocial = limitarTexto(razonSocial.trim(), 27);
        nit = limitarTexto(nit.trim(), 18);
        direccion = limitarTexto(direccion.trim(), 35);
        ciudad = limitarTexto(ciudad.trim(), 22);
        departamento = limitarTexto(departamento.trim(), 20);
        telefonoTaller = limitarTexto(telefonoTaller.trim(), 18);
        correo = limitarTexto(correo.trim(), 30);

        // Banda corporativa.
        p.setColor(Color.rgb(20, 45, 75));
        canvas.drawRect(0, 0, ancho, 140, p);

        // Identificador SIGTAL y datos del taller.
        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(25);
        canvas.drawText("SIGTAL", margen, 38, p);

        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(10);
        canvas.drawText(nombreTaller.toUpperCase(Locale.ROOT), margen, 59, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8);
        String lineaLegal = "";
        if (!razonSocial.isEmpty()) {
            lineaLegal = "Razón social: " + razonSocial;
        }
        if (!nit.isEmpty()) {
            if (!lineaLegal.isEmpty()) lineaLegal += " | ";
            lineaLegal += "NIT: " + nit;
        }
        if (!lineaLegal.isEmpty()) {
            canvas.drawText(limitarTexto(lineaLegal, 75), margen, 79, p);
        }

        String ubicacion = "";
        if (!direccion.isEmpty()) ubicacion = direccion;
        if (!ciudad.isEmpty()) {
            if (!ubicacion.isEmpty()) ubicacion += ", ";
            ubicacion += ciudad;
        }
        if (!departamento.isEmpty()) {
            if (!ubicacion.isEmpty()) ubicacion += ", ";
            ubicacion += departamento;
        }
        if (!ubicacion.isEmpty()) {
            canvas.drawText("Dirección: " + limitarTexto(ubicacion, 65),
                    margen, 98, p);
        }

        String contacto = "";
        if (!telefonoTaller.isEmpty()) contacto = "Tel: " + telefonoTaller;
        if (!correo.isEmpty()) {
            if (!contacto.isEmpty()) contacto += " | ";
            contacto += "Correo: " + correo;
        }
        if (!contacto.isEmpty()) {
            canvas.drawText(limitarTexto(contacto, 75), margen, 117, p);
        }

        // Información de la factura, alineada a la derecha.
        p.setTextSize(10);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("FACTURA DE SERVICIO", 365, 38, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8);
        canvas.drawText(limitarTexto(factura.optString("numero", ""), 28),
                365, 57, p);
        canvas.drawText(limitarTexto(factura.optString("fecha", ""), 28),
                365, 74, p);

        // Datos del cliente.
        int y = 165;
        p.setColor(Color.rgb(20, 45, 75));
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(12);
        canvas.drawText("DATOS DEL CLIENTE", margen, y, p);

        y += 23;
        p.setColor(Color.rgb(45, 55, 72));
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(10);
        canvas.drawText(
                "Cliente: " + limitarTexto(factura.optString("cliente", ""), 75),
                margen, y, p
        );

        y += 18;
        canvas.drawText(
                "Documento: " + factura.optString("documento", ""),
                margen, y, p
        );

        y += 18;
        canvas.drawText(
                "Teléfono: " + factura.optString("telefono", ""),
                margen, y, p
        );

        y += 18;
        canvas.drawText(
                "Vehículo: " + factura.optString("placa", "")
                        + " - " + factura.optString("marca", "")
                        + " " + factura.optString("modelo", ""),
                margen, y, p
        );

        p.setColor(Color.rgb(210, 218, 228));
        canvas.drawLine(margen, 265, ancho - margen, 265, p);
    }

    private String limitarTexto(String valor, int maximo) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\n", " ").replace("\r", " ").trim();
        if (limpio.length() <= maximo) {
            return limpio;
        }
        if (maximo <= 3) {
            return limpio.substring(0, maximo);
        }
        return limpio.substring(0, maximo - 3) + "...";
    }

    private void dibujarCabeceraTabla(Canvas canvas, Paint p, int y) {
        p.setColor(Color.rgb(20, 45, 75));
        canvas.drawRect(36, y - 17, 559, y + 8, p);

        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(9);

        canvas.drawText("DESCRIPCIÓN", 40, y, p);
        canvas.drawText("CANT.", 300, y, p);
        canvas.drawText("PRECIO", 350, y, p);
        canvas.drawText("SUBTOTAL", 455, y, p);

        p.setTypeface(Typeface.DEFAULT);
    }

    private void dibujarPiePagina(Canvas canvas, Paint p, int numeroPagina) {
        p.setColor(Color.rgb(100, 110, 120));
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8);

        canvas.drawText(
                "Documento generado por SIGTAL",
                36, 817, p
        );
        canvas.drawText(
                "Página " + numeroPagina,
                500, 817, p
        );
    }

}
