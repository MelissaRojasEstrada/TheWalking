/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;

public class Juego implements Serializable {
    
    //básicamente identifica la versión del código 
    private static final long serialVersionUID = 1L;// por si cambiamos de clase y queremos cargar una partida vieja  

    // info del jugador y partida
    private String nombreJugador;
    private int nivelActual;
    private int espaciosEjercitoDisponibles;
    private boolean juegoTerminado;
    private boolean batallaEnCurso;
    private final Object finLock = new Object(); //para sincronizar (usamos métodos sincronized)los threads cuando la batalla termina
    //final para que todos los threads cuando la batalla termina 
    
    
    private boolean finProcesado = false;
    private volatile boolean limpiezaHecha = false;
    private int arbolNivelPersistente = 1;  
    private static final int ARBOL_VIDA_BASE = 1000; 
    private volatile int batallaId = 0;
    private ArrayList<TropaResumen> ultimoResumenDefensas = new ArrayList<>();
    private ArrayList<TropaResumen> ultimoResumenZombies  = new ArrayList<>();
    private volatile boolean pausado = false; // Todos los threads ven si hay pausa inmediatamente
    private final Object pauseLock = new Object(); //nunca cambia de referencia , por eso el final 
    
    
    // objetos del juego
    private ArbolDeLaVida arbol;
    private ArrayList<Defensa> listaDefensas;
    private ArrayList<Zombies> listaZombies;
    private Tropa[][] matriz;

    private Pantalla refPantalla;

    private static final int ESPACIOS_INICIALES = 20;
    private static final int ESPACIOS_POR_NIVEL = 5;
    private static final int NIVELES_TOTALES = 10;
    private static final int TAMANO_MATRIZ = 25;

    // Cosntructor 
    public Juego(String nombreJugador) {
        this.nombreJugador = nombreJugador;
        this.nivelActual = 1;
        this.espaciosEjercitoDisponibles = ESPACIOS_INICIALES;
        this.juegoTerminado = false;
        this.batallaEnCurso = false;
        this.listaDefensas = new ArrayList<>();
        this.listaZombies = new ArrayList<>();
        this.matriz = new Tropa[TAMANO_MATRIZ][TAMANO_MATRIZ];
    }

    // inicializa el juego con la pantalla
    public void sacarPantalla(Pantalla refPantalla) {
        this.refPantalla = refPantalla;
    }


    public synchronized boolean iniciarBatalla() {
        
    if (juegoTerminado) {
        mostrarMensaje("El juego ya terminó.");
        return false;
    }
    if (batallaEnCurso) {
        mostrarMensaje("La batalla ya está en curso.");
        return false;
    }
    
    
    synchronized (finLock) { 
        finProcesado = false; 
    }

     tickNuevaBatalla();             

    batallaEnCurso = true;
    limpiezaHecha = false;

    if (listaZombies.isEmpty()) {
        generarZombies();
    }

    for (Defensa d : listaDefensas) {
        if (d instanceof Arma a) a.setListaZombies(listaZombies);
        d.setBattleId(batallaId);
        if (!d.isAlive()) d.start();
    }

    for (Zombies z : listaZombies) {
        z.setBattleId(batallaId);
        if (!z.isAlive()) z.start();
    }

    new Thread(new MonitorBatalla()).start();
    return true;
}

    public class MonitorBatalla implements Runnable {
        @Override
        public void run() {
            while (batallaEnCurso && !juegoTerminado) {
                try {
                    Thread.sleep(1000);
                    if (verificarFinBatalla()) {
                        finalizarBatalla();
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("Monitor de batalla interrumpido");
                    break;
                }
            }
        }
    }

    
    public int getBattleId() { return batallaId; }
    
