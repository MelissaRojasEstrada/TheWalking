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
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Alina
 */
public abstract class Zombies extends Tropa implements Serializable{
    
    private int velocidad; //velocidad a la que se mueve
    private Defensa objetivoActual; //el objetivo más cercano para atacar
    private transient Pantalla refPantalla; 
    private transient JLabel refLabel;      // referencia directa al JLabel del zombie
    private int tamanoCasilla;              // tamaño de la celda recibido desde Pantalla
    private static int contadorZombies = 0; //para generar nombres/id diferentes
    private volatile boolean needRecalc = false;
    private volatile boolean cancelado_ = false;
    private volatile int battleIdSnapshot_ = -1;
    
    public ArrayList<Defensa> listaDefensas = new ArrayList<>(); //lista con todas las defensas




    //CONSTRUCTOR
    public Zombies(String tipoZombie, int fila, int columna, int nivel, int vidaBase, int golpeBase, int golpesPorSeg, int alcanceBase) {
        
        contadorZombies++; //generar id
        this.setNombre(tipoZombie + " #" + contadorZombies);
        
        this.setFila(fila);
        this.setColumna(columna);

        //caracteristicas por nivel
        this.setVidaInicial(vidaBase + (nivel * 10));
        this.setVidaActual(vidaBase + (nivel * 10));
        this.setPoderGolpe(golpeBase + (nivel * 2));
        this.setGolpesPorSegundo(golpesPorSeg);
        this.setAlcance(alcanceBase);
        this.setNivelDeAparicion(nivel);

        this.velocidad = 1000 / this.getGolpesPorSegundo(); //entre mas golpes x segundo mas rapido se mueve
        this.objetivoActual = null;
    }
    
    //resetea el contador
    public static void resetearContador() {
        contadorZombies = 0;
    }
    

    public void adjuntarUI(Pantalla pantalla, JLabel label, int tamanoCasilla) {
    this.refPantalla = pantalla;
    this.refLabel = label;
    this.tamanoCasilla =tamanoCasilla ;
     }
    @Override
    public void run() {
        // snapshot de la batalla actual
        tomarSnapshotBatalla();

        while (getVidaActual() > 0 && contextoBatallaVigente()) {
            Juego j = getRefJuego();
            if(j != null) j.esperaSiPausado();
            // Buscar objetivo si no hay o murió
            if (objetivoActual == null || objetivoActual.getVidaActual() <= 0) {
                objetivoActual = buscarObjetivo(listaDefensas);
            }

            if (!contextoBatallaVigente()) break;

            if (objetivoActual != null) {
                // ¿ya estoy en la misma celda que el objetivo?
                boolean mismaCelda = (getFila() == objetivoActual.getFila() &&
                                      getColumna() == objetivoActual.getColumna());

                if (!mismaCelda) {
                    // se mueve una casilla hasta el objetivo 
                    int f = getFila();
                    int c = getColumna();

                    if (c < objetivoActual.getColumna()) c++;
                    else if (c > objetivoActual.getColumna()) c--;

                    if (f < objetivoActual.getFila()) f++;
                    else if (f > objetivoActual.getFila()) f--;

                    setFila(f);
                    setColumna(c);

   
                    final int x = c * tamanoCasilla;
                    final int y = f * tamanoCasilla;
                    final JLabel lbl = refLabel;
                    if (lbl != null) {
                        SwingUtilities.invokeLater(() -> {
                            lbl.setLocation(x, y);
                            lbl.repaint();
                        });
                    }
                } else {
                    // se ataca al obtejivo 
                    atacar(objetivoActual);
                }
            }

            if (!contextoBatallaVigente()) break;

            // sleep corto con manejo de interrupción
            dormirCorto(velocidad);
            if (Thread.currentThread().isInterrupted() || !contextoBatallaVigente()) break;
        }

        // Al morir o al finalizar por fin de batalla, puedes mostrar RIP solo si realmente murió
        if (getVidaActual() <= 0 && refPantalla != null) {
            SwingUtilities.invokeLater(() -> refPantalla.mostrarRIP(this));
        }
    }


    public void marcarRecalculoObjetivo() { needRecalc = true; }
    @Override
    public void atacar(Tropa objetivoAAtacar){
        if (objetivoAAtacar == null) return; //validar que el objetivo exista
        
        if (objetivoAAtacar instanceof Zombies) return; //zombie no puede atacar a zombie
        
        int dano = this.getPoderGolpe();
        int vidaAntes = objetivoAAtacar.getVidaActual();
        objetivoAAtacar.recibirAtaque(dano);
        int vidaDespues = objetivoAAtacar.getVidaActual();
        
        //registrar el ataque en el atacante y el atacado
        RegistroAtaques registro = new RegistroAtaques(this, objetivoAAtacar, dano, vidaAntes, vidaDespues);
        this.getAtaquesRealizados().add(registro);
        objetivoAAtacar.getAtaquesRecibidos().add(registro);
        
        System.out.println(this.getNombre() + " atacó a " + objetivoAAtacar.getNombre() + 
                          " causando " + dano + " de daño. Vida restante: " + vidaDespues);
    }

