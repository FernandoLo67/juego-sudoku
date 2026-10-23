package com.fernando.sudoku;

import java.util.Scanner;

/**
 * Menu de consola del sudoku.
 *
 * <p>Solo lee del teclado y escribe en pantalla. La logica de la pista vivia
 * antes aqui — donde no habia forma de probarla — y por eso nadie noto que podia
 * tumbar el programa. Ahora esta en {@link Partida}, con sus pruebas.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Juego Sudoku ===");
        System.out.print("Dificultad (FACIL / MEDIO / DIFICIL): ");
        Dificultad dificultad;
        try {
            dificultad = Dificultad.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Dificultad no reconocida, se usa FACIL.");
            dificultad = Dificultad.FACIL;
        }

        System.out.println("Generando tablero...");
        Partida partida = Partida.nueva(new GeneradorSudoku(), dificultad);
        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println(partida.getTablero());
            System.out.println("Casillas vacias: " + partida.getTablero().celdasVacias());

            if (partida.estaResuelto()) {
                System.out.printf("Felicidades, resolviste el Sudoku! (pistas usadas: %d)%n",
                        partida.getPistasUsadas());
                break;
            }
            if (!partida.celdasIncorrectas().isEmpty()) {
                System.out.printf("Aviso: hay %d casilla(s) mal puestas. Escribe 'revisar' para verlas.%n",
                        partida.celdasIncorrectas().size());
            }

            System.out.println("Escribe: fila columna valor  (por ejemplo '3 5 7', valor 0 para borrar)");
            System.out.println("Otros comandos: 'pista', 'revisar', 'resolver', 'salir'");
            System.out.print("> ");

            String entrada = scanner.nextLine().trim();
            switch (entrada.toLowerCase()) {
                case "salir":
                    salir = true;
                    break;
                case "resolver":
                    System.out.println(partida.verSolucion());
                    salir = true;
                    break;
                case "pista":
                    partida.pedirPista().ifPresentOrElse(
                            System.out::println,
                            () -> System.out.println("Ya no queda nada que revelar."));
                    break;
                case "revisar":
                    revisar(partida);
                    break;
                default:
                    aplicarJugada(partida, entrada);
                    break;
            }
        }
        System.out.println("Gracias por jugar!");
        scanner.close();
    }

    /** Dice que hay mal puesto y si el tablero todavia se puede terminar. */
    private static void revisar(Partida partida) {
        var incorrectas = partida.celdasIncorrectas();
        if (incorrectas.isEmpty()) {
            System.out.println("Todo lo que llevas escrito va bien.");
        } else {
            incorrectas.forEach(celda -> System.out.printf("  la fila %d, columna %d esta mal%n",
                    celda[0] + 1, celda[1] + 1));
        }
        System.out.println(partida.sigueSiendoResoluble()
                ? "El tablero todavia se puede terminar."
                : "Con lo que hay escrito ya no se puede terminar.");
    }

    private static void aplicarJugada(Partida partida, String entrada) {
        String[] partes = entrada.split("\\s+");
        if (partes.length != 3) {
            System.out.println("Formato invalido. Ejemplo: 3 5 7");
            return;
        }
        try {
            int fila = Integer.parseInt(partes[0]) - 1;
            int columna = Integer.parseInt(partes[1]) - 1;
            int valor = Integer.parseInt(partes[2]);
            partida.jugar(fila, columna, valor);
        } catch (NumberFormatException e) {
            System.out.println("Los tres valores deben ser numeros.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Fila y columna deben estar entre 1 y 9.");
        } catch (JugadaInvalidaException | IllegalArgumentException e) {
            System.out.println("Jugada invalida: " + e.getMessage());
        }
    }
}
