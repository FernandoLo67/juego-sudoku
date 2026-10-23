# Juego de Sudoku

Sudoku de consola con generador de tableros de solucion unica, solucionador por backtracking, tres dificultades, pistas y validacion de jugadas.

El defecto principal es de los que solo aparecen cuando el usuario ya lo esta pasando mal: **pedir una pista tumbaba el programa**, y solo despues de equivocarse.

## El defecto de fondo: valido no es lo mismo que correcto

`Tablero.jugar` comprueba que el numero no choque con los que ya estan escritos en su fila, columna y caja. Eso es **validez**. No es lo mismo que **correccion**: un numero puede caber donde se pone y aun asi no ser el que va ahi.

```
celda (2,1): la solucion es 8, el jugador escribe 1
jugar() lo acepta:   esConsistente = true
pero:                contarSoluciones = 0
```

El tablero no tiene ninguna contradiccion visible y ya no se puede terminar. El jugador sigue rellenando casillas durante media hora y solo se entera cuando se queda sin sitio — sin saber en que momento se torcio.

El juego **tenia** la informacion para avisarle: `SolucionadorSudoku.contarSoluciones` ya existia y se usaba en el generador. Simplemente no se le preguntaba.

## Y por eso la pista mataba el juego

`Main.darPista` buscaba la primera casilla vacia y escribia ahi el valor de la solucion — con `jugar()`, que valida:

```java
if (tablero.get(fila, columna) == Tablero.VACIA) {
    int valor = solucion.get(fila, columna);
    tablero.jugar(fila, columna, valor);      // <- puede lanzar
    ...
}
```

Si el error del jugador choca con ese valor:

```
el jugador escribe 5 en (1,4) -- valido, esConsistente = true
pide pista -> darPista() hace jugar(1,3,5)
  JugadaInvalidaException: El 5 choca con otro numero en la fila, columna o caja
```

Y `darPista` no atrapaba nada. El programa terminaba con una traza.

Lo que lo hace peor es **cuando** falla: la pista solo revienta si el jugador ya se ha equivocado, que es exactamente el motivo por el que la pide. **Pedir ayuda mataba el juego, y justo en el momento en que hacia falta.**

## La correccion: una clase que sepa la respuesta

La logica de la pista vivia dentro de `Main`, donde no habia forma de probarla. Ahi esta el motivo de que nadie lo notara: no es que la prueba fallara, es que no habia ningun sitio donde escribirla.

`Partida` junta el tablero del jugador con su solucion y contesta lo que hace falta:

| metodo | contesta |
|---|---|
| `celdasIncorrectas()` | que hay mal puesto |
| `esIncorrecta(f, c)` | si esa casilla en concreto esta mal |
| `sigueSiendoResoluble()` | si todavia se puede terminar |
| `pedirPista()` | **corrige un error si lo hay** antes de revelar nada |

Que la pista empiece por corregir no es una comodidad: es lo unico que tiene sentido. Revelar una casilla mas sobre un tablero que ya no tiene solucion no ayuda a nadie.

Y `pedirPista()` **no lanza nunca**. Devuelve un `Optional<Pista>`: vacio solo cuando ya no queda nada que revelar.

La prueba fuerte pide pistas hasta el final metiendo un error del jugador cada dos vueltas, y comprueba que ninguna de las ~38 llamadas lanza y que el tablero acaba resuelto.

## Las decisiones de diseno

**El generador quita casillas mientras la solucion siga siendo unica.** Ese es el requisito que distingue un sudoku de una cuadricula con huecos: si hay dos soluciones, el jugador puede rellenar una valida y que el juego le diga que esta mal.

```java
valores[fila][columna] = VACIA;
if (SolucionadorSudoku.tieneSolucionUnica(new Tablero(valores))) {
    quitadas++;
} else {
    valores[fila][columna] = respaldo;    // se devuelve
}
```

Medido sobre 20 semillas por dificultad, las tres alcanzan siempre el numero de casillas pedido: 38, 46 y 52. Generar un DIFICIL cuesta unos 9.5 ms; un FACIL, 1.6 ms. La diferencia es que cada intento de quitar una casilla obliga a contar soluciones sobre el tablero entero.

**`contarSoluciones` para de contar al llegar al maximo.**

```java
public static boolean tieneSolucionUnica(Tablero tablero) {
    return contarSoluciones(tablero, 2) == 1;
}
```

Con maximo 2 alcanza para saber si es unica, y evita explorar el arbol entero de un tablero casi vacio — que tiene miles de millones de soluciones.

**El tablero distingue las casillas del enunciado de las del jugador.** `jugar()` se niega a tocar las fijas; `set()` es de paquete y no comprueba nada, y lo usa el solucionador, que necesita escribir en cualquier sitio.

**`esMovimientoValido` excluye la propia casilla** de la comprobacion, lo que permite usarlo para tres cosas distintas: validar una jugada nueva, cambiar el valor de una casilla que ya tiene uno, y comprobar la consistencia de un tablero entero.

**`resolver` trabaja en el sitio y `resolverCopia` no.** Los dos existen porque los dos usos son reales: el solucionador interno necesita mutar, y el juego necesita una solucion sin tocar el tablero del jugador.

**Las pistas se cuentan solo cuando sirven de algo.** Una pista sobre un tablero terminado no incrementa el contador.

## Las 49 pruebas

**`PartidaTest` (14)**

| grupo | que cubre |
|---|---|
| **Valido contra correcto** | El defecto de fondo: una jugada valida que deja el tablero sin solucion; tablero limpio; jugadas correctas; borrar el error |
| **La pista** | El defecto que tumbaba el programa; corrige antes de revelar; revela el valor de la solucion; ~38 pistas seguidas sin lanzar; el contador |
| Construccion | Tablero y solucion obligatorios; la solucion es una copia; las casillas del enunciado siguen protegidas |

**`TableroTest` (15)** — validez en fila, columna y caja; casillas fijas; consistencia; coordenadas y valores fuera de rango; copiar; el formato.

**`SolucionadorSudokuTest` (11)** — resuelve, respeta el original, detecta los sin solucion, cuenta con tope.

**`GeneradorSudokuTest` (9)** — cuadricula completa valida, solucion unica, semilla reproducible, casillas por dificultad.

## Estructura

```
src/main/java/com/fernando/sudoku/
├── Tablero.java                  9x9, casillas fijas y validez.
├── Dificultad.java               Cuantas casillas quitar.
├── SolucionadorSudoku.java       Backtracking; cuenta soluciones con tope.
├── GeneradorSudoku.java          Cuadricula completa y vaciado con solucion unica.
├── Partida.java                  Tablero + solucion: errores, resolubilidad y pistas.
├── JugadaInvalidaException.java
└── Main.java                     Solo lee del teclado y escribe en pantalla.
src/test/java/com/fernando/sudoku/
├── PartidaTest.java            14 pruebas.
├── TableroTest.java            15 pruebas.
├── SolucionadorSudokuTest.java 11 pruebas.
└── GeneradorSudokuTest.java     9 pruebas.
```

## Como ejecutar

```bash
mvn package
java -jar target/juego-sudoku-1.0.0.jar
```

## Como correr las pruebas

```bash
mvn test
```
