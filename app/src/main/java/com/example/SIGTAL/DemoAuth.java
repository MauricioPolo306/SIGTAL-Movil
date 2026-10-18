package com.example.SIGTAL;

import android.content.Context;
import android.content.SharedPreferences;

public class DemoAuth {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String EMPLEADO = "EMPLEADO";

    private static final String PREFS = "SIGTAL_CUENTAS";
    private static final String CLAVE_ADMIN = "password_admin";
    private static final String CLAVE_EMPLEADO = "password_empleado";

    private static SharedPreferences obtenerPreferencias(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String obtenerContrasenaInicial(
            Context context, String usuario) {

        SharedPreferences prefs = obtenerPreferencias(context);

        if (usuario.equalsIgnoreCase("admin")) {
            return prefs.getString(CLAVE_ADMIN, "12365478");
        }

        if (usuario.equalsIgnoreCase("empleado")) {
            return prefs.getString(CLAVE_EMPLEADO, "Empleado123*");
        }

        return null;
    }

    public static String autenticar(
            Context context,
            String usuario,
            String contrasena) {

        if (context == null || usuario == null || contrasena == null) {
            return null;
        }

        String usuarioLimpio = usuario.trim();
        String claveGuardada = obtenerContrasenaInicial(context, usuarioLimpio);

        if (claveGuardada == null || !claveGuardada.equals(contrasena)) {
            return null;
        }

        if (usuarioLimpio.equalsIgnoreCase("admin")) {
            return ADMINISTRADOR;
        }

        if (usuarioLimpio.equalsIgnoreCase("empleado")) {
            return EMPLEADO;
        }

        return null;
    }

    public static boolean cambiarContrasena(
            Context context,
            String usuario,
            String nuevaContrasena) {

        if (context == null || usuario == null || nuevaContrasena == null) {
            return false;
        }

        String usuarioLimpio = usuario.trim();

        if (nuevaContrasena.length() < 8) {
            return false;
        }

        SharedPreferences.Editor editor =
                obtenerPreferencias(context).edit();

        if (usuarioLimpio.equalsIgnoreCase("admin")) {
            editor.putString(CLAVE_ADMIN, nuevaContrasena);
        } else if (usuarioLimpio.equalsIgnoreCase("empleado")) {
            editor.putString(CLAVE_EMPLEADO, nuevaContrasena);
        } else {
            return false;
        }

        editor.apply();
        return true;
    }
}
