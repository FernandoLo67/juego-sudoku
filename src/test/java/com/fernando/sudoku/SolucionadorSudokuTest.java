package com.fernando.sudoku;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolucionadorSudokuTest {

    private static final int[][] ENUNCIADO_CLASICO = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}};

    /** El mismo enunciado, con la solucion conocida en la primera fila. */
    private static final int[] PRIMERA_FILA_ESPERADA = {5, 3, 4, 6, 7, 8, 9, 1, 2};

    @Test
    void resuelveUnSudokuClasico() {
        Tablero tablero = new Tablero(ENUNCIADO_CLASICO);
        assertTrue(SolucionadorSudoku.resolver(tablero));
        assertTrue(tablero.estaResuelto());
    }

    @Test
    void laSolucionCoincideConLaEsperada() {
        Tablero resuelto = SolucionadorSudoku.resolverCopia(new Tablero(ENUNCIADO_CLASICO));
        for (int columna = 0; columna < Tablero.TAMANO; columna++) {
            assertEquals(PRIMERA_FILA_ESPERADA[columna], resuelto.get(0, columna),
                    "Diferencia en la columna " + columna);
        }
    }

    @Test
    void resolverCopiaDejaIntactoElTableroOriginal() {
        Tablero original = new Tablero(ENUNCIADO_CLASICO);
        int vaciasAntes = original.celdasVacias();

        SolucionadorSudoku.resolverCopia(original);

        assertEquals(vaciasAntes, original.celdasVacias());
    }

    @Test
    void resuelveUnTableroCompletamenteVacio() {
        Tablero vacio = new Tablero();
        assertTrue(SolucionadorSudoku.resolver(vacio));
        assertTrue(vacio.estaResuelto());
    }

    @Test
    void unTableroInconsistenteNoTieneSolucion() {
        int[][] conRepetido = new int[9][9];
        conRepetido[0][0] = 5;
        conRepetido[0][1] = 5;

        assertFalse(SolucionadorSudoku.resolver(new Tablero(conRepetido)));
        assertEquals(0, SolucionadorSudoku.contarSoluciones(new Tablero(conRepetido), 2));
    }

    @Test
    void unTableroSinSalidaDevuelveFalso() {
        // Fila valida pero que deja la primera casilla sin ningun candidato posible
        int[][] imposible = new int[9][9];
        imposible[0] = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9};
        imposible[1] = new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[2] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[3] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[4] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[5] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[6] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[7] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        imposible[8] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

        // en (0,0) no cabe ningun numero: 2..9 estan en la fila y el 1 en la columna
        Tablero tablero = new Tablero(imposible);
        for (int valor = 1; valor <= 9; valor++) {
            assertFalse(tablero.esMovimientoValido(0, 0, valor), "El " + valor + " no deberia caber");
        }
        assertFalse(SolucionadorSudoku.resolver(tablero));
    }

    @Test
    void elEnunciadoClasicoTieneSolucionUnica() {
        assertTrue(SolucionadorSudoku.tieneSolucionUnica(new Tablero(ENUNCIADO_CLASICO)));
        assertEquals(1, SolucionadorSudoku.contarSoluciones(new Tablero(ENUNCIADO_CLASICO), 2));
    }

    @Test
    void unTableroCasiVacioTieneMuchasSoluciones() {
        int[][] casiVacio = new int[9][9];
        casiVacio[0][0] = 1;
        assertEquals(2, SolucionadorSudoku.contarSoluciones(new Tablero(casiVacio), 2));
        assertFalse(SolucionadorSudoku.tieneSolucionUnica(new Tablero(casiVacio)));
    }

    @Test
    void unTableroYaResueltoCuentaComoUnaSolucion() {
        Tablero resuelto = SolucionadorSudoku.resolverCopia(new Tablero(ENUNCIADO_CLASICO));
        assertEquals(1, SolucionadorSudoku.contarSoluciones(resuelto, 2));
    }

    @Test
    void resolverCopiaDeUnTableroSinSolucionLanzaExcepcion() {
        int[][] conRepetido = new int[9][9];
        conRepetido[0][0] = 5;
        conRepetido[0][1] = 5;
        assertThrows(IllegalArgumentException.class,
                () -> SolucionadorSudoku.resolverCopia(new Tablero(conRepetido)));
    }

    @Test
    void maximoNoPositivoEsRechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> SolucionadorSudoku.contarSoluciones(new Tablero(), 0));
    }
}
