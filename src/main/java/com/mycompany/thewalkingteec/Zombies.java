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
import java.util.List;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Alina
 */
public abstract class Zombies extends Tropa implements Serializable {
    
    private int velocidad;                      // Velocidad con la que el zombi se mueve por el campo
    private Defensa objetivoActual;             // Defensa que el zombi tiene como objetivo
    private transient Pantalla refPantalla;     // Referencia a la pantalla del juego (no se guarda al serializar)
    private transient JLabel refLabel;          // Etiqueta visual del zombi en la interfaz
    private int tamanoCasilla;                  // Tamaño de cada casilla del tablero
    private static int contadorZombies = 0;     // Contador global para asignar IDs únicos
    private volatile boolean needRecalc = false;// Marca si el zombi debe volver a buscar objetivo
    private volatile boolean cancelado_ = false;// Indica si su hilo fue cancelado (por fin de batalla)
    private volatile int battleIdSnapshot_ = -1;// Guarda el ID de la batalla donde se creó

    // CONSTRUCTOR
    public Zombies(String tipoZombie, int fila, int columna, int nivel, int vidaBase, int golpeBase, int golpesPorSeg, int alcanceBase) {
        contadorZombies++;                                   // Genera un número único para el zombi
        this.setNombre(tipoZombie + " #" + contadorZombies); // Asigna nombre con tipo + número
        this.setFila(fila);                                  // Posición inicial
        this.setColumna(columna);
        this.setVidaInicial(vidaBase + (nivel * 10));        // Escala de vida según nivel
        this.setVidaActual(vidaBase + (nivel * 10));
        this.setPoderGolpe(golpeBase + (nivel * 2));         // Golpe aumenta con el nivel
        this.setGolpesPorSegundo(golpesPorSeg);
        this.setAlcance(alcanceBase);
        this.setNivelDeAparicion(nivel);
        this.velocidad = 1000 / this.getGolpesPorSegundo();  // Entre más rápido golpea, más rápido se mueve
        this.objetivoActual = null;                          // Sin objetivo inicial
    }
    
    public static void resetearContador() { contadorZombies = 0; } // Reinicia el contador global
    
    public ArrayList<Defensa> listaDefensas = new ArrayList<>();    // Lista de todas las defensas activas

    public void adjuntarUI(Pantalla pantalla, JLabel label, int tamanoCasilla) {
        this.refPantalla = pantalla;                       // Conecta el zombi con la interfaz
        this.refLabel = label;                             // Guarda la etiqueta visual
        this.tamanoCasilla = tamanoCasilla;                // Guarda el tamaño de cada celda
    }

    @Override
    public void run() {
        tomarSnapshotBatalla();                            // Guarda el estado actual de la batalla

        while (getVidaActual() > 0 && contextoBatallaVigente()) { // Corre mientras esté vivo y en batalla
            Juego j = getRefJuego();
            if (j != null) j.esperaSiPausado();            // Se detiene si el juego está en pausa

            if (objetivoActual == null || objetivoActual.getVidaActual() <= 0) // Si no tiene objetivo o murió
                objetivoActual = buscarObjetivo(listaDefensas);                // Busca uno nuevo

            if (!contextoBatallaVigente()) break;

            if (objetivoActual != null) {
                boolean mismaCelda = (getFila() == objetivoActual.getFila() && getColumna() == objetivoActual.getColumna()); // Comprueba si ya está sobre su objetivo

                if (!mismaCelda) {                         // Si no está, se mueve una casilla hacia él
                    int f = getFila(), c = getColumna();
                    if (c < objetivoActual.getColumna()) c++;
                    else if (c > objetivoActual.getColumna()) c--;
                    if (f < objetivoActual.getFila()) f++;
                    else if (f > objetivoActual.getFila()) f--;
                    setFila(f); setColumna(c);

                    final int x = c * tamanoCasilla, y = f * tamanoCasilla; // Calcula nueva posición visual
                    final JLabel lbl = refLabel;
                    if (lbl != null)
                        SwingUtilities.invokeLater(() -> { lbl.setLocation(x, y); lbl.repaint(); }); // Mueve el JLabel en la interfaz
                } else {
                    atacar(objetivoActual);                 // Si ya está encima, ataca
                }
            }

            if (!contextoBatallaVigente()) break;
            dormirCorto(velocidad);                        // Espera un momento antes del siguiente paso
            if (Thread.currentThread().isInterrupted()) break;
        }

        if (getVidaActual() <= 0 && refPantalla != null)   // Si muere, muestra “RIP” en pantalla
            SwingUtilities.invokeLater(() -> refPantalla.mostrarRIP(this));
    }

