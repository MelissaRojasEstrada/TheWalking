/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
public class ZombieContacto extends Zombies{

    //CONSTRUCTOR
    public ZombieContacto(int fila, int columna, int nivel) {
        super("Zombie Contacto", fila, columna, nivel, 50, 10, 1, 1);
    }
    
    
    @Override
    public boolean puedeAtacar(Defensa objetivoActual) {
        // Solo puede atacar si está en la misma casilla
        return this.getFila() == objetivoActual.getFila() && this.getColumna() == objetivoActual.getColumna();
    }
    
}