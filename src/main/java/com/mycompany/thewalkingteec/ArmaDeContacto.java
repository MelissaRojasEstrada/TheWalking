/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.List;

/**
 * Ataca zombies en contacto
 * @author melissa
 */
public class ArmaDeContacto extends Arma {
    
    public ArmaDeContacto() {
        super();
        setCostoCampos(1);
        setNombre("Arma de Contacto");
        setVidaInicial(5);
        setVidaActual(5);
        setAlcance(1); // Solo ataca zombies adyacentes
        setPoderGolpe(50);
        setGolpesPorSegundo(1);
        setTiempoRecarga(1000); // Ataca cada 0.5 segundos
    }
    
    
    
    
    @Override
     public boolean zombieEnRango(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return false; // ← vidaActual
        int distancia = Math.abs(getFila() - zombie.getFila())
                      + Math.abs(getColumna() - zombie.getColumna());
        return distancia <= getAlcance(); // <=1 adyacente
    }
    
    @Override
     public void atacarZombie(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return;       // ← vidaActual
        if (!zombieEnRango(zombie)) {
            System.out.println(getNombre() + " no puede alcanzar a " + zombie.getNombre());
            return;
        }

        int dano = calcularDano();                       // daño por golpe = poderGolpe (ya corregido en Arma)
        int vidaRestante = zombie.recibirAtaque(dano);
        registrarAtaque(zombie, dano);

        System.out.println(getNombre() + " golpeó a " + zombie.getNombre()
                + " causando " + dano + " de daño. Vida restante: " + vidaRestante);
    }

    @Override
    public void run() {
        System.out.println("Arma [" + getFila() + "][" + getColumna() + "] iniciada");
        while (getVidaActual() > 0 && !isEstaDestruida()) { 
            Juego j = getRefJuego();
            if (j != null) j.esperaSiPausado();// ← vidaActual
            // Buscar objetivo cercano usando la lista que gestiona Pantalla
            Zombies objetivo = buscarZombieCercano();

            if (objetivo != null && objetivo.getVidaActual() > 0) {      // ← vidaActual
                if (zombieEnRango(objetivo) && puedeDispararAhora()) {
                    atacarZombie(objetivo);
                    registrarDisparo(); // respeta tiempoRecarga y munición
                }
            }

            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
        System.out.println("Arma [" + getFila() + "][" + getColumna() + "] destruida");
    }

    @Override
    public void subirNivel() {
        super.subirNivel(); // no debe recalcular stats en Arma
        // Incrementos por nivel (melee: más tanque y más daño)
        setVidaInicial(getVidaInicial() + 10);
        setVidaActual(Math.min(getVidaActual() + 10, getVidaInicial()));
        setPoderGolpe(getPoderGolpe() + 3);
        
        // (Opcional) pegar un poco más rápido: reduce levemente el tiempo de recarga
        // setTiempoRecarga(Math.max(200, getTiempoRecarga() - 25));

        System.out.println(getNombre() + " subió al nivel " + getNivel());
    }
}