    public synchronized void onArbolMuerto() {
    capturarResumenActual();
    batallaEnCurso = false;
    detenerBatalla();

    for (int f = 0; f < TAMANO_MATRIZ; f++) {
        for (int c = 0; c < TAMANO_MATRIZ; c++) {
            matriz[f][c] = null;
        }
    }
    listaZombies.clear();
    listaDefensas.clear();
    arbol = null;

    if (refPantalla != null) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            refPantalla.resetearTableroCompleto();
            refPantalla.refrescarHUD();
        });
    }

    limpiezaHecha = true;
    
    finalizarBatalla();
}

    private static class TropaResumen implements java.io.Serializable {
    String nombre;
    int fila, columna;
    int vidaActual;
    int poderGolpe;
    int ataquesRealizados;
    int ataquesRecibidos;

    TropaResumen(Tropa t) {
        this.nombre = t.getNombre();
        this.fila = t.getFila();
        this.columna = t.getColumna();
        this.vidaActual = t.getVidaActual();
        this.poderGolpe = t.getPoderGolpe();
        this.ataquesRealizados = (t.getAtaquesRealizados() != null ? t.getAtaquesRealizados().size() : 0);
        this.ataquesRecibidos  = (t.getAtaquesRecibidos()  != null ? t.getAtaquesRecibidos().size()  : 0);
        }
    }
    private void capturarResumenActual() {
    ArrayList<TropaResumen> def = new ArrayList<>();
    ArrayList<TropaResumen> zom = new ArrayList<>();

    for (Defensa d : listaDefensas) {
        def.add(new TropaResumen(d));
    }
    for (Zombies z : listaZombies) {
        zom.add(new TropaResumen(z));
    }
    ultimoResumenDefensas = def;
    ultimoResumenZombies  = zom;
    }
    
    public void generarZombies() {
        Random random = new Random();

        int espaciosZombies = espaciosEjercitoDisponibles;
        int zombiesGenerados = 0;

        // Reiniciar la lista de zombies anterior
        listaZombies.clear();

        while (zombiesGenerados < espaciosZombies) {
            int tipoZombie = random.nextInt(4); // 0-3

            // posición aleatoria vacía
            int fila, columna;
            do {
                fila = random.nextInt(TAMANO_MATRIZ);
                columna = random.nextInt(TAMANO_MATRIZ);
            } while (matriz[fila][columna] != null);

            // crear zombie
            Zombies nuevoZombie = crearZombie(tipoZombie, fila, columna);

            if (nuevoZombie != null) {

                nuevoZombie.setRefJuego(this);

                // lista de defensas para que el zombie pueda buscar objetivos
                nuevoZombie.listaDefensas = listaDefensas;

                // Registrar en estructura del juego
                listaZombies.add(nuevoZombie);
                matriz[fila][columna] = nuevoZombie;

                // Pintar en pantalla
                if (refPantalla != null) {
                    refPantalla.mostrarZombie(nuevoZombie, fila, columna);
                }

                zombiesGenerados++;
            }
        }
    }

    public boolean hayDefensasReales() {
        for (Defensa d : listaDefensas) {
            if (!(d instanceof ArbolDeLaVida)) return true;
        }
        return false;
    }

    public synchronized void eliminarTropa(Tropa t) {
        // 1) quitar de la matriz
        int f = t.getFila(), c = t.getColumna();
        if (f >= 0 && f < TAMANO_MATRIZ && c >= 0 && c < TAMANO_MATRIZ) {
            if (matriz[f][c] == t) matriz[f][c] = null;
        }

        // 2) quitar de listas
        if (t instanceof Defensa) {
            listaDefensas.remove((Defensa) t);
        } else if (t instanceof Zombies) {
            listaZombies.remove((Zombies) t);
        }
    }

    public Zombies crearZombie(int tipoZombie, int fila, int columna) {
        Zombies zombie;

        switch (tipoZombie) {
            case 0: // Zombie de contacto
                zombie = new ZombieContacto(fila, columna, nivelActual);
                break;

            case 1: // Zombie mediano alcance
                zombie = new ZombieMedianoAlcance(fila, columna, nivelActual);
                break;

            case 2: // Zombie aéreo
                if (nivelActual >= 3) { // aparece a partir del nivel 3
                    zombie = new ZombieAereo(fila, columna, nivelActual);
                } else {
                    // si no alcanza al nivel, crear zombie de contacto
                    return crearZombie(0, fila, columna);
                }
                break;

            case 3: // Zombie de choque
                if (nivelActual >= 4) { // Solo aparece desde nivel 4
                    zombie = new ZombieChoque(fila, columna, nivelActual);
                } else {
                    // si no alcanza al nivel, crear zombie de contacto
                    return crearZombie(0, fila, columna);
                }
                break;

            default:
                // crear zombie de contacto por default
                zombie = new ZombieContacto(fila, columna, nivelActual);
                break;
        }

        return zombie;
    }

    public void detenerBatalla() {
        reanudar();
        batallaEnCurso = false;

        // interrumpir threads de objetos
        for (Defensa defensa : listaDefensas) {
            if (defensa.isAlive()) {
                defensa.interrupt();
            }
        }

        for (Zombies zombie : listaZombies) {
            zombie.cancelarPorFinDeBatalla();
            if (zombie.isAlive()) {
                zombie.interrupt();
            }
        }
    }

    public void pausarBatalla() {
        // pausar todos los threads de defensas
        for (Defensa defensa : listaDefensas) {
            if (defensa != null && defensa.isAlive()) {
                synchronized (defensa) {
                    try {
                        defensa.wait();
                    } catch (InterruptedException e) {
                        System.err.println("Error al pausar defensa: " + e.getMessage());
                    }
                }
            }
        }

        // pausar todos los threads de zombies
        for (Zombies zombie : listaZombies) {
            if (zombie != null && zombie.isAlive()) {
                synchronized (zombie) {
                    try {
                        zombie.wait();
                    } catch (InterruptedException e) {
                        System.err.println("Error al pausar zombie: " + e.getMessage());
                    }
                }
            }
        }
    }

    public boolean verificarFinBatalla() {
        // victoria si todos los zombies eliminados
        boolean todosZombiesMuertos = true;
        for (Zombies zombie : listaZombies) {
            if (zombie.getVidaActual() > 0) {
                todosZombiesMuertos = false;
                break;
            }
        }

        // derrota si el árbol se muere
        boolean arbolDestruido = (arbol == null) || (arbol.getVidaActual() <= 0);
        return todosZombiesMuertos || arbolDestruido;
    }

   public void finalizarBatalla() {
    synchronized (finLock) {
        if (finProcesado) return;
        finProcesado = true;
    }
    if (ultimoResumenDefensas.isEmpty() && ultimoResumenZombies.isEmpty()) {
        capturarResumenActual();
    }
    
    reanudar();

    
    

    batallaEnCurso = false;
    detenerBatalla();

    boolean arbolDestruido = (arbol == null) || (arbol.getVidaActual() <= 0);
    boolean todosZombiesMuertos = true;
    for (Zombies z : listaZombies) {
        if (z.getVidaActual() > 0) { todosZombiesMuertos = false; break; }
    }

    // --- DERROTA (prioridad) ---
    if (arbolDestruido) {
        mostrarMensaje("Derrota: El Árbol de la Vida ha sido destruido");

        mostrarResumenBatalla();

        String[] opciones = {"Reintentar nivel", "Avanzar de nivel"};
        int op = JOptionPane.showOptionDialog(
            refPantalla,
            "Derrota: el Árbol de la Vida ha sido destruido.\n¿Desea reintentar el nivel o avanzar de nivel?",
            "Nivel perdido",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (op == 1) { // Avanzar
            avanzarNivel(); // limpia y deja listo siguiente
        } else {        // Reintentar
            prepararTableroParaNuevoNivel(); // limpia y deja listo mismo nivel
            mostrarMensaje("Reintento del nivel " + nivelActual + ". Coloque Árbol y defensas y presione Start.");
        }
        return; // ← no volver a llamar mostrarResumenBatalla()
    }

    // --- VICTORIA ---
    if (todosZombiesMuertos) {
        mostrarMensaje("¡Victoria! Has completado el nivel " + nivelActual);
        mostrarResumenBatalla();

        int opcion = JOptionPane.showConfirmDialog(
            null,
            "¿Desea avanzar al siguiente nivel?",
            "Nivel completado",
            JOptionPane.YES_NO_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            avanzarNivel();
        } else {
            prepararTableroParaNuevoNivel();
        }
        return;
    }

    // Caso raro
    if (refPantalla != null) {
        javax.swing.SwingUtilities.invokeLater(refPantalla::refrescarHUD);
    }
}

public void pausar() {
    if (!batallaEnCurso) return;
    pausado = true;
}

public void reanudar() {
    synchronized (pauseLock) {
        pausado = false;
        pauseLock.notifyAll(); // despierta a todos los hilos pausados
    }
}

public boolean isPausado() { return pausado; }


public void esperaSiPausado() {
    if (!pausado) return;
    synchronized (pauseLock) {
        while (pausado) {
            try { pauseLock.wait(); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}


    public void avanzarNivel() {
    detenerBatalla();
    synchronized (finLock) { finProcesado = false; }

        nivelActual++;
            if (nivelActual > NIVELES_TOTALES) {
            int opcion = javax.swing.JOptionPane.showConfirmDialog(
               null,
               "¡Felicidades! Has completado todos los niveles.\n¿Deseas generar más niveles?",
               "Juego completado",
                javax.swing.JOptionPane.YES_NO_OPTION
            );
            
            if (opcion != javax.swing.JOptionPane.YES_OPTION) {
                juegoTerminado = true;
                mostrarMensaje("¡Gracias por jugar!");
                arbolNivelPersistente= Math.max(0, arbolNivelPersistente +1);
                prepararTableroParaNuevoNivel(); // limpio y listo
                return;
            }
        }

        prepararTableroParaNuevoNivel(); // ← limpia todo, sube árbol y lo quita
        mostrarMensaje("Nivel " + nivelActual + " listo. Coloque Árbol y defensas y presione Start.");
    }

    
    public void reiniciarNivel() {
        synchronized (finLock) { finProcesado = false; }
        limpiarZombies();
    }

    public void limpiarZombies() {
        // elimina zombies de la matriz
        for (Zombies zombie : listaZombies) {
            int fila = zombie.getFila();
            int columna = zombie.getColumna();
            if (fila >= 0 && fila < TAMANO_MATRIZ && columna >= 0 && columna < TAMANO_MATRIZ) {
                matriz[fila][columna] = null;
            }
        }

        // limpiar lista
        listaZombies.clear();
    }
    private void limpiarDefensas() {
    // quita de la matriz
    for (Defensa d : new ArrayList<>(listaDefensas)) {
        if (d instanceof ArbolDeLaVida) continue;
        int f = d.getFila(), c = d.getColumna();
        if (f >= 0 && f < TAMANO_MATRIZ && c >= 0 && c < TAMANO_MATRIZ && matriz[f][c] == d) {
            matriz[f][c] = null;
        }
    }
    // quita de la lista
    listaDefensas.removeIf(d -> !(d instanceof ArbolDeLaVida));

    // UI
    if (refPantalla != null) {
        javax.swing.SwingUtilities.invokeLater(refPantalla::removerDefensasVisuales);
    }
}

// Quita el árbol del modelo y matriz (subiéndolo de nivel ANTES si corresponde)
private void subirYQuitarArbolParaNuevoNivel() {
    if (arbol == null) return;

    // Subir de nivel
    arbol.subirNivel();

    // Quitar de matriz
    int f = arbol.getFila(), c = arbol.getColumna();
    if (f >= 0 && f < TAMANO_MATRIZ && c >= 0 && c < TAMANO_MATRIZ && matriz[f][c] == arbol) {
        matriz[f][c] = null;
    }

    // Quitar de lista de defensas
    listaDefensas.remove(arbol);

    // Quitar de UI
    if (refPantalla != null) {
        javax.swing.SwingUtilities.invokeLater(refPantalla::removerArbolVisual);
    }

    // Dejar null para que el jugador lo coloque de nuevo
    arbol = null;
}
    public void prepararTableroParaNuevoNivel() {
    // Parar hilos y esperar un toque
    detenerBatalla();
    joinHilosVivos(600);      
    batallaEnCurso = false;
    
    subirYQuitarArbolParaNuevoNivel();

    // Vaciar MODELO por completo (listas + matriz)
    listaZombies = new ArrayList<>();
    listaDefensas = new ArrayList<>();
    matriz = new Tropa[TAMANO_MATRIZ][TAMANO_MATRIZ];
    arbol = null;

    // Vaciar UI de un solo golpe (en EDT)
        if (refPantalla != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                refPantalla.resetearTableroCompleto();
                refPantalla.setModoJuegoBloqueado(false);
                refPantalla.refrescarHUD();
            });
        }

    // Recalcular espacios del ejército para este nivel
    espaciosEjercitoDisponibles = ESPACIOS_INICIALES + (nivelActual - 1) * ESPACIOS_POR_NIVEL;

    // Desarmar cualquier final viejo
    synchronized (finLock) { finProcesado = false; }
}

   private void joinHilosVivos(long millisMax) {
    long deadline = System.currentTimeMillis() + Math.max(0, millisMax);

    // defensas
    for (Defensa d : new ArrayList<>(listaDefensas)) {
        if (d != null && d.isAlive()) {
            try {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) break;
                d.join(Math.min(left, 200)); // joins cortitos
            } catch (InterruptedException ignored) { }
        }
    }

    // zombies
    for (Zombies z : new ArrayList<>(listaZombies)) {
        if (z != null && z.isAlive()) {
            try {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) break;
                z.join(Math.min(left, 200));
            } catch (InterruptedException ignored) { }
        }
    }
}


    public void mostrarResumenBatalla() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== RESUMEN DE LA BATALLA ===\n\n");
    sb.append("Nivel: ").append(nivelActual).append("\n\n");

    sb.append("--- DEFENSAS ---\n");
    if (ultimoResumenDefensas.isEmpty()) {
        sb.append("(sin datos)\n");
    } else {
        for (TropaResumen tr : ultimoResumenDefensas) {
            sb.append(tr.nombre).append(" [").append(tr.fila).append(",").append(tr.columna).append("]\n");
            sb.append("  Vida restante: ").append(tr.vidaActual).append("\n");
            sb.append("  Poder de golpe: ").append(tr.poderGolpe).append("\n");
            sb.append("  Ataques realizados: ").append(tr.ataquesRealizados).append("\n");
            sb.append("  Ataques recibidos: ").append(tr.ataquesRecibidos).append("\n\n");
        }
    }

    sb.append("\n--- ZOMBIES ---\n");
    if (ultimoResumenZombies.isEmpty()) {
        sb.append("(sin datos)\n");
    } else {
        for (TropaResumen tr : ultimoResumenZombies) {
            sb.append(tr.nombre).append(" [").append(tr.fila).append(",").append(tr.columna).append("]\n");
            sb.append("  Vida restante: ").append(tr.vidaActual).append("\n");
            sb.append("  Poder de golpe: ").append(tr.poderGolpe).append("\n");
            sb.append("  Ataques realizados: ").append(tr.ataquesRealizados).append("\n");
            sb.append("  Ataques recibidos: ").append(tr.ataquesRecibidos).append("\n\n");
        }
    }

    javax.swing.JTextArea txtArea = new javax.swing.JTextArea(sb.toString());
    txtArea.setEditable(false);
    txtArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
    txtArea.setCaretPosition(0); // Para que inicie desde arriba
    
    javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(txtArea);
    scrollPane.setPreferredSize(new java.awt.Dimension(500, 400)); // Ajusta el tamaño
    
    javax.swing.JOptionPane.showMessageDialog(null,scrollPane, "Resumen de Batalla :)", javax.swing.JOptionPane.INFORMATION_MESSAGE );
    // Limpia snapshot para la próxima batalla
    ultimoResumenDefensas.clear();
    ultimoResumenZombies.clear();
}


    public String obtenerInfoTropa(Tropa tropa) {
        StringBuilder info = new StringBuilder();
        info.append(tropa.getNombre())
            .append(" [")
            .append(tropa.getFila())
            .append(",")
            .append(tropa.getColumna())
            .append("]\n");
        info.append("  Vida restante: ").append(tropa.getVidaActual()).append("\n");
        info.append("  Poder de golpe: ").append(tropa.getPoderGolpe()).append("\n");
        info.append("  Ataques realizados: ").append(tropa.getAtaquesRealizados().size()).append("\n");
        info.append("  Ataques recibidos: ").append(tropa.getAtaquesRecibidos().size()).append("\n");

        return info.toString();
    }

    
    public synchronized void reubicarDefensa(Defensa d, int f0, int c0, int f1, int c1) {
    // Limpia casilla anterior si coincide
    if (f0 >= 0 && f0 < TAMANO_MATRIZ && c0 >= 0 && c0 < TAMANO_MATRIZ) {
        if (matriz[f0][c0] == d) matriz[f0][c0] = null;
    }
        // Coloca en nueva casilla (si está libre o si decides permitir “volar sobre”)
        if (f1 >= 0 && f1 < TAMANO_MATRIZ && c1 >= 0 && c1 < TAMANO_MATRIZ) {
        matriz[f1][c1] = d;
        }
    }
    // TODO: revisar este metodo
    public void guardarPartida(String nombreArchivo) {
        try {
            // Detener batalla si está en curso
            if (batallaEnCurso) {
                detenerBatalla();
            }

            FileOutputStream fileOut = new FileOutputStream(nombreArchivo + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();

            mostrarMensaje("Partida guardada exitosamente en: " + nombreArchivo + ".ser");
        } catch (IOException e) {
            mostrarMensaje("Error al guardar la partida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: revisar este metodo
    public static Juego cargarPartida(String nombreArchivo) {
        Juego juego = null;
        try {
            FileInputStream fileIn = new FileInputStream(nombreArchivo);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            juego = (Juego) in.readObject();
            in.close();
            fileIn.close();

            System.out.println(
                    "Partida cargada: " + juego.getNombreJugador() + " - Nivel " + juego.getNivelActual()
            );
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la partida: " + e.getMessage());
            e.printStackTrace();
        }
        return juego;
    }

    // TODO: revisar la parte de costo defensas (creo que se repite)
    public boolean agregarDefensa(Defensa defensa) {
        // espacios disponibles
        int costoCampos = defensa.getCostoCampos();
        if (costoCampos == 0) {
            costoCampos = 1; // default
        }
        if (espaciosEjercitoDisponibles < costoCampos) {
            mostrarMensaje(
                    "No tienes suficientes espacios. Disponibles: " + espaciosEjercitoDisponibles
                            + ", Necesarios: " + costoCampos
            );
            return false;
        }

        // agregar a lista de registros
        listaDefensas.add(defensa);
        espaciosEjercitoDisponibles -= costoCampos;

        // Agregar a la matriz
        int fila = defensa.getFila();
        int columna = defensa.getColumna();
        if (fila >= 0 && fila < TAMANO_MATRIZ && columna >= 0 && columna < TAMANO_MATRIZ) {
            matriz[fila][columna] = defensa;
        }

        return true;
    }

    public void resetearParaNuevoNivel() {
        // 1) Detén combate y hilos
        detenerBatalla();

        // 2) Eliminar de la matriz
        for (int f = 0; f < TAMANO_MATRIZ; f++) {
            for (int c = 0; c < TAMANO_MATRIZ; c++) {
                matriz[f][c] = null;
            }
        }

        // 3) Limpiar listas del modelo
        listaDefensas.clear();
        listaZombies.clear();
        arbol = null;

        // 4) Recalcular espacios del ejército para este nivel
        espaciosEjercitoDisponibles = ESPACIOS_INICIALES + (nivelActual - 1) * ESPACIOS_POR_NIVEL;

        // 5) Limpiar UI
        if (refPantalla != null) {
            refPantalla.resetearTableroCompleto();
            // si hiciste wrapper:
            // refPantalla.refrescarHUD();
        }
    }

    public void prepararNuevoNivelDesdeCero() {
        // 1) Parar pelea y threads
        detenerBatalla();

        // 2) Resetear modelo COMPLETO
        listaZombies.clear();
        listaDefensas.clear();
        matriz = new Tropa[TAMANO_MATRIZ][TAMANO_MATRIZ];
        arbol = null;

        // 3) Vaciar UI
        if (refPantalla != null) {
            refPantalla.resetearTableroCompleto();
        }

        // 4) Mensaje guía
        mostrarMensaje("Nuevo nivel " + nivelActual + ". Coloca de nuevo el Árbol y tus defensas, luego presiona Start.");
    }

    // setter de arbol
    public void setArbol(ArbolDeLaVida arbol) {
    this.arbol = arbol;

    // Posición en la matriz
    int f = arbol.getFila(), c = arbol.getColumna();
    if (f >= 0 && f < TAMANO_MATRIZ && c >= 0 && c < TAMANO_MATRIZ) {
        matriz[f][c] = arbol;
    }

    if (!listaDefensas.contains(arbol)) {
        listaDefensas.add(arbol);
    }

    arbol.setRefJuego(this);
    if (refPantalla != null) {
        arbol.setRefPantalla(refPantalla);
    }

    // <<< ADD: aplicar nivel persistente al nuevo Árbol colocado >>>
    // El constructor lo deja en 1000; subimos (nivel-1) veces para sumar +100 por nivel.
    int nivelObjetivo = Math.max(1, arbolNivelPersistente);
    for (int i = 1; i < nivelObjetivo; i++) {
        arbol.subirNivel();
    }
}
    private void tickNuevaBatalla() {
    batallaId++; // nuevo id para distinguir hilos viejos de los nuevos
}


    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
        if (refPantalla != null) {
            JOptionPane.showMessageDialog(refPantalla, mensaje);
        }
    }

    // ===============================
    // GETTERS
    // ===============================

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public int getEspaciosEjercitoDisponibles() {
        return espaciosEjercitoDisponibles;
    }

    public boolean isJuegoTerminado() {
        return juegoTerminado;
    }

    public boolean isBatallaEnCurso() {
        return batallaEnCurso;
    }

    public ArbolDeLaVida getArbol() {
        return arbol;
    }

    public ArrayList<Defensa> getListaDefensas() {
        return listaDefensas;
    }

    public ArrayList<Zombies> getListaZombies() {
        return listaZombies;
    }

    public Tropa[][] getMatriz() {
        return matriz;
    }

    public Pantalla getRefPantalla() {
        return refPantalla;
    }

    public static int getESPACIOS_INICIALES() {
        return ESPACIOS_INICIALES;
    }

    public static int getESPACIOS_POR_NIVEL() {
        return ESPACIOS_POR_NIVEL;
    }

    public static int getNIVELES_TOTALES() {
        return NIVELES_TOTALES;
    }

    public static int getTAMANO_MATRIZ() {
        return TAMANO_MATRIZ;
    }
    public int getBatallaId() {
    return batallaId;
}

    // ===============================
    // SETTERS
    // ===============================

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public void setNivelActual(int nivelActual) {
        this.nivelActual = nivelActual;
    }

    public void setEspaciosEjercitoDisponibles(int espaciosEjercitoDisponibles) {
        this.espaciosEjercitoDisponibles = espaciosEjercitoDisponibles;
    }

    public void setJuegoTerminado(boolean juegoTerminado) {
        this.juegoTerminado = juegoTerminado;
    }

    public void setBatallaEnCurso(boolean batallaEnCurso) {
        this.batallaEnCurso = batallaEnCurso;
    }

    public void setListaDefensas(ArrayList<Defensa> listaDefensas) {
        this.listaDefensas = listaDefensas;
    }

    public void setListaZombies(ArrayList<Zombies> listaZombies) {
        this.listaZombies = listaZombies;
    }

    public void setMatriz(Tropa[][] matriz) {
        this.matriz = matriz;
    }

    public void setRefPantalla(Pantalla refPantalla) {
        this.refPantalla = refPantalla;
    }
}