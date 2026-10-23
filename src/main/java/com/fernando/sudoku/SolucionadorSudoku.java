package com.fernando.sudoku;

/**
 * Resuelve un Sudoku por backtracking: prueba un numero, avanza, y si llega a
 * un callejon sin salida deshace y prueba el siguiente.
 */
public final class SolucionadorSudoku {

    private SolucionadorSudoku() {
    }

    /** Resuelve el tablero en el sitio. Devuelve false si no tiene solucion. */
    public static boolean resolver(Tablero tablero) {
        if (!tablero.esConsistente()) {
            return false;
        }
        return backtracking(tablero);
    }

    /** Devuelve una copia resuelta, dejando el tablero original intacto. */
    public static Tablero resolverCopia(Tablero tablero) {
        Tablero copia = tablero.copiar();
        if (!resolver(copia)) {
            throw new IllegalArgumentException("El tablero no tiene solucion");
        }
        return copia;
    }

    /**
     * Cuenta las soluciones hasta un maximo (para no explorar de mas).
     * Con maximo = 2 alcanza para saber si la solucion es unica.
     */
    public static int contarSoluciones(Tablero tablero, int maximo) {
        if (maximo <= 0) {
            throw new IllegalArgumentException("El maximo debe ser mayor a 0");
        }
        if (!tablero.esConsistente()) {
            return 0;
        }
        return contar(tablero.copiar(), maximo);
    }

    public static boolean tieneSolucionUnica(Tablero tablero) {
        return contarSoluciones(tablero, 2) == 1;
    }

    private static boolean backtracking(Tablero tablero) {
        int[] vacia = buscarCeldaVacia(tablero);
        if (vacia == null) {
            return true;
        }
        int fila = vacia[0];
        int columna = vacia[1];

        for (int valor = 1; valor <= Tablero.TAMANO; valor++) {
            if (tablero.esMovimientoValido(fila, columna, valor)) {
                tablero.set(fila, columna, valor);
                if (backtracking(tablero)) {
                    return true;
                }
                tablero.set(fila, columna, Tablero.VACIA);
            }
        }
        return false;
    }

    private static int contar(Tablero tablero, int maximo) {
        int[] vacia = buscarCeldaVacia(tablero);
        if (vacia == null) {
            return 1;
        }
        int fila = vacia[0];
        int columna = vacia[1];
        int soluciones = 0;

        for (int valor = 1; valor <= Tablero.TAMANO && soluciones < maximo; valor++) {
            if (tablero.esMovimientoValido(fila, columna, valor)) {
                tablero.set(fila, columna, valor);
                soluciones += contar(tablero, maximo - soluciones);
                tablero.set(fila, columna, Tablero.VACIA);
            }
        }
        return soluciones;
    }

    private static int[] buscarCeldaVacia(Tablero tablero) {
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (tablero.get(fila, columna) == Tablero.VACIA) {
                    return new int[]{fila, columna};
                }
            }
        }
        return null;
    }
}
