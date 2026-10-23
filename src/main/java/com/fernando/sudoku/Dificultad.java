package com.fernando.sudoku;

public enum Dificultad {
    FACIL(38),
    MEDIO(46),
    DIFICIL(52);

    private final int celdasAQuitar;

    Dificultad(int celdasAQuitar) {
        this.celdasAQuitar = celdasAQuitar;
    }

    public int getCeldasAQuitar() {
        return celdasAQuitar;
    }
}
