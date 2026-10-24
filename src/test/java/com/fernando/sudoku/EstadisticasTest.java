package com.fernando.sudoku;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasTest {

    @Test
    void puntuacionBaseSinPenalizaciones() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        ahora[0] = 30_000;
        stats.terminar();

        assertEquals(1000, stats.calcularPuntuacion());
    }

    @Test
    void erroresPenalizan50PuntosCadaUno() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        stats.registrarError();
        stats.registrarError();
        stats.terminar();

        assertEquals(900, stats.calcularPuntuacion());
    }

    @Test
    void pistasPenalizan100PuntosCadaUna() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        stats.registrarPista();
        stats.terminar();

        assertEquals(900, stats.calcularPuntuacion());
    }

    @Test
    void tiempoExcedidoPenaliza() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        ahora[0] = 90_000; // 90s = 30s de exceso = 3 penalizaciones
        stats.terminar();

        assertEquals(997, stats.calcularPuntuacion());
    }

    @Test
    void dificultadMedioMultiplicaPor1punto5() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.MEDIO, () -> ahora[0]);

        stats.terminar();

        assertEquals(1500, stats.calcularPuntuacion());
    }

    @Test
    void dificultadDificilMultiplicaPor2() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.DIFICIL, () -> ahora[0]);

        stats.terminar();

        assertEquals(2000, stats.calcularPuntuacion());
    }

    @Test
    void laPuntuacionNuncaBajaDeCero() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        for (int i = 0; i < 30; i++) {
            stats.registrarError();
        }
        stats.terminar();

        assertEquals(0, stats.calcularPuntuacion());
    }

    @Test
    void tiempoFormateadoMuestraMinutosYSegundos() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, () -> ahora[0]);

        ahora[0] = 125_000; // 2 min 5 seg
        stats.terminar();

        assertEquals("02:05", stats.tiempoFormateado());
    }

    @Test
    void registrarMovimientosLosAcumula() {
        Estadisticas stats = new Estadisticas(Dificultad.FACIL, System::currentTimeMillis);

        stats.registrarMovimiento();
        stats.registrarMovimiento();
        stats.registrarMovimiento();

        assertEquals(3, stats.getMovimientos());
    }

    @Test
    void toStringIncluyeTodosCampos() {
        long[] ahora = {0L};
        Estadisticas stats = new Estadisticas(Dificultad.MEDIO, () -> ahora[0]);

        stats.registrarMovimiento();
        stats.registrarError();
        stats.registrarPista();
        stats.terminar();

        String texto = stats.toString();
        assertTrue(texto.contains("MEDIO"));
        assertTrue(texto.contains("Movimientos: 1"));
        assertTrue(texto.contains("Errores: 1"));
        assertTrue(texto.contains("Pistas: 1"));
    }
}
