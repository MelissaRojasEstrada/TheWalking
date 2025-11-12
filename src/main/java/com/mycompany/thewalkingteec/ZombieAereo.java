/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
public class ZombieAereo extends Zombies{

    //CONSTRUCTOR
    public ZombieAereo(int fila, int columna, int nivel) {
        super("Zombie Aéreo", fila, columna, nivel, 30, 20, 2, 1);
    }

    
    @Override
    public boolean puedeAtacar(Defensa objetivoActual) {
        //tiene que ser defensa aerea y estar en contacto
        if(objetivoActual instanceof ArmaAerea){
            return this.getFila() == objetivoActual.getFila() && this.getColumna() == objetivoActual.getColumna();
        }
        return false; //si no se cumplen las condiciones
    }

    
}
