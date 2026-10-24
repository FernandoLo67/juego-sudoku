package com.fernando.sudoku;

import java.util.function.LongSupplier;

/**
 * Estadisticas de una partida de Sudoku: movimientos, errores, pistas,
 * tiempo y puntuacion final.
 *
 * <p>La puntuacion parte de 1000 y penaliza errores (-50), pistas (-100)
 * y tiempo (1 punto por cada 10 segundos pasados del primer minuto).
 * La dificultad aplica un multiplicador: FACIL x1, MEDIO x1.5, DIFICIL x2.
 *
 * <p>El reloj es inyectable para pruebas deterministas.
 */
public class Estadisticas {

    private static final int PUNTUACION_BASE = 1000;
    private static final int PENALIZACION_ERROR = 50;
    private static final int PENALIZACION_PISTA = 100;
    private static final long TIEMPO_GRATIS_MS = 60_000;
    private static final long INTERVALO_PENALIZACION_MS = 10_000;

    private final Dificultad dificultad;
    private final LongSupplier reloj;
    private final long inicio;

    private int movimientos;
    private int errores;
    private int pistasUsadas;
    private long finMs;

    public Estadisticas(Dificultad dificultad, LongSupplier reloj) {
        this.dificultad = dificultad;
        this.reloj = reloj;
        this.inicio = reloj.getAsLong();
    }

    public void registrarMovimiento() {
        movimientos++;
    }

    public void registrarError() {
        errores++;
    }

    public void registrarPista() {
        pistasUsadas++;
    }

    public void terminar() {
        finMs = reloj.getAsLong();
    }

    public int getMovimientos() {
        return movimientos;
    }

    public int getErrores() {
        return errores;
    }

    public int getPistasUsadas() {
        return pistasUsadas;
    }

    public long tiempoTranscurridoMs() {
        long fin = finMs > 0 ? finMs : reloj.getAsLong();
        return fin - inicio;
    }

    public String tiempoFormateado() {
        long totalSegundos = tiempoTranscurridoMs() / 1000;
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public int calcularPuntuacion() {
        int puntos = PUNTUACION_BASE;

        puntos -= errores * PENALIZACION_ERROR;
        puntos -= pistasUsadas * PENALIZACION_PISTA;

        long tiempoMs = tiempoTranscurridoMs();
        if (tiempoMs > TIEMPO_GRATIS_MS) {
            long exceso = tiempoMs - TIEMPO_GRATIS_MS;
            puntos -= (int) (exceso / INTERVALO_PENALIZACION_MS);
        }

        puntos = Math.max(0, puntos);

        double multiplicador = multiplicadorDificultad();
        return (int) (puntos * multiplicador);
    }

    private double multiplicadorDificultad() {
        switch (dificultad) {
            case FACIL: return 1.0;
            case MEDIO: return 1.5;
            case DIFICIL: return 2.0;
            default: return 1.0;
        }
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    @Override
    public String toString() {
        return String.format(
                "Dificultad: %s | Tiempo: %s | Movimientos: %d | Errores: %d | Pistas: %d | Puntuacion: %d",
                dificultad, tiempoFormateado(), movimientos, errores, pistasUsadas, calcularPuntuacion());
    }
}
