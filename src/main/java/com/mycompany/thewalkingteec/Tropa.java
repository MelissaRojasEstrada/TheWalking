/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author melissa
 */
public abstract class Tropa extends Thread{
    
    private String nombre;
    private ImageIcon apariencia;
    int vidaInicial;
    private int vidaActual; //para guardar la vida con la que quedo en los registros
    private int poderGolpe;
    private int golpesPorSegundo;
    private int nivel = 0;
    private int alcance;
    private int nivelDeAparicion;
    private int fila, columna; //posicion
    private transient Juego refJuego;
    private transient Pantalla refPantalla;
    private transient javax.swing.JLabel refLabel;
    protected volatile int myBattleId = -1;
    protected volatile boolean detenido = false;
    
    //para los registros
    private List<RegistroAtaques> ataquesRealizados = new ArrayList<>();
    private List<RegistroAtaques> ataquesRecibidos = new ArrayList<>();
    
    //registra este ataque realizado por una tropa hacia un objetivo 
    public void registrarAtaque(Tropa objetivo, int dano) {
        
        //this es quién atacó, objetivo a quién se atacó y así  
        RegistroAtaques registro = new RegistroAtaques(this, objetivo, dano, objetivo.getVidaActual(), objetivo.getVidaActual() - dano);
        this.ataquesRealizados.add(registro);
        objetivo.ataquesRecibidos.add(registro);
    }
    
    //Método abstracto que define el comportamiento de ataque de cada tropa
    public abstract void atacar(Tropa objetivoAAtacar);
    
    //Método sincronizado para evitar problemas de concurrencia cuando muchos threads atacan a la misma vez y así 
    public synchronized int recibirAtaque(int dano){
    if (getVidaActual() <= 0) return 0;

    int vidaAntes = getVidaActual();
    int vidaDespues = Math.max(0, vidaAntes - Math.max(0, dano));
    setVidaActual(vidaDespues);

    if (vidaDespues <= 0) {
        morir();
    }
    return vidaDespues;
    }
    
    public void subirNivel(){
        this.nivel += 1; 
    }
    
   public void morir(){
    // dejar vida en 0
    setVidaActual(0);

        // detiene el thread
        try { interrupt(); } catch(Exception ignored){}

        // Remueve el label
        Pantalla p = getRefPantalla();
        javax.swing.JLabel lbl = getRefLabel();
        if (p != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                p.mostrarRIP(this); //no funciona correctamente este apartado 
                p.eliminarLabelDe(this); 
            });
        }

        // quitar de listas y matriz
        Juego j = getRefJuego();
        if (j != null) {
            j.eliminarTropa(this);
        }
    }
    
    
    //GETTERS Y SETTERS

    public String getNombre() {
        return nombre;
    }

    public ImageIcon getApariencia() {
        return apariencia;
    }

    public int getNivel() {
        return nivel;
    }

    public int getAlcance() {
        return alcance;
    }

    public int getNivelDeAparicion() {
        return nivelDeAparicion;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public int getVidaInicial() {
        return vidaInicial;
    }

    public int getVidaActual() {
        return vidaActual;
    }

    public int getPoderGolpe() {
        return poderGolpe;
    }

    public int getGolpesPorSegundo() {
        return golpesPorSegundo;
    }

    public List<RegistroAtaques> getAtaquesRealizados() {
        return ataquesRealizados;
    }

    public List<RegistroAtaques> getAtaquesRecibidos() {
        return ataquesRecibidos;
    }
    
    public void setBattleId(int id) { 
        this.myBattleId = id; 
    }
    public void solicitarDetener() { 
        this.detenido = true; this.interrupt();
    }
    
    
    public void setRefJuego(Juego j){ 
        this.refJuego = j; 
    }
    public void setRefPantalla(Pantalla p){
        this.refPantalla = p;
    }
    public void setRefLabel(javax.swing.JLabel lbl){
        this.refLabel = lbl; 
    }
    public javax.swing.JLabel getRefLabel(){ 
        return refLabel; 
    }
    public Pantalla getRefPantalla(){
        return refPantalla; 
    }
    public Juego getRefJuego(){ 
        return refJuego; 
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApariencia(ImageIcon apariencia) {
        this.apariencia = apariencia;
    }


    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public void setAlcance(int alcance) {
        this.alcance = alcance;
    }

    public void setNivelDeAparicion(int nivelDeAparicion) {
        this.nivelDeAparicion = nivelDeAparicion;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public static int getMIN_PRIORITY() {
        return MIN_PRIORITY;
    }

    public static int getNORM_PRIORITY() {
        return NORM_PRIORITY;
    }

    public static int getMAX_PRIORITY() {
        return MAX_PRIORITY;
    }
    

    public void setVidaInicial(int vidaInicial) {
        this.vidaInicial = vidaInicial;
    }

    public void setVidaActual(int vidaActual) {
        this.vidaActual = vidaActual;
    }

    public void setPoderGolpe(int poderGolpe) {
        this.poderGolpe = poderGolpe;
    }

    public void setGolpesPorSegundo(int golpesPorSegundo) {
        this.golpesPorSegundo = golpesPorSegundo;
    }

    public void setAtaquesRealizados(List<RegistroAtaques> ataquesRealizados) {
        this.ataquesRealizados = ataquesRealizados;
    }

    public void setAtaquesRecibidos(List<RegistroAtaques> ataquesRecibidos) {
        this.ataquesRecibidos = ataquesRecibidos;
    }
    

}

