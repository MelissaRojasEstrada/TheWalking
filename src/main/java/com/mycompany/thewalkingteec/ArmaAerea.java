/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.List;

/**
 *
 * @author melissa
 */
public class ArmaAerea extends Arma {
    private boolean enMovimiento;
    private int velocidadMovimiento; // Casillas por segundo
    private Pantalla ui;
    
    public ArmaAerea() {
        super();
        setCostoCampos(2);
        setNombre("Drone");
        setAlcance(3); // Rango de detección
        setPoderGolpe(10);
        setGolpesPorSegundo(1);
        setTiempoRecarga(600); // Ataca cada 0.6 segundos
        this.enMovimiento = true;
        this.velocidadMovimiento = 2;
        setVidaInicial(50);
        setVidaActual(50);
    }
    
    @Override
    public boolean zombieEnRango(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return false;
        int distancia = Math.abs(getFila() - zombie.getFila())
                  + Math.abs(getColumna() - zombie.getColumna());
        return distancia <= 1;
    }

    public boolean zombieDetectable(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return false;
        double distancia = calcularDistanciaEuclidiana(zombie);
        return distancia <= getAlcance();
    }
    
    /**
     * Mueve el drone hacia el zombie objetivo
     */
    private void moverHaciaZombie(Zombies zombie) {
        if (zombie == null) return; //no zombie, no se hace nada 

        
        //Se guarda en donde está el zombie y la arma
        int filaObjetivo = zombie.getFila();
        int colObjetivo  = zombie.getColumna();
        int f0 = getFila();
        int c0 = getColumna();

        if (this.getFila()    < filaObjetivo){ //si esta arriba del zombie
            this.setFila(this.getFila() + 1); //baja una casilla
        }
        else if (this.getFila() > filaObjetivo){ //si está abajo 
            this.setFila(this.getFila() - 1); // sube una casilla 
        }

        if (this.getColumna() < colObjetivo) { //izquierda del zombie 
            this.setColumna(this.getColumna() + 1); //mueve derecha
        }
        else if (this.getColumna() > colObjetivo){
            this.setColumna(this.getColumna() - 1);
        }

        System.out.println(getNombre() + " se movió a [" + getFila() + "," + getColumna() + "]");

        // actualiza la matriz
        if (getRefJuego() != null) {
            getRefJuego().reubicarDefensa(this, f0, c0, getFila(), getColumna());
        }

        // actualiza el label en la pantalla o la IU 
        if (getRefPantalla() != null) {
            getRefPantalla().actualizarPosicionTropa(this);
        }
    }
    
    @Override
    public void atacarZombie(Zombies zombie) {

        if (zombie == null || zombie.getVidaActual() <= 0) return;
        
        if (!zombieEnRango(zombie) && zombieDetectable(zombie)) { //está fuera de rango pero detectable?
            moverHaciaZombie(zombie); //busquelo
            return;
        }
        if (zombieEnRango(zombie)) { //está en rango?
            int dano = calcularDano();               
            int vidaRestante = zombie.recibirAtaque(dano);
            registrarAtaque(zombie, dano);
            System.out.println(getNombre() + " atacó desde el aire a " + zombie.getNombre()
                + " causando " + dano + " de daño. Vida restante: " + vidaRestante);
        }
    }

    @Override
    public void run() {
        // Mientras el arma esté viva
        while (getVidaActual() > 0 && !isEstaDestruida()) {
            //pausar si hay pausa
            Juego j = getRefJuego();
            if (j != null) j.esperaSiPausado();
            //busq al zombie más cercano 
            Zombies objetivo = buscarZombieCercano();
            
            if (objetivo != null && objetivo.getVidaActual() > 0) {
                // Mover primero si está lejos 
            if (!zombieEnRango(objetivo) && zombieDetectable(objetivo)) {
                moverHaciaZombie(objetivo);
            }
            // Atacar si está en rango y ya toca disparar
            if (zombieEnRango(objetivo) && puedeDispararAhora()) {
                atacarZombie(objetivo);
                registrarDisparo();
            }
        }
        try {
                //a más velocidad, menos espera
                int delay = Math.max(60, 220 - (velocidadMovimiento * 20));
                Thread.sleep(delay);
            } catch (InterruptedException e) { break; }
        }
    }
    
    @Override
    public void subirNivel() {
        super.subirNivel();          
        // +10 de vida por nivel
        setVidaInicial(getVidaInicial() + 10);
        setVidaActual(Math.min(getVidaActual() + 10, getVidaInicial()));
        // +2 de daño por nivel
        setPoderGolpe(getPoderGolpe() + 2);
        // +1 de alcance por nivel
        setAlcance(getAlcance() + 1);
        velocidadMovimiento++; 
        System.out.println(getNombre() + " subió al nivel " + getNivel());
    }
    
    // Getters y Setters
    public boolean isEnMovimiento() {
        return enMovimiento;
    }

    public void setEnMovimiento(boolean enMovimiento) {
        this.enMovimiento = enMovimiento;
    }

    public int getVelocidadMovimiento() {
        return velocidadMovimiento;
    }

    public void setVelocidadMovimiento(int velocidadMovimiento) {
        this.velocidadMovimiento = velocidadMovimiento;
    }
}