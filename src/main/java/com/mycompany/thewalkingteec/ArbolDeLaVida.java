/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */

import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author Melissa
 */


    public class ArbolDeLaVida extends Defensa {
    private JLabel refLabel;
    private Pantalla refPantalla;

    public ArbolDeLaVida(int fila, int columna, JLabel refLabel, Pantalla refPantalla) {
        super();
        setVidaActual(1000);
        setVidaInicial(1000);
        setGolpesPorSegundo(0);
        setPoderGolpe(0);
        this.refLabel = refLabel;
        this.refPantalla = refPantalla;
        setFila(fila);
        setColumna(columna);
    }

   @Override
public void morir() {
    if (isEstaDestruida()) return;
    setEstaDestruida(true);

    if (refPantalla != null) {
        refPantalla.mostrarArbolMuerto();
    }
    if (getRefJuego() != null) {
        getRefJuego().onArbolMuerto();
    }
}
    @Override
public void subirNivel() {
    super.subirNivel();
    // El árbol solo gana vida al subir de nivel
    setVidaInicial(getVidaInicial() + 100); // +100 por nivel
    setVidaActual(getVidaInicial()); //restaurar a vida máxima
    System.out.println("Árbol de la Vida subió al nivel " + getNivel() + ". Nueva vida: " + getVidaActual());
}

    @Override public boolean objetivoEnRango(Zombies z) { return false; }
    @Override public Zombies buscarObjetivoCercano(java.util.List<Zombies> zs) { return null; }
    @Override public void atacar(Tropa t) { }
}