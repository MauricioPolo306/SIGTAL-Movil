package com.example.SIGTAL;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegistroLocal {

    private static final String PREFS = "SIGTAL_REGISTROS";
    private static final String CLAVE = "clientes_vehiculos";

    public static boolean guardar(
            Context context,
            String nombre,
            String documento,
            String telefono,
            String correo,
            String direccion,
            String ciudad,
            String placa,
            String marca,
            String modelo,
            String anio,
            String color,
            String tipo,
            String kilometraje) {

        try {
            SharedPreferences prefs = context.getSharedPreferences(
                    PREFS, Context.MODE_PRIVATE);

            JSONArray registros = new JSONArray(
                    prefs.getString(CLAVE, "[]"));

            for (int i = 0; i < registros.length(); i++) {
                JSONObject actual = registros.getJSONObject(i);

                if (placa.equalsIgnoreCase(
                        actual.optString("placa", "").trim())) {
                    return false;
                }

                if (!documento.isEmpty()
                        && documento.equals(
                        actual.optString("documento", ""))) {
                    return false;
                }
            }

            JSONObject registro = new JSONObject();
            registro.put("nombre", nombre);
            registro.put("documento", documento);
            registro.put("telefono", telefono);
            registro.put("correo", correo);
            registro.put("direccion", direccion);
            registro.put("ciudad", ciudad);
            registro.put("placa", placa);
            registro.put("marca", marca);
            registro.put("modelo", modelo);
            registro.put("anio", anio);
            registro.put("color", color);
            registro.put("tipo", tipo);
            registro.put("kilometraje", kilometraje);

            registros.put(registro);

            return prefs.edit()
                    .putString(CLAVE, registros.toString())
                    .commit();

        } catch (Exception e) {
            return false;
        }
    }

    public static JSONArray consultar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);

        try {
            return new JSONArray(prefs.getString(CLAVE, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /**
     * Elimina un registro de cliente y vehículo por su placa.
     * Devuelve true si se eliminó y guardó correctamente.
     */
    public static boolean eliminar(Context context, String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return false;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(
                    PREFS, Context.MODE_PRIVATE);

            JSONArray registros = new JSONArray(
                    prefs.getString(CLAVE, "[]"));

            JSONArray actualizados = new JSONArray();
            boolean encontrado = false;

            for (int i = 0; i < registros.length(); i++) {
                JSONObject registro = registros.optJSONObject(i);

                if (registro == null) {
                    continue;
                }

                String placaRegistrada =
                        registro.optString("placa", "").trim();

                if (!encontrado
                        && placa.trim().equalsIgnoreCase(placaRegistrada)) {
                    encontrado = true;
                    continue;
                }

                actualizados.put(registro);
            }

            if (!encontrado) {
                return false;
            }

            return prefs.edit()
                    .putString(CLAVE, actualizados.toString())
                    .commit();

        } catch (Exception e) {
            return false;
        }
    }
}