    public Defensa buscarObjetivo(ArrayList<Defensa> listaDefensas){
        Defensa objetivoCercano = null; //resetear el objetivo mas cercano
        int menorDistancia = 50; //valor para iniciar a comparar las distancias (todas deben ser menor a 50 xq matriz es 25x25)
        
        if(listaDefensas == null || listaDefensas.isEmpty()) return null; //verificar que la lista se haya creado bien
        
        for(Defensa defensaActual: listaDefensas){ //evaluar para cada defensa
            
            // Verificar que la defensa esté viva
            if(defensaActual != null && defensaActual.getVidaActual() > 0 && !defensaActual.isEstaDestruida()){
                
                int x = Math.abs(this.getFila() - defensaActual.getFila());
                int y = Math.abs(this.getColumna()- defensaActual.getColumna());
                
                int distancia = x + y; //total de casillas a mover
                
                if(distancia < menorDistancia){
                    menorDistancia = distancia;
                    objetivoCercano = defensaActual;
                }
            }   
        }
        
        return objetivoCercano;
        
    }
    
    public void moverHaciaObjetivo(Defensa objetivoALlegar){
        
        if(objetivoALlegar == null) return;

        // Guardar posición anterior
        final int filaAnterior = this.getFila();
        final int columnaAnterior = this.getColumna();

        //mover horizontalmente
        if (this.getColumna() < objetivoALlegar.getColumna()) {
            setColumna(this.getColumna() + 1);
        } else if (this.getColumna() > objetivoALlegar.getColumna()) {
            setColumna(this.getColumna() - 1);
        }

        //mover verticalmente
        if (this.getFila() < objetivoALlegar.getFila()) {
            setFila(this.getFila() + 1);
        } else if (this.getFila() > objetivoALlegar.getFila()) {
            setFila(this.getFila() - 1);
        }
        
        // Actualiza UI solo si la posición cambió y hay referencia a pantalla
        if (refPantalla != null && (filaAnterior != this.getFila() || columnaAnterior != this.getColumna())) {
            SwingUtilities.invokeLater(() -> {
                refPantalla.actualizarPosicionTropa(this);
            });
        }
    }
    protected final void tomarSnapshotBatalla() {
        //se verifica que el battle id actual del juego sea el mismo q cuando el zombie comenzó
        Juego j = getRefJuego();
        battleIdSnapshot_ = (j != null ? j.getBatallaId() : -1);
        cancelado_ = false;
    }

    public final void cancelarPorFinDeBatalla() {
        cancelado_ = true;
        interrupt(); // despierta si está durmiendo
    }

    //se usa en el run, en el while loop
    protected final boolean contextoBatallaVigente() {
        Juego j = getRefJuego();
        return !cancelado_ && j != null && j.getBatallaId() == battleIdSnapshot_;
    }
    
    //este es solamente un wrapper para no escribir try-catch cada vez
    protected final void dormirCorto(long ms) { 
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
    
    public abstract boolean puedeAtacar(Defensa objetivoActual); //según su tipo y rango


    //GETTERS
    public int getVelocidad() {
        return velocidad;
    }

    public Defensa getObjetivoActual() {
        return objetivoActual;
    }

    public static int getContadorZombies() {
        return contadorZombies;
    }
    
    public Pantalla getRefPantalla() {
        return refPantalla;
    }

    public int getTamanoCasilla() {
        return tamanoCasilla;
    }

    public void setTamanoCasilla(int tamanoCasilla) {
        this.tamanoCasilla = tamanoCasilla;
    }

    public JLabel getRefLabel() {
        return refLabel;
    }

    public void setRefLabel(JLabel refLabel) {
        this.refLabel = refLabel;
    }

    public boolean isNeedRecalc() {
        return needRecalc;
    }

    public void setNeedRecalc(boolean needRecalc) {
        this.needRecalc = needRecalc;
    }

    public boolean isCancelado_() {
        return cancelado_;
    }

    public void setCancelado_(boolean cancelado_) {
        this.cancelado_ = cancelado_;
    }

    public int getBattleIdSnapshot_() {
        return battleIdSnapshot_;
    }

    public void setBattleIdSnapshot_(int battleIdSnapshot_) {
        this.battleIdSnapshot_ = battleIdSnapshot_;
    }

    public ArrayList<Defensa> getListaDefensas() {
        return listaDefensas;
    }

    public void setListaDefensas(ArrayList<Defensa> listaDefensas) {
        this.listaDefensas = listaDefensas;
    }


    //setters
    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public void setObjetivoActual(Defensa objetivoActual) {
        this.objetivoActual = objetivoActual;
    }

    public static void setContadorZombies(int contadorZombies) {
        Zombies.contadorZombies = contadorZombies;
    }
    
    public void setRefPantalla(Pantalla refPantalla) {
        this.refPantalla = refPantalla;
    }
}





    

    
