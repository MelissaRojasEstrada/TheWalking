/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author melissa
 */
public abstract class Arma extends Defensa {
    private int municiones; // municiones disponibles (-1 para infinito)
    private int tiempoRecarga; // tiempo entre ataques en milisegundos
    private long ultimoAtaque; // del último ataque
    private boolean puedeAtacar; // si puede o no atacar
    private boolean esMultiple; // para armas de ataque múltiple
    private volatile Zombies objetivoActual; // la variable puede ser modificada
    private List<Zombies> listaZombies; 
    private volatile boolean cancelado_ = false;
    private volatile int battleIdSnapshot_ = -1; //en -1 pq indica q el zombie no ha participado en la batalla 
    
    public Arma() {
        super();
        this.municiones = -1; // infinito
        this.tiempoRecarga = 1000; // 1s
        this.ultimoAtaque = 0;
        this.puedeAtacar = true;
        this.esMultiple = false;
        this.listaZombies = new ArrayList<>();
    }
    
    /**
     * Implementación del método abstracto de Defensa
     */
    @Override
    public boolean objetivoEnRango(Zombies objetivo) {
        if(objetivo == null) {
            return false;
        }
        return zombieEnRango(objetivo);
    }
     
    public boolean puedeDispararAhora() {
        /**
         * Verifica si el arma puede atacar según el tiempo de recarga
         */
        long tiempoActual = System.currentTimeMillis();
        return puedeAtacar && (tiempoActual - ultimoAtaque >= tiempoRecarga) 
               && (municiones != 0) && !isEstaDestruida();
    }
    
    protected void registrarDisparo() {
        /**
         * Actualiza el tiempo del último ataque
         */
        this.ultimoAtaque = System.currentTimeMillis();
        if (municiones > 0) {
            municiones--;
        }
    }
    
    protected int calcularDano() {
        /**
         * Calcula el daño que hace esta arma
         */
        return getPoderGolpe();
    }
    
    public Zombies buscarZombieCercano() {
        /**
         * Busca el zombie más cercano
         */
        Zombies objetivoCercano = null;
        int menorDistancia = Integer.MAX_VALUE;
        
        if(listaZombies == null || listaZombies.isEmpty()) {
            return null;
        }
        
        for (Zombies zombie : listaZombies) {
            if (zombie != null && zombie.isAlive() && zombie.getVidaActual() > 0) { 
                int distancia = calcularDistanciaManhattan(zombie);
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    objetivoCercano = zombie;
                }
            }
        }
        
        return objetivoCercano;
    }
    
    /**
     * Busca el zombie + cercano
     */
    @Override
    public Zombies buscarObjetivoCercano(List<Zombies> zombies) {
        // cambia temporalmente la lista proporcionada
        List<Zombies> listaOriginal = this.listaZombies;
        this.listaZombies = zombies; // usa la lista pasada como parámetro
        Zombies resultado = buscarZombieCercano();
        this.listaZombies = listaOriginal; // restaura la original
        return resultado;
    }
    
    //calcula la distancia que hay entre casillas 
    private int calcularDistanciaManhattan(Zombies zombie) {
        int x = Math.abs(this.getFila() - zombie.getFila());
        int y = Math.abs(this.getColumna() - zombie.getColumna());
        return x + y;     
    }
    
    //calcula la distacia real o la diagonal
    protected double calcularDistanciaEuclidiana(Zombies objetivo) {
        int deltaFila = this.getFila() - objetivo.getFila();
        int deltaColumna = this.getColumna() - objetivo.getColumna();
        return Math.sqrt(deltaFila * deltaFila + deltaColumna * deltaColumna);
    }
    
    /**
     * verifica si un zombie está en rango
     */
    public abstract boolean zombieEnRango(Zombies zombie);
    
    @Override
    public void atacar(Tropa objetivoAAtacar) {
        if(!(objetivoAAtacar instanceof Zombies)) {
            return;
        }
        
        Zombies zombie = (Zombies)objetivoAAtacar;
        atacarZombie(zombie);
    }
    
    /**
     * Cada arma implementa según su tipo de ataque
     */
    public abstract void atacarZombie(Zombies zombie);
    @Override
    public void run() {
        while (getVidaActual() > 0 && !isEstaDestruida()) {
            
            //Pausa el juego si se presiona el botón 
            Juego j = getRefJuego();
            if (j != null) j.esperaSiPausado();
            
            //busca objetivo si no hay o si muri+o
            if (objetivoActual == null || objetivoActual.getVidaActual() <= 0 || !objetivoActual.isAlive()) { 
                objetivoActual = buscarZombieCercano();
            }
            //si hay objetivo atacar si está en rango
            if (objetivoActual != null) {
            if (zombieEnRango(objetivoActual)) {
                if (puedeDispararAhora()) {
                    atacarZombie(objetivoActual);
                    registrarDisparo();
                }
            }
        } //dormir 100m antes de la sig iteración para q el cpu descanse alguito
        try { Thread.sleep(100); } catch (InterruptedException e) { break; }
    }
}
    
    /**
     * Calcula la distancia entre esta arma y un objetivo
     */
    protected double calcularDistancia(Tropa objetivo) {
        int deltaFila = this.getFila() - objetivo.getFila();
        int deltaColumna = this.getColumna() - objetivo.getColumna();
        return Math.sqrt(deltaFila * deltaFila + deltaColumna * deltaColumna);
    }

    // Getters y Setters
    public int getMuniciones() {
        return municiones;
    }

    public void setMuniciones(int municiones) {
        this.municiones = municiones;
    }

    public int getTiempoRecarga() {
        return tiempoRecarga;
    }

    public void setTiempoRecarga(int tiempoRecarga) {
        this.tiempoRecarga = tiempoRecarga;
    }

    public long getUltimoAtaque() {
        return ultimoAtaque;
    }

    public void setUltimoAtaque(long ultimoAtaque) {
        this.ultimoAtaque = ultimoAtaque;
    }

    public boolean isPuedeAtacar() {
        return puedeAtacar;
    }

    public void setPuedeAtacar(boolean puedeAtacar) {
        this.puedeAtacar = puedeAtacar;
    }

    public boolean isEsMultiple() {
        return esMultiple;
    }

    public void setEsMultiple(boolean esMultiple) {
        this.esMultiple = esMultiple;
    }

    public List<Zombies> getListaZombies() {
        return listaZombies;
    }

    public void setListaZombies(List<Zombies> listaZombies) {
        this.listaZombies = listaZombies;
    }
}

    
    
    

