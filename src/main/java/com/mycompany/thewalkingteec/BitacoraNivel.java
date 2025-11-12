/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.util.ArrayList;

/**
 *
 * @author Alina
 */
public class BitacoraNivel {
    
    private static final long serialVersionUID = 1L;
    
    private int numeroNivel;
    private String nombreJugador;
    private boolean victoria;
    
    private ArrayList<RegistroAtaques> registrosAtaques;
    
    private int totalDefensasColocadas;
    private int totalZombiesGenerados;
    private int totalDefensasDestruidas;
    private int totalZombiesEliminados;
    private int totalAtaquesRealizados;
    
    //CONSTRUCTOR

    public BitacoraNivel(int numeroNivel, String nombreJugador) {
        this.numeroNivel = numeroNivel;
        this.nombreJugador = nombreJugador;
        this.registrosAtaques = new ArrayList<>();
        
        this.victoria = false;

        this.totalDefensasColocadas = 0;
        this.totalZombiesGenerados = 0;
        this.totalDefensasDestruidas = 0;
        this.totalZombiesEliminados = 0;
        this.totalAtaquesRealizados = 0;
    }
    
    public void agregarRegistro(RegistroAtaques registro) {
        registrosAtaques.add(registro);
        totalAtaquesRealizados++;
    }
    
    public void finalizarNivel(boolean ganado, int defensasColocadas, int zombiesGenerados, int defensasDestruidas, int zombiesEliminados) {
        //registra datos generales al finalizar nivel
        
        this.victoria = ganado;
        this.totalDefensasColocadas = defensasColocadas;
        this.totalZombiesGenerados = zombiesGenerados;
        this.totalDefensasDestruidas = defensasDestruidas;
        this.totalZombiesEliminados = zombiesEliminados;
    }
    
    public ArrayList<RegistroAtaques> getAtaquesPor(String nombreAtacante) {
        //registra ataques de un solo atacante
        
        ArrayList<RegistroAtaques> ataques = new ArrayList<>();
        for (RegistroAtaques registro : registrosAtaques) {
            if (registro.getNombreAtacante().equals(nombreAtacante)) {
                ataques.add(registro);
            }
        }
        return ataques;
    }
    
    public ArrayList<RegistroAtaques> getAtaquesContra(String nombreAtacado) {
        //registra los ataques recibidos por atacado
        
        ArrayList<RegistroAtaques> ataques = new ArrayList<>();
        for (RegistroAtaques registro : registrosAtaques) {
            if (registro.getNombreAtacado().equals(nombreAtacado)) {
                ataques.add(registro);
            }
        }
        return ataques;
    }
    
    public String generarResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("           BITÁCORA DEL NIVEL ").append(numeroNivel).append("\n");
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("Jugador: ").append(nombreJugador).append("\n");
        sb.append("Resultado: ").append(victoria ? "VICTORIA" : "DERROTA").append("\n");
        sb.append("───────────────────────────────────────────────────\n");
        sb.append("ESTADÍSTICAS:\n");
        sb.append("  • Defensas colocadas: ").append(totalDefensasColocadas).append("\n");
        sb.append("  • Zombies generados: ").append(totalZombiesGenerados).append("\n");
        sb.append("  • Defensas destruidas: ").append(totalDefensasDestruidas).append("\n");
        sb.append("  • Zombies eliminados: ").append(totalZombiesEliminados).append("\n");
        sb.append("  • Total de ataques: ").append(totalAtaquesRealizados).append("\n");
        sb.append("═══════════════════════════════════════════════════\n");

        return sb.toString();
    }
    
    public String generarReporteCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(generarResumen());
        sb.append("\nDETALLE DE ATAQUES:\n");
        sb.append("───────────────────────────────────────────────────\n");

        if (registrosAtaques.isEmpty()) {
            sb.append("No se registraron ataques en este nivel.\n");
        } else {
            for (int i = 0; i < registrosAtaques.size(); i++) {
                sb.append(i + 1).append(". ").append(registrosAtaques.get(i).toString()).append("\n");
            }
        }

        sb.append("═══════════════════════════════════════════════════\n");
        return sb.toString();
    }
    
    //GETTERS Y SETTERS

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public boolean isVictoria() {
        return victoria;
    }

    public ArrayList<RegistroAtaques> getRegistrosAtaques() {
        return registrosAtaques;
    }

    public int getTotalDefensasColocadas() {
        return totalDefensasColocadas;
    }

    public int getTotalZombiesGenerados() {
        return totalZombiesGenerados;
    }

    public int getTotalDefensasDestruidas() {
        return totalDefensasDestruidas;
    }

    public int getTotalZombiesEliminados() {
        return totalZombiesEliminados;
    }

    public int getTotalAtaquesRealizados() {
        return totalAtaquesRealizados;
    }

    public void setNumeroNivel(int numeroNivel) {
        this.numeroNivel = numeroNivel;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public void setVictoria(boolean victoria) {
        this.victoria = victoria;
    }

    public void setRegistrosAtaques(ArrayList<RegistroAtaques> registrosAtaques) {
        this.registrosAtaques = registrosAtaques;
    }

    public void setTotalDefensasColocadas(int totalDefensasColocadas) {
        this.totalDefensasColocadas = totalDefensasColocadas;
    }

    public void setTotalZombiesGenerados(int totalZombiesGenerados) {
        this.totalZombiesGenerados = totalZombiesGenerados;
    }

    public void setTotalDefensasDestruidas(int totalDefensasDestruidas) {
        this.totalDefensasDestruidas = totalDefensasDestruidas;
    }

    public void setTotalZombiesEliminados(int totalZombiesEliminados) {
        this.totalZombiesEliminados = totalZombiesEliminados;
    }

    public void setTotalAtaquesRealizados(int totalAtaquesRealizados) {
        this.totalAtaquesRealizados = totalAtaquesRealizados;
    }
    
}
