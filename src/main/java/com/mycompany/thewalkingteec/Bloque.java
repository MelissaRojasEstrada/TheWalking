/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.List;

public class Bloque extends Defensa {
    private String tipoMaterial; // madera, metal
    private int resistenciaExtra; // resistencia adicional del material
    
    public Bloque() {
        super();
        // Configurar propiedades específicas de bloque
        setCostoCampos(1);
        setNombre("Bloque");
        setVidaInicial(250); // Bloques tienen mucha vida
        setVidaActual(250);
        setPoderGolpe(0); // No atacan
        setGolpesPorSegundo(0);
        setAlcance(0);
        this.tipoMaterial = "madera";
        this.resistenciaExtra = 10;
    }
    
    /**
     * tipo de material específico
     */
    public Bloque(String tipoMaterial, int resistenciaExtra) {
        this();
        this.tipoMaterial = tipoMaterial;
        this.resistenciaExtra = resistenciaExtra;
        setNombre("Bloque de " + tipoMaterial);
        
        // Ajustar vida según material
        switch(tipoMaterial.toLowerCase()) {
            case "madera":
                setVidaInicial(500);
                break;
            case "piedra":
                setVidaInicial(800);
                break;
            case "metal":
                setVidaInicial(1200);
                break;
            case "concreto":
                setVidaInicial(1500);
                break;
            default:
                setVidaInicial(500);
        }
    }
    
    @Override
    public void atacar(Tropa objetivoAAtacar) {
        // NO atacan, solo defienden
    }
    
    @Override
    public boolean objetivoEnRango(Zombies objetivo) {
        //no tienen rango de ataque
        return false;
    }
    
    @Override
    public Zombies buscarObjetivoCercano(List<Zombies> zombies) {
        // Los bloques no buscan objetivos, no atacan
        return null;
    }
    
    @Override
    public void run() {
        // Bloque no hace nada en su thread
        // Solo barrera
        while(getVidaInicial() > 0 && !isEstaDestruida()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        System.out.println(getNombre() + " ha sido destruido en posición [" + 
                          getFila() + "," + getColumna() + "]");
    }
    
    @Override
    public int recibirAtaque(int golpesPorSegundo) {
        // Los bloques tienen resistencia extra
        int danoReducido = Math.max(1, golpesPorSegundo - resistenciaExtra);
        
        System.out.println(getNombre() + " recibió " + golpesPorSegundo + 
                          " de daño, reducido a " + danoReducido + 
                          " (resistencia: " + resistenciaExtra + ")");
        
        return super.recibirAtaque(danoReducido);
    }
    
    /**
     * Mejora el bloque aumentando su resistencia
     */
    public void mejorarResistencia(int incremento) {
        this.resistenciaExtra += incremento;
        System.out.println(getNombre() + " mejoró su resistencia a " + resistenciaExtra);
    }
    
    @Override
    public void subirNivel() {
        super.subirNivel();
        // Al subir nivel, aumenta vida y resistencia
        setVidaInicial(getVidaInicial() + 100);
        resistenciaExtra += 5;
        
        System.out.println(getNombre() + " subió al nivel " + getNivel() + 
                          ". Nueva vida: " + getVidaInicial() + 
                          ", resistencia: " + resistenciaExtra);
    }
    
    // Getters y Setters
    public String getTipoMaterial() {
        return tipoMaterial;
    }
    
    public void setTipoMaterial(String tipoMaterial) {
        this.tipoMaterial = tipoMaterial;
    }
    
    public int getResistenciaExtra() {
        return resistenciaExtra;
    }
    
    public void setResistenciaExtra(int resistenciaExtra) {
        this.resistenciaExtra = resistenciaExtra;
    }
}
    
    
    
    
    
   
