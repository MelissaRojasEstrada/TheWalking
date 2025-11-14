/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
public class ZombieMedianoAlcance extends Zombies{

    //CONSTRUCTOR
    public ZombieMedianoAlcance(int fila, int columna, int nivel) {
        super("Zombie Francotirador", fila, columna, nivel, 40, 15, 1, 3 + (nivel / 2));
    }

    @Override
    public boolean puedeAtacar(Defensa objetivoActual) {

        int distanciax = this.getFila() - objetivoActual.getFila();
        int distanciay = this.getColumna() - objetivoActual.getColumna();
        return Math.sqrt(distanciax * distanciax + distanciay * distanciay) <= this.getAlcance();
        
    }
    
}