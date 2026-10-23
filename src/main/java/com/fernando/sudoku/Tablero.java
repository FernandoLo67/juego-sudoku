package com.fernando.sudoku;

import java.util.Arrays;

/**
 * Tablero de Sudoku 9x9. El 0 representa una casilla vacia.
 *
 * Distingue entre las casillas "fijas" (las que trae el enunciado y no se pueden
 * tocar) y las que escribe el jugador.
 */
public class Tablero {

    public static final int TAMANO = 9;
    public static final int TAMANO_CAJA = 3;
    public static final int VACIA = 0;

    private final int[][] celdas;
    private final boolean[][] fijas;

    public Tablero() {
        this.celdas = new int[TAMANO][TAMANO];
        this.fijas = new boolean[TAMANO][TAMANO];
    }

    public Tablero(int[][] valores) {
        this();
        if (valores.length != TAMANO) {
            throw new IllegalArgumentException("El tablero debe tener " + TAMANO + " filas");
        }
        for (int fila = 0; fila < TAMANO; fila++) {
            if (valores[fila].length != TAMANO) {
                throw new IllegalArgumentException("La fila " + fila + " debe tener " + TAMANO + " columnas");
            }
            for (int columna = 0; columna < TAMANO; columna++) {
                int valor = valores[fila][columna];
                if (valor < 0 || valor > 9) {
                    throw new IllegalArgumentException("Valor invalido en (" + fila + "," + columna + "): " + valor);
                }
                celdas[fila][columna] = valor;
                fijas[fila][columna] = valor != VACIA;
            }
        }
    }

    public Tablero copiar() {
        Tablero copia = new Tablero();
        for (int fila = 0; fila < TAMANO; fila++) {
            copia.celdas[fila] = Arrays.copyOf(celdas[fila], TAMANO);
            copia.fijas[fila] = Arrays.copyOf(fijas[fila], TAMANO);
        }
        return copia;
    }

    public int get(int fila, int columna) {
        exigirCoordenadas(fila, columna);
        return celdas[fila][columna];
    }

    public boolean esFija(int fila, int columna) {
        exigirCoordenadas(fila, columna);
        return fijas[fila][columna];
    }

    /** Escribe sin comprobar si la casilla es fija (lo usa el solucionador). */
    void set(int fila, int columna, int valor) {
        exigirCoordenadas(fila, columna);
        exigirValor(valor);
        celdas[fila][columna] = valor;
    }

    /** Escribe una jugada del usuario: falla si la casilla es del enunciado. */
    public void jugar(int fila, int columna, int valor) {
        exigirCoordenadas(fila, columna);
        exigirValor(valor);
        if (fijas[fila][columna]) {
            throw new JugadaInvalidaException("La casilla (" + (fila + 1) + "," + (columna + 1) + ") es del enunciado");
        }
        if (valor != VACIA && !esMovimientoValido(fila, columna, valor)) {
            throw new JugadaInvalidaException("El " + valor + " choca con otro numero en la fila, columna o caja");
        }
        celdas[fila][columna] = valor;
    }

    /** true si poner ese valor ahi no repite en fila, columna ni caja de 3x3. */
    public boolean esMovimientoValido(int fila, int columna, int valor) {
        exigirCoordenadas(fila, columna);
        exigirValor(valor);
        if (valor == VACIA) {
            return true;
        }
        for (int i = 0; i < TAMANO; i++) {
            if (i != columna && celdas[fila][i] == valor) {
                return false;
            }
            if (i != fila && celdas[i][columna] == valor) {
                return false;
            }
        }
        int filaCaja = (fila / TAMANO_CAJA) * TAMANO_CAJA;
        int columnaCaja = (columna / TAMANO_CAJA) * TAMANO_CAJA;
        for (int f = filaCaja; f < filaCaja + TAMANO_CAJA; f++) {
            for (int c = columnaCaja; c < columnaCaja + TAMANO_CAJA; c++) {
                if ((f != fila || c != columna) && celdas[f][c] == valor) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean estaLleno() {
        for (int[] fila : celdas) {
            for (int valor : fila) {
                if (valor == VACIA) {
                    return false;
                }
            }
        }
        return true;
    }

    /** true si no hay ninguna contradiccion entre los numeros escritos. */
    public boolean esConsistente() {
        for (int fila = 0; fila < TAMANO; fila++) {
            for (int columna = 0; columna < TAMANO; columna++) {
                int valor = celdas[fila][columna];
                if (valor != VACIA && !esMovimientoValido(fila, columna, valor)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean estaResuelto() {
        return estaLleno() && esConsistente();
    }

    public int celdasVacias() {
        int total = 0;
        for (int[] fila : celdas) {
            for (int valor : fila) {
                if (valor == VACIA) {
                    total++;
                }
            }
        }
        return total;
    }

    public int[][] aMatriz() {
        int[][] copia = new int[TAMANO][];
        for (int fila = 0; fila < TAMANO; fila++) {
            copia[fila] = Arrays.copyOf(celdas[fila], TAMANO);
        }
        return copia;
    }

    @Override
    public String toString() {
        StringBuilder salida = new StringBuilder();
        salida.append("    1 2 3   4 5 6   7 8 9\n");
        salida.append("  +-------+-------+-------+\n");
        for (int fila = 0; fila < TAMANO; fila++) {
            salida.append(fila + 1).append(' ');
            for (int columna = 0; columna < TAMANO; columna++) {
                if (columna % TAMANO_CAJA == 0) {
                    salida.append("| ");
                }
                int valor = celdas[fila][columna];
                salida.append(valor == VACIA ? "." : valor).append(' ');
            }
            salida.append("|\n");
            if ((fila + 1) % TAMANO_CAJA == 0) {
                salida.append("  +-------+-------+-------+\n");
            }
        }
        return salida.toString();
    }

    private void exigirCoordenadas(int fila, int columna) {
        if (fila < 0 || fila >= TAMANO || columna < 0 || columna >= TAMANO) {
            throw new IndexOutOfBoundsException("Coordenadas fuera del tablero: (" + fila + "," + columna + ")");
        }
    }

    private void exigirValor(int valor) {
        if (valor < 0 || valor > 9) {
            throw new IllegalArgumentException("El valor debe estar entre 0 y 9, y fue " + valor);
        }
    }
}
