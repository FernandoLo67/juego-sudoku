package com.fernando.sudoku;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableroTest {

    private static final int[][] ENUNCIADO = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}};

    @Test
    void unTableroNuevoEstaVacioYEsConsistente() {
        Tablero tablero = new Tablero();
        assertEquals(81, tablero.celdasVacias());
        assertTrue(tablero.esConsistente());
        assertFalse(tablero.estaLleno());
    }

    @Test
    void lasCasillasDelEnunciadoQuedanMarcadasComoFijas() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertTrue(tablero.esFija(0, 0));
        assertFalse(tablero.esFija(0, 2));
    }

    @Test
    void noSePuedeRepetirUnNumeroEnLaMismaFila() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertFalse(tablero.esMovimientoValido(0, 2, 5));
    }

    @Test
    void noSePuedeRepetirUnNumeroEnLaMismaColumna() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertFalse(tablero.esMovimientoValido(2, 0, 6));
    }

    @Test
    void noSePuedeRepetirUnNumeroEnLaMismaCaja() {
        Tablero tablero = new Tablero(ENUNCIADO);
        // en la caja superior izquierda ya hay un 9 (posicion 2,1)
        assertFalse(tablero.esMovimientoValido(0, 2, 9));
    }

    @Test
    void unMovimientoSinConflictosEsValido() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertTrue(tablero.esMovimientoValido(0, 2, 4));
    }

    @Test
    void jugarEnUnaCasillaDelEnunciadoEsRechazado() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertThrows(JugadaInvalidaException.class, () -> tablero.jugar(0, 0, 9));
    }

    @Test
    void jugarUnNumeroQueChocaEsRechazado() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertThrows(JugadaInvalidaException.class, () -> tablero.jugar(0, 2, 5));
    }

    @Test
    void jugarUnNumeroValidoLoEscribeEnElTablero() {
        Tablero tablero = new Tablero(ENUNCIADO);
        int vaciasAntes = tablero.celdasVacias();

        tablero.jugar(0, 2, 4);

        assertEquals(4, tablero.get(0, 2));
        assertEquals(vaciasAntes - 1, tablero.celdasVacias());
    }

    @Test
    void jugarCeroBorraLaCasilla() {
        Tablero tablero = new Tablero(ENUNCIADO);
        tablero.jugar(0, 2, 4);
        tablero.jugar(0, 2, Tablero.VACIA);
        assertEquals(Tablero.VACIA, tablero.get(0, 2));
    }

    @Test
    void coordenadasFueraDelTableroLanzanExcepcion() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertThrows(IndexOutOfBoundsException.class, () -> tablero.get(9, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> tablero.get(0, -1));
    }

    @Test
    void valoresFueraDeUnoANueveSonRechazados() {
        Tablero tablero = new Tablero(ENUNCIADO);
        assertThrows(IllegalArgumentException.class, () -> tablero.jugar(0, 2, 10));
        assertThrows(IllegalArgumentException.class, () -> tablero.jugar(0, 2, -1));
    }

    @Test
    void unaMatrizConFilasDeMenosEsRechazada() {
        assertThrows(IllegalArgumentException.class, () -> new Tablero(new int[3][9]));
        assertThrows(IllegalArgumentException.class, () -> new Tablero(new int[9][3]));
    }

    @Test
    void copiarNoComparteEstadoConElOriginal() {
        Tablero original = new Tablero(ENUNCIADO);
        Tablero copia = original.copiar();
        copia.jugar(0, 2, 4);

        assertEquals(4, copia.get(0, 2));
        assertEquals(Tablero.VACIA, original.get(0, 2));
    }

    @Test
    void laRepresentacionDeTextoMuestraLaCuadricula() {
        String texto = new Tablero(ENUNCIADO).toString();
        assertTrue(texto.contains("+-------+-------+-------+"));
        assertTrue(texto.contains("."), "Las casillas vacias se muestran con un punto");
    }
}
