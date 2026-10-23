package com.fernando.sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Una partida en curso: el tablero del jugador y la solucion que le corresponde.
 *
 * <h2>El defecto: el juego no distinguia "valido" de "correcto"</h2>
 *
 * {@link Tablero#jugar} comprueba que el numero no choque con los que ya estan
 * escritos. Eso es <b>validez</b>, y no es lo mismo que <b>correccion</b>: un
 * numero puede caber donde se pone y aun asi no ser el que va ahi.
 *
 * <pre>
 * celda (2,1): la solucion es 8, el jugador escribe 1
 * jugar() lo acepta:   esConsistente = true
 * pero el tablero ya no tiene solucion: contarSoluciones = 0
 * </pre>
 *
 * <p>El jugador sigue rellenando casillas durante media hora sobre un tablero
 * que ya no se puede terminar, y solo se entera cuando se queda sin sitio.
 *
 * <h2>Y por eso la pista tumbaba el programa</h2>
 *
 * {@code Main.darPista} buscaba la primera casilla vacia y escribia ahi el valor
 * de la solucion — con {@code jugar()}, que valida. Si el error del jugador
 * chocaba con ese valor:
 *
 * <pre>
 * el jugador escribe 5 en (1,4) -- valido
 * pide pista -> darPista() hace jugar(1,3,5)
 *   JugadaInvalidaException: El 5 choca con otro numero en la fila, columna o caja
 * </pre>
 *
 * <p>Y {@code darPista} no atrapaba nada: el programa terminaba con una traza.
 * <b>Pedir ayuda mataba el juego, y justo en el momento en que hacia falta</b> —
 * porque la pista solo revienta cuando el jugador ya se ha equivocado, que es
 * exactamente cuando la pide.
 *
 * <h2>La correccion</h2>
 *
 * La logica de la pista estaba en {@code Main}, donde no habia forma de
 * probarla. Aqui hay una clase que conoce la solucion y puede responder lo que
 * el jugador necesita:
 *
 * <ul>
 *   <li>{@link #celdasIncorrectas()} — que hay mal puesto</li>
 *   <li>{@link #sigueSiendoResoluble()} — si todavia se puede terminar</li>
 *   <li>{@link #pedirPista()} — que <b>corrige un error si lo hay</b> antes de
 *       revelar nada, y no lanza nunca</li>
 * </ul>
 *
 * <p>Que la pista empiece por corregir no es una comodidad: es lo unico que
 * tiene sentido. Revelar una casilla mas sobre un tablero que ya no tiene
 * solucion no ayuda a nadie.
 */
public class Partida {

    private final Tablero tablero;
    private final Tablero solucion;
    private int pistasUsadas;

    /**
     * @param tablero  tablero del enunciado
     * @param solucion su solucion completa
     * @throws IllegalArgumentException si falta alguno o la solucion no esta resuelta
     */
    public Partida(Tablero tablero, Tablero solucion) {
        if (tablero == null || solucion == null) {
            throw new IllegalArgumentException("El tablero y la solucion son obligatorios");
        }
        if (!solucion.estaResuelto()) {
            throw new IllegalArgumentException("La solucion tiene que estar completa y ser valida");
        }
        this.tablero = tablero;
        this.solucion = solucion;
    }

    /**
     * Empieza una partida nueva.
     *
     * @param generador  de donde sale el tablero
     * @param dificultad cuantas casillas quitar
     * @return la partida, con su solucion ya calculada
     */
    public static Partida nueva(GeneradorSudoku generador, Dificultad dificultad) {
        Tablero tablero = generador.generar(dificultad);
        return new Partida(tablero, SolucionadorSudoku.resolverCopia(tablero));
    }

    /** @return el tablero en el que juega el usuario */
    public Tablero getTablero() {
        return tablero;
    }

    /** @return cuantas pistas se han pedido */
    public int getPistasUsadas() {
        return pistasUsadas;
    }

    /**
     * Escribe una jugada del usuario.
     *
     * <p>Se aceptan las jugadas <b>validas</b> aunque no sean correctas: parte de
     * jugar al sudoku es equivocarse. La diferencia es que ahora el juego puede
     * decirlo — ver {@link #celdasIncorrectas()}.
     *
     * @param fila    fila, base cero
     * @param columna columna, base cero
     * @param valor   numero del 1 al 9, o {@code 0} para borrar
     * @throws JugadaInvalidaException si la casilla es del enunciado o el numero choca
     */
    public void jugar(int fila, int columna, int valor) {
        tablero.jugar(fila, columna, valor);
    }

    /**
     * @param fila    fila
     * @param columna columna
     * @return {@code true} si esa casilla tiene un numero y no es el de la solucion
     */
    public boolean esIncorrecta(int fila, int columna) {
        int valor = tablero.get(fila, columna);
        return valor != Tablero.VACIA && valor != solucion.get(fila, columna);
    }

    /**
     * @return las casillas con un numero que no corresponde, en orden de lectura.
     *         Vacia si todo lo escrito hasta ahora va bien.
     */
    public List<int[]> celdasIncorrectas() {
        List<int[]> incorrectas = new ArrayList<>();
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (esIncorrecta(fila, columna)) {
                    incorrectas.add(new int[]{fila, columna});
                }
            }
        }
        return incorrectas;
    }

    /**
     * @return {@code true} si desde este estado todavia se puede llegar a una
     *         solucion. Es lo que {@code esConsistente()} no puede contestar:
     *         un tablero sin contradicciones visibles puede ser ya imposible.
     */
    public boolean sigueSiendoResoluble() {
        return SolucionadorSudoku.contarSoluciones(tablero, 1) > 0;
    }

    /** @return {@code true} si el tablero esta completo y correcto */
    public boolean estaResuelto() {
        return tablero.estaResuelto();
    }

    /**
     * Da una pista, sin lanzar nunca.
     *
     * <p>Si hay algo mal puesto, la pista es <b>eso</b>: se borra la casilla y se
     * avisa. Solo cuando no queda ningun error se revela una casilla nueva.
     *
     * @return la pista, o vacio si el tablero ya esta terminado
     */
    public Optional<Pista> pedirPista() {
        pistasUsadas++;

        List<int[]> incorrectas = celdasIncorrectas();
        if (!incorrectas.isEmpty()) {
            int[] celda = incorrectas.get(0);
            int equivocado = tablero.get(celda[0], celda[1]);
            tablero.jugar(celda[0], celda[1], Tablero.VACIA);
            return Optional.of(new Pista(celda[0], celda[1], equivocado, Pista.Tipo.CORRECCION));
        }

        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (tablero.get(fila, columna) == Tablero.VACIA) {
                    int valor = solucion.get(fila, columna);
                    tablero.jugar(fila, columna, valor);
                    return Optional.of(new Pista(fila, columna, valor, Pista.Tipo.REVELACION));
                }
            }
        }

        pistasUsadas--;   // no habia nada que revelar
        return Optional.empty();
    }

    /** @return una copia de la solucion, para cuando el jugador se rinde */
    public Tablero verSolucion() {
        return solucion.copiar();
    }

    /**
     * Lo que devuelve {@link #pedirPista()}.
     *
     * @param fila    fila afectada, base cero
     * @param columna columna afectada, base cero
     * @param valor   el numero revelado, o el que estaba mal en una correccion
     * @param tipo    si se corrigio un error o se revelo una casilla
     */
    public static final class Pista {

        private final int fila;
        private final int columna;
        private final int valor;
        private final Tipo tipo;

        public Pista(int fila, int columna, int valor, Tipo tipo) {
            this.fila = fila;
            this.columna = columna;
            this.valor = valor;
            this.tipo = tipo;
        }

        public int fila() {
            return fila;
        }

        public int columna() {
            return columna;
        }

        public int valor() {
            return valor;
        }

        public Tipo tipo() {
            return tipo;
        }

        /** Que clase de ayuda es. */
        public enum Tipo {
            /** Se borro un numero que estaba mal. */
            CORRECCION,
            /** Se revelo el numero de una casilla vacia. */
            REVELACION
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pista)) {
                return false;
            }
            Pista otra = (Pista) o;
            return fila == otra.fila
                    && columna == otra.columna
                    && valor == otra.valor
                    && tipo == otra.tipo;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fila, columna, valor, tipo);
        }

        @Override
        public String toString() {
            switch (tipo) {
                case CORRECCION:
                    return String.format(
                            "El %d de la fila %d, columna %d esta mal: se ha borrado",
                            valor, fila + 1, columna + 1);
                case REVELACION:
                    return String.format(
                            "En la fila %d, columna %d va un %d", fila + 1, columna + 1, valor);
                default:
                    throw new IllegalStateException("Tipo de pista desconocido: " + tipo);
            }
        }
    }
}
