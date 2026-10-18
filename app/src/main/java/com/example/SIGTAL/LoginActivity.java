package com.example.SIGTAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String SESION_PREFS = "SIGTAL_SESION";
    public static final String CLAVE_USUARIO = "usuario";
    public static final String CLAVE_ROL = "rol";

    private EditText edtUsuario;
    private EditText edtContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sesion =
                getSharedPreferences(SESION_PREFS, MODE_PRIVATE);

        String rolGuardado = sesion.getString(CLAVE_ROL, null);

        if (rolGuardado != null) {
            abrirDashboard(
                    sesion.getString(CLAVE_USUARIO, ""),
                    rolGuardado
            );
            return;
        }

        setContentView(R.layout.activity_login);

        edtUsuario = findViewById(R.id.edtUsuarioLogin);
        edtContrasena = findViewById(R.id.edtContrasenaLogin);

        Button btnIngresar = findViewById(R.id.btnIniciarSesion);
        TextView txtOlvideContrasena =
                findViewById(R.id.txtOlvideContrasena);

        btnIngresar.setOnClickListener(v -> validarAcceso());

        if (txtOlvideContrasena != null) {
            txtOlvideContrasena.setOnClickListener(
                    v -> mostrarRecuperacion()
            );
        }
    }

    private void validarAcceso() {

        String usuario = edtUsuario.getText().toString().trim();
        String contrasena = edtContrasena.getText().toString();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(
                    this,
                    "Ingresa el usuario y la contraseña.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String rol = DemoAuth.autenticar(this, usuario, contrasena);

        if (rol == null) {
            edtContrasena.setText("");

            Toast.makeText(
                    this,
                    "Usuario o contraseña incorrectos.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        getSharedPreferences(SESION_PREFS, MODE_PRIVATE)
                .edit()
                .putString(CLAVE_USUARIO, usuario)
                .putString(CLAVE_ROL, rol)
                .apply();

        abrirDashboard(usuario, rol);
    }

    private void mostrarRecuperacion() {

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);

        int margen = (int) (20 * getResources()
                .getDisplayMetrics().density);

        contenedor.setPadding(margen, margen / 2, margen, 0);

        EditText edtUsuarioRecuperacion = new EditText(this);
        edtUsuarioRecuperacion.setHint("Usuario: admin o empleado");
        edtUsuarioRecuperacion.setSingleLine(true);
        edtUsuarioRecuperacion.setInputType(
                InputType.TYPE_CLASS_TEXT
        );

        EditText edtNuevaContrasena = new EditText(this);
        edtNuevaContrasena.setHint("Nueva contraseña (mínimo 8 caracteres)");
        edtNuevaContrasena.setSingleLine(true);
        edtNuevaContrasena.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        EditText edtConfirmarContrasena = new EditText(this);
        edtConfirmarContrasena.setHint("Confirmar nueva contraseña");
        edtConfirmarContrasena.setSingleLine(true);
        edtConfirmarContrasena.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        contenedor.addView(edtUsuarioRecuperacion);
        contenedor.addView(edtNuevaContrasena);
        contenedor.addView(edtConfirmarContrasena);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Recuperar contraseña")
                .setMessage(
                        "Recuperación local de demostración. " +
                                "Ingresa una cuenta existente y la nueva contraseña."
                )
                .setView(contenedor)
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Cierra la ventana sin modificar la contraseña.
                })
                .setPositiveButton("Cambiar contraseña", null)
                .create();

        dialogo.setOnShowListener(dialog -> {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String usuario = edtUsuarioRecuperacion
                                .getText().toString().trim();

                        String nueva = edtNuevaContrasena
                                .getText().toString();

                        String confirmacion = edtConfirmarContrasena
                                .getText().toString();

                        if (usuario.isEmpty()
                                || nueva.isEmpty()
                                || confirmacion.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "Completa todos los campos.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (!usuario.equalsIgnoreCase("admin")
                                && !usuario.equalsIgnoreCase("empleado")) {
                            Toast.makeText(
                                    this,
                                    "La cuenta no existe.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (nueva.length() < 8) {
                            Toast.makeText(
                                    this,
                                    "La contraseña debe tener al menos 8 caracteres.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        if (!nueva.equals(confirmacion)) {
                            Toast.makeText(
                                    this,
                                    "Las contraseñas no coinciden.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        boolean cambiado = DemoAuth.cambiarContrasena(
                                this, usuario, nueva
                        );

                        if (cambiado) {
                            if (edtUsuario.getText().toString()
                                    .trim().equalsIgnoreCase(usuario)) {
                                edtContrasena.setText("");
                            }

                            Toast.makeText(
                                    this,
                                    "Contraseña actualizada. Ya puedes iniciar sesión.",
                                    Toast.LENGTH_LONG
                            ).show();

                            dialogo.dismiss();
                        } else {
                            Toast.makeText(
                                    this,
                                    "No fue posible cambiar la contraseña.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialogo.show();
    }

    private void abrirDashboard(String usuario, String rol) {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.putExtra(CLAVE_USUARIO, usuario);
        intent.putExtra(CLAVE_ROL, rol);

        startActivity(intent);
        finish();
    }
}
