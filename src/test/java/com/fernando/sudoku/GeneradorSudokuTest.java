package com.fernando.sudoku;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorSudokuTest {

    @Test
    void elTableroCompletoEstaLlenoYEsValido() {
        Tablero completo = new GeneradorSudoku(new Random(42)).generarCompleto();

        assertTrue(completo.estaLleno());
        assertTrue(completo.esConsistente());
        assertTrue(completo.estaResuelto());
        assertEquals(0, completo.celdasVacias());
    }

    @Test
    void cadaFilaDelTableroCompletoTieneLosNueveNumeros() {
        Tablero completo = new GeneradorSudoku(new Random(7)).generarCompleto();

        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            boolean[] vistos = new boolean[10];
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                int valor = completo.get(fila, columna);
                assertFalse(vistos[valor], "El " + valor + " se repite en la fila " + fila);
                vistos[valor] = true;
            }
        }
    }

    @Test
    void cadaColumnaDelTableroCompletoTieneLosNueveNumeros() {
        Tablero completo = new GeneradorSudoku(new Random(7)).generarCompleto();

        for (int columna = 0; columna < Tablero.TAMANO; columna++) {
            boolean[] vistos = new boolean[10];
            for (int fila = 0; fila < Tablero.TAMANO; fila++) {
                int valor = completo.get(fila, columna);
                assertFalse(vistos[valor], "El " + valor + " se repite en la columna " + columna);
                vistos[valor] = true;
            }
        }
    }

    @Test
    void elTableroGeneradoTieneSolucionUnica() {
        Tablero puzzle = new GeneradorSudoku(new Random(123)).generar(Dificultad.FACIL);

        assertTrue(puzzle.esConsistente());
        assertTrue(SolucionadorSudoku.tieneSolucionUnica(puzzle),
                "Un Sudoku bien generado debe tener exactamente una solucion");
    }

    @Test
    void elTableroGeneradoSePuedeResolver() {
        Tablero puzzle = new GeneradorSudoku(new Random(555)).generar(Dificultad.MEDIO);
        assertTrue(SolucionadorSudoku.resolver(puzzle.copiar()));
    }

    @Test
    void masDificultadImplicaAlMenosTantasCasillasVacias() {
        GeneradorSudoku generador = new GeneradorSudoku(new Random(9));
        int facil = generador.generar(Dificultad.FACIL).celdasVacias();
        int dificil = new GeneradorSudoku(new Random(9)).generar(Dificultad.DIFICIL).celdasVacias();

        assertTrue(facil > 0);
        assertTrue(dificil >= facil, "DIFICIL (" + dificil + ") deberia dejar al menos tantas vacias como FACIL (" + facil + ")");
    }

    @Test
    void lasCasillasQueQuedanSonDelEnunciado() {
        Tablero puzzle = new GeneradorSudoku(new Random(31)).generar(Dificultad.FACIL);

        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                boolean tieneValor = puzzle.get(fila, columna) != Tablero.VACIA;
                assertEquals(tieneValor, puzzle.esFija(fila, columna));
            }
        }
    }

    @Test
    void laMismaSemillaGeneraElMismoTablero() {
        Tablero uno = new GeneradorSudoku(new Random(2024)).generarCompleto();
        Tablero otro = new GeneradorSudoku(new Random(2024)).generarCompleto();

        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            assertArrayEquals(uno.aMatriz()[fila], otro.aMatriz()[fila], "Difiere la fila " + fila);
        }
    }

    @Test
    void semillasDistintasGeneranTablerosDistintos() {
        Tablero uno = new GeneradorSudoku(new Random(1)).generarCompleto();
        Tablero otro = new GeneradorSudoku(new Random(2)).generarCompleto();

        boolean hayDiferencia = false;
        for (int fila = 0; fila < Tablero.TAMANO && !hayDiferencia; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (uno.get(fila, columna) != otro.get(fila, columna)) {
                    hayDiferencia = true;
                    break;
                }
            }
        }
        assertTrue(hayDiferencia, "Dos semillas distintas deberian dar tableros distintos");
    }
}
