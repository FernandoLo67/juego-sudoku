package com.fernando.sudoku;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de {@link Partida}.
 *
 * <p>Esta clase no existia: la logica de la pista vivia dentro de {@code Main},
 * donde no habia forma de probarla — y por eso nadie noto que <b>pedir una pista
 * podia tumbar el programa</b>.
 *
 * <p>Lo que aqui se comprueba es la distincion que le faltaba al juego: una
 * jugada puede ser <b>valida</b> — no choca con lo escrito — y no ser
 * <b>correcta</b>.
 */
class PartidaTest {

    private Partida partida;
    private Tablero solucion;

    @BeforeEach
    void prepararPartida() {
        partida = Partida.nueva(new GeneradorSudoku(new Random(2)), Dificultad.FACIL);
        solucion = partida.verSolucion();
    }

    /** Escribe un valor valido pero equivocado en la primera casilla vacia que lo admita. */
    private int[] escribirUnErrorValido() {
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (partida.getTablero().get(fila, columna) != Tablero.VACIA) {
                    continue;
                }
                for (int valor = 1; valor <= Tablero.TAMANO; valor++) {
                    if (valor != solucion.get(fila, columna)
                            && partida.getTablero().esMovimientoValido(fila, columna, valor)) {
                        partida.jugar(fila, columna, valor);
                        return new int[]{fila, columna, valor};
                    }
                }
            }
        }
        throw new IllegalStateException("no se encontro una jugada valida pero incorrecta");
    }

    // ---------------------------------------------------------------
    //  Valido no es lo mismo que correcto
    // ---------------------------------------------------------------

    /**
     * LA PRUEBA QUE DOCUMENTA EL DEFECTO.
     *
     * <p>{@code jugar()} comprueba que el numero no choque con los que ya estan
     * escritos. Eso es validez, no correccion: un numero puede caber donde se
     * pone y no ser el que va ahi.
     *
     * <pre>
     * jugar() lo acepta:  esConsistente = true
     * y sin embargo:      contarSoluciones = 0
     * </pre>
     *
     * <p>El jugador seguia rellenando casillas sobre un tablero que ya no se
     * podia terminar, y solo se enteraba al quedarse sin sitio.
     */
    @Test
    void unaJugadaValidaPuedeSerIncorrecta() {
        int[] error = escribirUnErrorValido();

        assertTrue(partida.getTablero().esConsistente(),
                "no choca con nada: el tablero sigue siendo consistente");
        assertTrue(partida.esIncorrecta(error[0], error[1]), "pero no es el numero que va ahi");
        assertFalse(partida.sigueSiendoResoluble(), "y el tablero ya no se puede terminar");
    }

    @Test
    void unTableroReciennEmpezadoNoTieneErroresYesResoluble() {
        assertTrue(partida.celdasIncorrectas().isEmpty());
        assertTrue(partida.sigueSiendoResoluble());
        assertFalse(partida.estaResuelto());
    }

    @Test
    void lasJugadasCorrectasNoAparecenComoErrores() {
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (partida.getTablero().get(fila, columna) == Tablero.VACIA) {
                    partida.jugar(fila, columna, solucion.get(fila, columna));
                }
            }
        }
        assertTrue(partida.celdasIncorrectas().isEmpty());
        assertTrue(partida.estaResuelto());
    }

    @Test
    void celdasIncorrectasLasListaEnOrdenDeLectura() {
        escribirUnErrorValido();
        List<int[]> incorrectas = partida.celdasIncorrectas();

        assertEquals(1, incorrectas.size());
        assertTrue(partida.esIncorrecta(incorrectas.get(0)[0], incorrectas.get(0)[1]));
    }

    /** Borrar el error lo quita de la lista. */
    @Test
    void borrarUnaCasillaEquivocadaLaQuitaDeLosErrores() {
        int[] error = escribirUnErrorValido();

        partida.jugar(error[0], error[1], Tablero.VACIA);

        assertTrue(partida.celdasIncorrectas().isEmpty());
        assertTrue(partida.sigueSiendoResoluble());
    }

    // ---------------------------------------------------------------
    //  La pista: donde estaba el fallo que tumbaba el programa
    // ---------------------------------------------------------------

    /**
     * LA PRUEBA QUE DOCUMENTA EL DEFECTO CORREGIDO.
     *
     * <p>{@code Main.darPista} buscaba la primera casilla vacia y escribia ahi el
     * valor de la solucion con {@code jugar()}, que valida. Si el error del
     * jugador chocaba con ese valor:
     *
     * <pre>
     * el jugador escribe 5 en (1,4) -- valido
     * pide pista -> jugar(1,3,5) -> JugadaInvalidaException
     * </pre>
     *
     * <p>Y no lo atrapaba nadie. <b>Pedir ayuda mataba el juego</b> — y solo
     * fallaba cuando el jugador ya se habia equivocado, o sea justo cuando pedia
     * la pista.
     */
    @Test
    void pedirPistaConUnErrorEnElTableroNoRevienta() {
        int[] error = escribirUnErrorValido();

        // Con la version anterior esta linea lanzaba JugadaInvalidaException.
        Partida.Pista pista = partida.pedirPista().orElseThrow();

        assertEquals(Partida.Pista.Tipo.CORRECCION, pista.tipo());
        assertEquals(error[0], pista.fila());
        assertEquals(error[1], pista.columna());
        assertEquals(error[2], pista.valor(), "dice cual era el numero equivocado");
        assertEquals(Tablero.VACIA, partida.getTablero().get(error[0], error[1]), "y lo borra");
        assertTrue(partida.sigueSiendoResoluble(), "el tablero vuelve a tener salida");
    }

    /**
     * La pista corrige <b>antes</b> de revelar. Revelar una casilla mas sobre un
     * tablero que ya no tiene solucion no ayuda a nadie.
     */
    @Test
    void laPistaCorrigeAntesDeRevelar() {
        escribirUnErrorValido();
        int vaciasAntes = partida.getTablero().celdasVacias();

        assertEquals(Partida.Pista.Tipo.CORRECCION, partida.pedirPista().orElseThrow().tipo());
        assertEquals(vaciasAntes + 1, partida.getTablero().celdasVacias(), "borro, no revelo");

        assertEquals(Partida.Pista.Tipo.REVELACION, partida.pedirPista().orElseThrow().tipo(),
                "ya sin errores, la siguiente si revela");
    }

    @Test
    void laPistaRevelaElValorDeLaSolucion() {
        Partida.Pista pista = partida.pedirPista().orElseThrow();

        assertEquals(Partida.Pista.Tipo.REVELACION, pista.tipo());
        assertEquals(solucion.get(pista.fila(), pista.columna()), pista.valor());
        assertEquals(pista.valor(), partida.getTablero().get(pista.fila(), pista.columna()));
        assertFalse(partida.esIncorrecta(pista.fila(), pista.columna()));
    }

    /**
     * Pedir pistas hasta el final resuelve el tablero y nunca lanza. Es la
     * prueba fuerte: 38 llamadas seguidas, alternando errores del jugador.
     */
    @Test
    void pedirPistasHastaElFinalResuelveElTableroSinLanzarNunca() {
        int vueltas = 0;
        while (!partida.estaResuelto() && vueltas < 200) {
            // Cada dos vueltas, el jugador mete un error valido si puede.
            if (vueltas % 2 == 0) {
                try {
                    escribirUnErrorValido();
                } catch (IllegalStateException e) {
                    // ya no queda hueco para equivocarse
                }
            }
            assertDoesNotThrow(() -> partida.pedirPista(), "vuelta " + vueltas);
            vueltas++;
        }

        assertTrue(partida.estaResuelto());
        assertTrue(partida.celdasIncorrectas().isEmpty());
        assertTrue(partida.pedirPista().isEmpty(), "sin nada que revelar, devuelve vacio");
    }

    @Test
    void lasPistasSeCuentanSoloCuandoSirvenParaAlgo() {
        assertEquals(0, partida.getPistasUsadas());

        partida.pedirPista();
        assertEquals(1, partida.getPistasUsadas());

        while (partida.pedirPista().isPresent()) {
            // hasta terminar
        }
        int usadas = partida.getPistasUsadas();
        partida.pedirPista();
        assertEquals(usadas, partida.getPistasUsadas(),
                "una pista que no revela nada no cuenta");
    }

    /** El texto de la pista habla en filas y columnas de 1 a 9, como el tablero. */
    @Test
    void elTextoDeLaPistaUsaLaNumeracionDelJugador() {
        assertEquals("En la fila 1, columna 1 va un 5",
                new Partida.Pista(0, 0, 5, Partida.Pista.Tipo.REVELACION).toString());
        assertEquals("El 7 de la fila 9, columna 3 esta mal: se ha borrado",
                new Partida.Pista(8, 2, 7, Partida.Pista.Tipo.CORRECCION).toString());
    }

    // ---------------------------------------------------------------
    //  Construccion
    // ---------------------------------------------------------------

    @Test
    void unaPartidaNecesitaUnTableroYunaSolucionCompleta() {
        Tablero incompleto = new Tablero();

        assertThrows(IllegalArgumentException.class, () -> new Partida(null, solucion));
        assertThrows(IllegalArgumentException.class, () -> new Partida(new Tablero(), null));
        assertThrows(IllegalArgumentException.class, () -> new Partida(new Tablero(), incompleto));
    }

    /** {@code verSolucion} devuelve una copia: modificarla no toca la partida. */
    @Test
    void verSolucionDevuelveUnaCopia() {
        Tablero copia = partida.verSolucion();
        int original = copia.get(0, 0);

        assertEquals(original, partida.verSolucion().get(0, 0));
        assertNotSame(partida.verSolucion(), partida.verSolucion());
    }

    @Test
    void jugarEnUnaCasillaDelEnunciadoSigueLanzando() {
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (partida.getTablero().esFija(fila, columna)) {
                    int f = fila;
                    int c = columna;
                    assertThrows(JugadaInvalidaException.class, () -> partida.jugar(f, c, 1));
                    return;
                }
            }
        }
    }
}