    public void marcarRecalculoObjetivo() { needRecalc = true; } // Señala que debe buscar otro objetivo

    @Override
    public void atacar(Tropa objetivoAAtacar) {
        if (objetivoAAtacar == null) return;               // No hace nada si no hay objetivo
        if (objetivoAAtacar instanceof Zombies) return;    // Los zombis no atacan a otros zombis

        int dano = this.getPoderGolpe();                   // Calcula el daño del golpe
        int vidaAntes = objetivoAAtacar.getVidaActual();
        objetivoAAtacar.recibirAtaque(dano);
        int vidaDespues = objetivoAAtacar.getVidaActual();

        RegistroAtaques registro = new RegistroAtaques(this, objetivoAAtacar, dano, vidaAntes, vidaDespues); // Guarda el ataque en los registros
        this.getAtaquesRealizados().add(registro);
        objetivoAAtacar.getAtaquesRecibidos().add(registro);

        System.out.println(this.getNombre() + " atacó a " + objetivoAAtacar.getNombre() +
                           " causando " + dano + " de daño. Vida restante: " + vidaDespues); // Mensaje en consola
    }

    public Defensa buscarObjetivo(ArrayList<Defensa> listaDefensas) {
        Defensa objetivoCercano = null; int menorDistancia = 50;   // Busca la defensa más cercana (50 es un límite alto)
        if (listaDefensas == null || listaDefensas.isEmpty()) return null;
        for (Defensa d : listaDefensas) {
            if (d != null && d.getVidaActual() > 0 && !d.isEstaDestruida()) { // Solo defensas activas
                int distancia = Math.abs(this.getFila() - d.getFila()) + Math.abs(this.getColumna() - d.getColumna());
                if (distancia < menorDistancia) { menorDistancia = distancia; objetivoCercano = d; }
            }
        }
        return objetivoCercano;                              // Devuelve la defensa más cercana encontrada
    }

    public void moverHaciaObjetivo(Defensa objetivoALlegar) {
        if (objetivoALlegar == null) return;                 // Si no hay objetivo, no se mueve
        final int filaAnterior = this.getFila(), columnaAnterior = this.getColumna();

        // Movimiento horizontal y vertical paso a paso
        if (this.getColumna() < objetivoALlegar.getColumna()) setColumna(this.getColumna() + 1);
        else if (this.getColumna() > objetivoALlegar.getColumna()) setColumna(this.getColumna() - 1);
        if (this.getFila() < objetivoALlegar.getFila()) setFila(this.getFila() + 1);
        else if (this.getFila() > objetivoALlegar.getFila()) setFila(this.getFila() - 1);

        // Actualiza la posición visual solo si se movió realmente
        if (refPantalla != null && (filaAnterior != this.getFila() || columnaAnterior != this.getColumna()))
            SwingUtilities.invokeLater(() -> refPantalla.actualizarPosicionTropa(this));
    }

    protected final void tomarSnapshotBatalla() {            // Guarda el estado actual de la batalla
        Juego j = getRefJuego();
        battleIdSnapshot_ = (j != null ? j.getBatallaId() : -1);
        cancelado_ = false;
    }

    public final void cancelarPorFinDeBatalla() {            // Detiene al zombi cuando la batalla termina
        cancelado_ = true;
        interrupt();
    }

    protected final boolean contextoBatallaVigente() {       // Comprueba si el zombi sigue en la misma batalla
        Juego j = getRefJuego();
        return !cancelado_ && j != null && j.getBatallaId() == battleIdSnapshot_;
    }

    protected final void dormirCorto(long ms) {              // Pequeña pausa entre acciones
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    public abstract boolean puedeAtacar(Defensa objetivoActual); // Cada tipo define sus condiciones de ataque

    // --- Getters y Setters (sin comentarios porque ya son claros) ---
}

    
