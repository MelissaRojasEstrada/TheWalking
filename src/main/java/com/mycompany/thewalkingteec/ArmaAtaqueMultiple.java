/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;


import java.util.ArrayList;
import java.util.List;

/**
 * Esta arma lanza varios proyectiles simultáneos 
 * atacando múltiples zombies
 * @author melissa
 */
public class ArmaAtaqueMultiple extends Arma {
    private int numeroProyectiles; // Cantidad de proyectiles por disparo
    private String tipoProyectil;
    
    public ArmaAtaqueMultiple() {
        super();
        setCostoCampos(3);
        setNombre("Arma de Ataque Múltiple");
        setAlcance(4); // Alcance medio
        setPoderGolpe(20); // Menor daño individual que armas simples
        setGolpesPorSegundo(2);
        setTiempoRecarga(1200); // 1.2 segundos entre ráfagas
        this.numeroProyectiles = 3; // Dispara 3 proyectiles por ráfaga
        this.tipoProyectil = "Proyectil";
        setEsMultiple(true);
        setVidaInicial(50);
        setVidaActual(50);
    }
    
    /**
     * Constructor 
     * @param nombre
     * @param numeroProyectiles
     * @param tipoProyectil
     */
    public ArmaAtaqueMultiple(String nombre, int numeroProyectiles, String tipoProyectil) {
        this();
        setNombre(nombre);
        this.numeroProyectiles = numeroProyectiles;
        this.tipoProyectil = tipoProyectil;
    }
    
    @Override
    public boolean zombieEnRango(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return false;   // ← vidaActual
        double distancia = calcularDistanciaEuclidiana(zombie);
        return distancia <= getAlcance();
    }

    /**
     * Busca los n zombies más cercanos para atacar simultáneamente
     */
    private List<Zombies> buscarObjetivosMultiples() {
        List<Zombies> objetivos = new ArrayList<>();
        if (getListaZombies() == null || getListaZombies().isEmpty()) return objetivos;

        // Filtrar por vivos y en rango
        List<Zombies> zombiesEnRango = new ArrayList<>();
        for (Zombies zombie : getListaZombies()) {
            if (zombie != null && zombie.getVidaActual() > 0 && zombieEnRango(zombie)) {  // ← vidaActual
                zombiesEnRango.add(zombie);
            }
        }
        if (zombiesEnRango.isEmpty()) return objetivos;

        // Ordenar por distancia (más cercano primero)
        zombiesEnRango.sort((z1, z2) -> {
            double d1 = calcularDistanciaEuclidiana(z1);
            double d2 = calcularDistanciaEuclidiana(z2);
            return Double.compare(d1, d2);
        });

        // Tomar hasta N zombies (número de proyectiles)
        int cantidad = Math.min(numeroProyectiles, zombiesEnRango.size());
        for (int i = 0; i < cantidad; i++) {
            objetivos.add(zombiesEnRango.get(i));
        }
        return objetivos;
    }

    
    @Override
    public void atacarZombie(Zombies zombie) {
        // Mantén compatibilidad, pero usa el múltiple internamente
        List<Zombies> objetivos = new ArrayList<>();
        if (zombie != null && zombie.getVidaActual() > 0 && zombieEnRango(zombie)) {  // ← vidaActual
            objetivos.add(zombie);
        }
        atacarMultiple(objetivos);
    }

    
    /**
     * Ataca a múltiples zombies simultáneamente
     * @param objetivos o zoombies
     */
    public void atacarMultiple(List<Zombies> objetivos) {
        if (objetivos == null || objetivos.isEmpty()) return;

        // daño por proyectil (calcularDano() debe devolver solo poderGolpe)
        int dano = calcularDano();

        System.out.println(getNombre() + " lanza una ráfaga de " + objetivos.size() + " " + tipoProyectil + "(s):");

        int impactosExitosos = 0;
        for (Zombies zombie : objetivos) {
            if (zombie != null && zombie.getVidaActual() > 0 && zombieEnRango(zombie)) {   // ← vidaActual
                int vidaRestante = zombie.recibirAtaque(dano);
                registrarAtaque(zombie, dano);
                impactosExitosos++;
                System.out.println("  - " + tipoProyectil + " #" + impactosExitosos +
                        " impactó a " + zombie.getNombre() +
                        " causando " + dano + " de daño. Vida restante: " + vidaRestante);
            }
        }

        if (impactosExitosos == 0) {
            System.out.println("  - Ningún proyectil alcanzó su objetivo");
        }
    }

    @Override
    public void run() {
        while (getVidaActual() > 0 && !isEstaDestruida()) {           
            List<Zombies> objetivos = buscarObjetivosMultiples();
            if (!objetivos.isEmpty() && puedeDispararAhora()) {
                atacarMultiple(objetivos);
                registrarDisparo(); 
            }
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }

    @Override
    public void subirNivel() {
        super.subirNivel(); // sube el nivel (sin tocar stats en Arma)
        setVidaInicial(getVidaInicial() + 10);
        setVidaActual(Math.min(getVidaActual() + 10, getVidaInicial()));
        setPoderGolpe(getPoderGolpe() + 2);   // daño por proyectil
        setAlcance(getAlcance() + 1);
        numeroProyectiles++;              

        System.out.println(getNombre() + " subió al nivel " + getNivel() +
                ". Proyectiles por ráfaga: " + numeroProyectiles +
                ", alcance: " + getAlcance());
    }

    @Override
    protected int calcularDano() {
        // Daño por proyectil (la cadencia la maneja tiempoRecarga)
        return getPoderGolpe();
    }
    
    /**
     * Mejora el arma aumentando el número de proyectiles
     */
    public void mejorarCapacidad(int proyectilesExtra) {
        this.numeroProyectiles += proyectilesExtra;
        System.out.println(getNombre() + " mejoró su capacidad. Nuevos proyectiles: " + 
                          numeroProyectiles);
    }
    
    /**
     * Muestra estadísticas del arma
     */
    public void mostrarEstadisticas() {
        System.out.println("=== Estadísticas de " + getNombre() + " ===");
        System.out.println("Nivel: " + getNivel());
        System.out.println("Proyectiles por ráfaga: " + numeroProyectiles);
        System.out.println("Daño por proyectil: " + getPoderGolpe());
        System.out.println("Daño total por ráfaga: " + (getPoderGolpe() * numeroProyectiles));
        System.out.println("Alcance: " + getAlcance());
        System.out.println("Tiempo de recarga: " + getTiempoRecarga() + "ms");
        System.out.println("Ataques realizados: " + getAtaquesRealizados().size());
    }
    // Getters y Setters
    public int getNumeroProyectiles() {
        return numeroProyectiles;
    }

    public void setNumeroProyectiles(int numeroProyectiles) {
        this.numeroProyectiles = numeroProyectiles;
    }
    
    public String getTipoProyectil() {
        return tipoProyectil;
    }

    public void setTipoProyectil(String tipoProyectil) {
        this.tipoProyectil = tipoProyectil;
    }
}