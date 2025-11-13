/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
import java.util.ArrayList;

public class PartidaGuardada {
    
    private static final long serialVersionUID = 1L;

    private String nombreJugador;
    private int numeroPartida; // Para identificar múltiples partidas del mismo jugador
    private int nivelActual;
    private int espaciosEjercitoDisponibles;
    private String fechaGuardado;

    private ArrayList<BitacoraNivel> bitacoras; //bitacoras de cada nivel
    
    //CONSTRUCTOR
    public PartidaGuardada(String nombreJugador, int numeroPartida, int nivelActual, int espaciosEjercito) {
        this.nombreJugador = nombreJugador;
        this.numeroPartida = numeroPartida;
        this.nivelActual = nivelActual;
        this.espaciosEjercitoDisponibles = espaciosEjercito;
        this.bitacoras = new ArrayList<>();
    }
    
    public void agregarBitacora(BitacoraNivel bitacora) {
        this.bitacoras.add(bitacora);
    }
    
    public BitacoraNivel getBitacoraNivel(int numeroNivel) {
        //get la bitacora de un nivel en especifico
        for (BitacoraNivel bitacora : bitacoras) {
            if (bitacora.getNumeroNivel() == numeroNivel) {
                return bitacora;
            }
        }
        return null;
    }
    
    public String getNombreArchivo() {
        //genera el nombre del archivo de bitacora
        return nombreJugador + "_partida" + numeroPartida + ".ser";
    }
    
    public String getInfoResumida() {
        StringBuilder sb = new StringBuilder();
        sb.append("Partida #").append(numeroPartida).append(" - ").append(nombreJugador).append("\n");
        sb.append("Nivel actual: ").append(nivelActual).append("\n");
        sb.append("Espacios disponibles: ").append(espaciosEjercitoDisponibles).append("\n");
        sb.append("Guardado: ").append(fechaGuardado).append("\n");
        sb.append("Niveles completados: ").append(bitacoras.size()).append("\n");

        return sb.toString();
    }
    
    public String generarReporteCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════╗\n");
        sb.append("║    REPORTE COMPLETO DE LA PARTIDA                 ║\n");
        sb.append("╚═══════════════════════════════════════════════════╝\n\n");
        sb.append(getInfoResumida()).append("\n");

        if (bitacoras.isEmpty()) {
            sb.append("No hay bitácoras registradas aún.\n");
        } else {
            for (BitacoraNivel bitacora : bitacoras) {
                sb.append("\n").append(bitacora.generarResumen()).append("\n");
            }
        }

        return sb.toString();
    }
    
    //GETTERS Y SETTERS

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public int getNumeroPartida() {
        return numeroPartida;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public int getEspaciosEjercitoDisponibles() {
        return espaciosEjercitoDisponibles;
    }

    public String getFechaGuardado() {
        return fechaGuardado;
    }

    public ArrayList<BitacoraNivel> getBitacoras() {
        return bitacoras;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public void setNumeroPartida(int numeroPartida) {
        this.numeroPartida = numeroPartida;
    }

    public void setNivelActual(int nivelActual) {
        this.nivelActual = nivelActual;
    }

    public void setEspaciosEjercitoDisponibles(int espaciosEjercitoDisponibles) {
        this.espaciosEjercitoDisponibles = espaciosEjercitoDisponibles;
    }

    public void setFechaGuardado(String fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }

    public void setBitacoras(ArrayList<BitacoraNivel> bitacoras) {
        this.bitacoras = bitacoras;
    }
    
    
    
    
}
