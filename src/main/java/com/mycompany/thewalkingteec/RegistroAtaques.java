/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
import java.io.Serializable;

/**
 *
 * @author Alina
 */
public class RegistroAtaques implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private String nombreAtacante;
    private String tipoAtacante;
    private int filaAtacante;
    private int columnaAtacante;

    private String nombreAtacado;
    private String tipoAtacado;
    private int filaObjetivo;
    private int columnaObjetivo;

    private int danoAplicado;
    private int vidaAtacadoAntes;
    private int vidaAtacadoDespues;
    
    //CONSTRUCTOR

    public RegistroAtaques(Tropa atacante, Tropa atacado, int dano, int vidaAntes, int vidaDespues) {
        this.nombreAtacante = atacante.getNombre();
        this.tipoAtacante = atacante.getClass().getSimpleName();
        this.filaAtacante = atacante.getFila();
        this.columnaAtacante = atacante.getColumna();
        
        this.nombreAtacado = atacado.getNombre();
        this.tipoAtacado = atacado.getClass().getSimpleName();
        this.filaObjetivo = atacado.getFila();
        this.columnaObjetivo = atacado.getColumna();
        
        this.danoAplicado = dano;
        this.vidaAtacadoAntes = vidaAntes;
        this.vidaAtacadoDespues = vidaDespues;
    }
    
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(nombreAtacante).append(" (").append(tipoAtacante).append(") ");
        sb.append("en [").append(filaAtacante).append(",").append(columnaAtacante).append("] ");
        sb.append("atacó a ");
        sb.append(nombreAtacado).append(" (").append(tipoAtacado).append(") ");
        sb.append("en [").append(filaObjetivo).append(",").append(columnaObjetivo).append("] ");
        sb.append("causando ").append(danoAplicado).append(" de daño. ");
        sb.append("Vida: ").append(vidaAtacadoAntes).append(" → ").append(vidaAtacadoDespues);

        return sb.toString();
    }

    
    //GETTERS Y SETTERS
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getNombreAtacante() {
        return nombreAtacante;
    }

    public String getTipoAtacante() {
        return tipoAtacante;
    }

    public int getFilaAtacante() {
        return filaAtacante;
    }

    public int getColumnaAtacante() {
        return columnaAtacante;
    }

    public String getNombreAtacado() {
        return nombreAtacado;
    }

    public String getTipoAtacado() {
        return tipoAtacado;
    }

    public int getFilaObjetivo() {
        return filaObjetivo;
    }

    public int getColumnaObjetivo() {
        return columnaObjetivo;
    }

    public int getDanoAplicado() {
        return danoAplicado;
    }

    public int getVidaAtacadoAntes() {
        return vidaAtacadoAntes;
    }

    public int getVidaAtacadoDespues() {
        return vidaAtacadoDespues;
    }

    public void setNombreAtacante(String nombreAtacante) {
        this.nombreAtacante = nombreAtacante;
    }

    public void setTipoAtacante(String tipoAtacante) {
        this.tipoAtacante = tipoAtacante;
    }

    public void setFilaAtacante(int filaAtacante) {
        this.filaAtacante = filaAtacante;
    }

    public void setColumnaAtacante(int columnaAtacante) {
        this.columnaAtacante = columnaAtacante;
    }

    public void setNombreAtacado(String nombreAtacado) {
        this.nombreAtacado = nombreAtacado;
    }

    public void setTipoAtacado(String tipoAtacado) {
        this.tipoAtacado = tipoAtacado;
    }

    public void setFilaObjetivo(int filaObjetivo) {
        this.filaObjetivo = filaObjetivo;
    }

    public void setColumnaObjetivo(int columnaObjetivo) {
        this.columnaObjetivo = columnaObjetivo;
    }

    public void setDanoAplicado(int danoAplicado) {
        this.danoAplicado = danoAplicado;
    }

    public void setVidaAtacadoAntes(int vidaAtacadoAntes) {
        this.vidaAtacadoAntes = vidaAtacadoAntes;
    }

    public void setVidaAtacadoDespues(int vidaAtacadoDespues) {
        this.vidaAtacadoDespues = vidaAtacadoDespues;
    }
    
}
