package com.fernando.sudoku;

/**
 * Se lanza cuando una jugada no se puede hacer: la casilla es del enunciado, o
 * el numero choca con otro de la misma fila, columna o caja.
 *
 * <p>Ojo con lo que <b>no</b> significa: que una jugada no lance esto no quiere
 * decir que sea correcta. Un numero puede caber donde se pone y no ser el que va
 * ahi. Esa distincion la resuelve {@link Partida}.
 */
public class JugadaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JugadaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
