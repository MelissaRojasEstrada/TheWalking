/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
public class ZombieChoque extends Zombies{

    //CONSTRUCTOR
    public ZombieChoque(int fila, int columna, int nivel) {
        super("Zombie Kamikaze", fila, columna, nivel, 80, 50, 1, 2);
    }

    @Override
    public boolean puedeAtacar(Defensa objetivoActual) {
        //solo puede atacar si se muere
        return this.getVidaInicial() <= 0;
    }
    
}
