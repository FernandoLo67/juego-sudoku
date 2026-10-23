package com.fernando.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Genera tableros de Sudoku:
 *   1. Construye una cuadricula completa valida (backtracking con numeros barajados).
 *   2. Va quitando casillas mientras la solucion siga siendo unica.
 */
public class GeneradorSudoku {

    private final Random random;

    public GeneradorSudoku() {
        this(new Random());
    }

    /** Semilla fija = tablero reproducible, util para las pruebas. */
    public GeneradorSudoku(Random random) {
        this.random = random;
    }

    public Tablero generarCompleto() {
        Tablero tablero = new Tablero();
        rellenar(tablero);
        return new Tablero(tablero.aMatriz());
    }

    public Tablero generar(Dificultad dificultad) {
        Tablero completo = generarCompleto();
        int[][] valores = completo.aMatriz();

        List<int[]> posiciones = new ArrayList<>();
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                posiciones.add(new int[]{fila, columna});
            }
        }
        Collections.shuffle(posiciones, random);

        int quitadas = 0;
        for (int[] posicion : posiciones) {
            if (quitadas >= dificultad.getCeldasAQuitar()) {
                break;
            }
            int fila = posicion[0];
            int columna = posicion[1];
            int respaldo = valores[fila][columna];
            valores[fila][columna] = Tablero.VACIA;

            if (SolucionadorSudoku.tieneSolucionUnica(new Tablero(valores))) {
                quitadas++;
            } else {
                valores[fila][columna] = respaldo;
            }
        }
        return new Tablero(valores);
    }

    private boolean rellenar(Tablero tablero) {
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (tablero.get(fila, columna) != Tablero.VACIA) {
                    continue;
                }
                List<Integer> candidatos = new ArrayList<>();
                for (int valor = 1; valor <= Tablero.TAMANO; valor++) {
                    candidatos.add(valor);
                }
                Collections.shuffle(candidatos, random);

                for (int valor : candidatos) {
                    if (tablero.esMovimientoValido(fila, columna, valor)) {
                        tablero.set(fila, columna, valor);
                        if (rellenar(tablero)) {
                            return true;
                        }
                        tablero.set(fila, columna, Tablero.VACIA);
                    }
                }
                return false;
            }
        }
        return true;
    }
}
