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
public abstract class Defensa extends Tropa {
    private boolean estaDestruida; //si está dañada o no 
    private int costoCampos;
    
    public Defensa() {
        this.estaDestruida = false;
    } 

    
        /**
     * Verifica si un objetivo está dentro del rango de ataque
     * @param objetivo - El objetivo que queremos verificar si está en rango
     * @return true si el objetivo está en rango, false si no
     * Cada tipo de defensa implementará esto diferente
     */
    public abstract boolean objetivoEnRango(Zombies objetivo);
    
    public abstract Zombies buscarObjetivoCercano(List<Zombies> zombies);
    
    /**
     * Ejecuta el ciclo de defensa: buscar objetivo y atacar
     * @param zombies Lista de zombies a defender contra
     * Verifica q la defensa no esté destruida
     * Busca el zombie más cercano
     * Si lo encuentra y está en rango, lo ataca defendiendo
     */
    public void defender(List<Zombies> zombies) {
        if (!estaDestruida && getVidaInicial() > 0) {
            Zombies objetivo = buscarObjetivoCercano(zombies);
            if (objetivo != null && objetivoEnRango(objetivo)) {
                atacar(objetivo);
            }
        }
    }
    @Override
    public void morir() {
        super.morir();
        this.estaDestruida = true;
    }
    @Override
    public synchronized int recibirAtaque(int dano) {
        int restante = super.recibirAtaque(dano); // ya descuenta vidaActual y llama morir()
        if (restante <= 0 && !isEstaDestruida()) {
            setEstaDestruida(true);
        }
        return restante;
    }   
    
    // Getters y Setters
    public int getCostoCampos() {
        return costoCampos;
    }

    public void setCostoCampos(int costoCampos) {
        this.costoCampos = costoCampos;
    }

    public boolean isEstaDestruida() {
        return estaDestruida;
    }

    public void setEstaDestruida(boolean estaDestruida) {
        this.estaDestruida = estaDestruida;
    } 
}

