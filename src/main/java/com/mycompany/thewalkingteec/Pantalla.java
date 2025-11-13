/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.thewalkingteec;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Alina
 */
public class Pantalla extends javax.swing.JFrame {

    private final Juego juego;
    
    public Pantalla() {
        initComponents();
        prepararPanelJuego();
        detectaClicks();

        String nombreJugador = JOptionPane.showInputDialog(this, "Ingresa tu nombre:", "Nuevo Juego", JOptionPane.QUESTION_MESSAGE);
        if (nombreJugador == null || nombreJugador.trim().isEmpty()) nombreJugador = "Jugador1";

        juego = new Juego(nombreJugador);
        juego.sacarPantalla(this);  
        actualizarHudNivelYEspacios();

        System.out.println("Juego inicializado");
        System.out.println("Pantalla conectada");
        setTitle("The Walking Teec");
        setLocationRelativeTo(null); //centrar
        setResizable(false);

        pack();

        int ancho = ANCHO_PANEL + pnlControles.getPreferredSize().width + 40; // margen
        int alto  = Math.max(ALTO_PANEL + 60, getHeight()); // un poquito más alto
        setSize(ancho, alto);

        toFront();
        requestFocus();
    }
    public Pantalla(Juego juego) {
        this.juego = juego;
        initComponents();
        prepararPanelJuego(); 
        actualizarHudNivelYEspacios();
        detectaClicks();
        juego.sacarPantalla(this);
        System.out.println("Pantalla conectada (ctor con Juego)");
    }
     
    private void prepararPanelJuego() {
        // Para usar setBounds(x,y,w,h) en los labels de zombies/armas
        pnlJuego.setLayout(null);
        pnlJuego.setPreferredSize(new java.awt.Dimension(ANCHO_PANEL, ALTO_PANEL)); // 1000 x 1000
        pnlJuego.setBackground(new java.awt.Color(51, 102, 0));
        pnlJuego.revalidate();
    }
    private boolean colocacionBloqueada = false;
    //Para asignar los espacios según el nivel 
    public static final int TAMANO_CASILLA = 40;
    public static final int TAMANO_MATRIZ = 25; // 25x25 celdas
    public static final int ANCHO_PANEL = TAMANO_CASILLA * TAMANO_MATRIZ;  // 1000px
    public static final int ALTO_PANEL = TAMANO_CASILLA * TAMANO_MATRIZ; // 1000px
   
    private enum ModoJuego {
        NORMAL,                         //No hace nada
        ARBOL,                         //Para cuando se coloca el juego.getArbol() 
        COLOCANDO_ARMA_CONTACTO,       //Para cuando se coloca el arma de contacto
        COLOCANDO_ARMA_MULTIPLE,       //Para cuando se coloca el arma múltiple
        COLOCANDO_ARMA_AEREA,          //arma aerea...
        COLOCANDO_ARMA_MEDIO_ALCANCE,  //....
        COLOCANDO_BLOQUE  
    }

   private ModoJuego modoActual = ModoJuego.NORMAL;
    private JLabel lblModoActual;
    
    private final List<JLabel> armasVisuales = new ArrayList<>();
    private final List<Defensa> defensas = new ArrayList<>();
    

    // HashMap para relacionar defensas con sus JLabels
    private HashMap<Defensa, JLabel> defensaLabels = new HashMap<>();
    private HashMap<Zombies, JLabel> zombieLabels = new HashMap<>();
    
    //Objetos del juego     
    private JLabel lblArbol;
    
    
    private boolean batallaIniciada = false;

    
    private void detectaClicks() {
        // Detecta clics en el tablero para sacar la posicion
        pnlJuego.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                manejarClickEnMapa(e);
            }
        });
    }

    
    private void manejarClickEnMapa(MouseEvent e) {
    int columna = e.getX() / TAMANO_CASILLA;
    int fila    = e.getY() / TAMANO_CASILLA;

    if (fila < 0 || fila >= TAMANO_MATRIZ || columna < 0 || columna >= TAMANO_MATRIZ) return;

    if (colocacionBloqueada) {
        if (modoActual != ModoJuego.NORMAL) {
            JOptionPane.showMessageDialog(this, "Acción bloqueada temporalmente.");
        } else {
            mostrarInfoObjeto(fila, columna);
        }
        return;
    }

    //No permitir colocar el Árbol durante la batalla
    if (juego.isBatallaEnCurso() && modoActual == ModoJuego.ARBOL) {
    JOptionPane.showMessageDialog(this, "No se puede colocar el Árbol durante la batalla.");
    return;
    }

    if (juego.getMatriz()[fila][columna] != null) {
        JOptionPane.showMessageDialog(this, "Esta casilla ya está ocupada.");
        return;
    }

    switch (modoActual) {
        case COLOCANDO_ARMA_CONTACTO:
            colocarArmaContacto(fila, columna); break;
        case COLOCANDO_ARMA_AEREA:
            colocarArmaAerea(fila, columna); break;
        case COLOCANDO_ARMA_MULTIPLE:
            colocarArmaMultiple(fila, columna); break;
        case COLOCANDO_ARMA_MEDIO_ALCANCE:
            colocarArmaMedioAlcance(fila, columna); break;
        case ARBOL:
            colocarArbol(fila, columna); break;
        case COLOCANDO_BLOQUE:
            colocarBloque(fila, columna); break;
        case NORMAL:
        default:
            mostrarInfoObjeto(fila, columna); break;
    }

    modoActual = ModoJuego.NORMAL;
    pnlJuego.setCursor(Cursor.getDefaultCursor());
}
    public void setModoJuegoBloqueado(boolean bloqueado) {
        // Dejá habilitados los botones de colocar
        btnArmaContacto.setEnabled(true);
        btnAgregarArmaMedioAlcance.setEnabled(true);
        btnArmaMultiple.setEnabled(true);
        AgregarArmaAerea.setEnabled(true);
        btnBloque.setEnabled(true);
        btnArbolDeLaVida.setEnabled(true);

        // Solo controla el flujo de combate
        btnStart.setEnabled(!bloqueado);
    }
    
    //Métodos privados de la clase 
    private ImageIcon cargarIcono(String ruta) {
        try {
          URL url = getClass().getResource(ruta);        
          if (url != null) {
               ImageIcon icon = new ImageIcon(url);
              return new ImageIcon(icon.getImage().getScaledInstance(
                    TAMANO_CASILLA, TAMANO_CASILLA, java.awt.Image.SCALE_SMOOTH));
            } else {
                System.err.println("Recurso no encontrado: " + ruta);
            }
        }     catch (Exception e) {
            System.err.println("Error cargando icono: " + ruta);
            e.printStackTrace();
        }
        return null;
    }
    
    
    // Método genérico para colocar cualquier tipo de arma en el tablero
    private void colocarArmaGenerica(Defensa arma, String iconPath, int fila, int columna) {

        // Validar espacios
        if (!hayEspacioPara(arma)) {
            JOptionPane.showMessageDialog(
            this,
            "No puedes colocar más armas.\n" +
            "Nivel: " + juego.getNivelActual() +
            " | Usados: " + camposUsados() +
            " | Límite: " + calcularLimiteCampos(juego.getNivelActual()) +
            " | Faltan: " + Math.max(0, calcularLimiteCampos(juego.getNivelActual()) - camposUsados()),
            "Límite de espacios alcanzado",
            JOptionPane.WARNING_MESSAGE
            );
        return;
        }
        
    int nivel = juego.getNivelActual();
    for (int i = 1; i < nivel; i++) {
        arma.subirNivel();  // usa tu override (+vida, +daño, +alcance, etc.)
    }

    // Crear label 
    ImageIcon icon = cargarIcono(iconPath);
    JLabel lbl = (icon == null) ? new JLabel("🛡") : new JLabel(icon);
    lbl.setBounds(columna * TAMANO_CASILLA, fila * TAMANO_CASILLA, TAMANO_CASILLA, TAMANO_CASILLA);
    
    pnlJuego.add(lbl);
    pnlJuego.revalidate();
    pnlJuego.repaint();
    armasVisuales.add(lbl);

    // Posición en el modelo
    arma.setFila(fila);
    arma.setColumna(columna);

    //Registrar en colecciones
    defensas.add(arma);
    defensaLabels.put(arma, lbl);
    if (arma instanceof Arma a) {
        a.setListaZombies(juego.getListaZombies());
    }
    juego.getListaDefensas().add(arma);
    juego.getMatriz()[fila][columna] = arma;
    actualizarHudNivelYEspacios();
    if (juego.isBatallaEnCurso() && arma != null && !arma.isAlive()) {
    arma.start(); // para que empiece a atacar de inmediato en plena batalla
    }
  
    arma.setRefLabel(lbl);        // para poder remover su JLabel al morir
    arma.setRefPantalla(this);    // para llamar mostrarRIP/eliminarLabelDe desde Tropa.morir()
    arma.setRefJuego(juego);      // para que Tropa.morir() pueda llamar juego.eliminarTropa(this)


    if (arma.getVidaActual() <= 0) arma.setVidaActual(arma.getVidaInicial());
    for (Zombies z : juego.getListaZombies()) {
    z.marcarRecalculoObjetivo();
}
    
}
    
    private void colocarArmaAerea(int fila, int columna) {
        colocarArmaGenerica(new ArmaAerea(), "/images/arma_aerea.png", fila, columna);
    }

    private void colocarArmaMultiple(int fila, int columna) {
        colocarArmaGenerica(new ArmaAtaqueMultiple(), "/images/arma_multiple.png", fila, columna);
    }

    private void colocarArmaMedioAlcance(int fila, int columna) {
        colocarArmaGenerica(new ArmaMedioAlcance(), "/images/arma_medio_alcance.png", fila, columna);
    }
    private void colocarArmaContacto(int fila, int columna) {
        colocarArmaGenerica(new ArmaDeContacto(), "/images/arma_contacto.png", fila, columna);
    }
    private void colocarBloque(int fila, int columna) {
        colocarArmaGenerica(new Bloque(), "/images/bloque.png", fila, columna);   
    }
    
    public void initPanelControles(){
    }
    public static class PendingPlacement {
    public final ModoJuego type;
    public final int row;
    public final int col;
    public final javax.swing.ImageIcon icon;

    public PendingPlacement(ModoJuego type, int row, int col, ImageIcon icon) {
        this.type = type; this.row = row; this.col = col; this.icon = icon;
    }
}
    //Calcular los espacios por nivel, comenzando por 20 en el 1ero y aumentando 5 * nivel
    private int calcularLimiteCampos(int nivel) {
    if (nivel < 1) nivel = 1;
    if (nivel > 10) nivel = 10;
    return 20 + (nivel - 1) * 5; // nivel 10 = 65
    }
    public void setNivelActual(int nivel) {
    int nivelClampeado = Math.max(1, Math.min(10, nivel));
    juego.setNivelActual(nivelClampeado);   // ← usa el SETTER, no el getter
    actualizarHudNivelYEspacios();
    }
    
    private int camposUsados() {
        int total = 0;
        for (Defensa d : juego.getListaDefensas()) {

            // no se cuenta el árbol de la vida
            if (d instanceof ArbolDeLaVida) continue;

            // Suma el costo de todos los demás tipos de defensa 
            total += d.getCostoCampos();
        }
        return total;
    }

    private boolean hayEspacioPara(Defensa arma) {
        int limite = calcularLimiteCampos(juego.getNivelActual());
        return camposUsados() + arma.getCostoCampos() <= limite;
    }

    private int espaciosRestantes() {
        int restantes =calcularLimiteCampos(juego.getNivelActual()) - camposUsados();
        return restantes;
    }
    
    private void actualizarHudNivelYEspacios() {
        int limite = calcularLimiteCampos(juego.getNivelActual());
        int restantes = Math.max(0, limite - camposUsados());
        lblNivel.setText("Nivel: " + juego.getNivelActual());
        lblEspacioDisponible.setText("Espacios disponibles: " + restantes + " / " + limite);
    }
    
    public int getConteoComponentesJuego() {
       return pnlJuego.getComponentCount();
    }
    
    public void removerDefensasVisuales() {
        for (javax.swing.JLabel lbl : defensaLabels.values()) {
            pnlJuego.remove(lbl);
        }
        
        defensaLabels.clear();
        armasVisuales.clear();
        pnlJuego.revalidate();
        pnlJuego.repaint();
    }

    public void removerArbolVisual() {
        if (lblArbol != null) {
            pnlJuego.remove(lblArbol);
            lblArbol = null;
            pnlJuego.revalidate();
            pnlJuego.repaint();
        }
    }
 
    public void colocarArbol(int fila, int columna) {

        // Verificar si ya hay un árbol
        if (lblArbol != null) {
            JOptionPane.showMessageDialog(this, "Ya existe un Árbol de la Vida en el tablero.");
            return;
        }

        // Verificar si la casilla está ocupada
        if (juego.getMatriz()[fila][columna] != null) {
            JOptionPane.showMessageDialog(this, "Esta casilla ya está ocupada.");
            return;
        }

        // Cargar imagen
        ImageIcon iconoArbol = cargarIcono("/images/ArbolVivo.png"); // ️ Corrige el nombre si no existe

        // Crear JLabel visual
        lblArbol = new JLabel(iconoArbol);
        lblArbol.setBounds(
            columna * TAMANO_CASILLA,
            fila * TAMANO_CASILLA,
            TAMANO_CASILLA,
            TAMANO_CASILLA
        );

        // Mostrar en el panel
        pnlJuego.add(lblArbol);
        pnlJuego.repaint();

        // Crear el objeto ArbolDeLaVida y registrarlo en el juego
        ArbolDeLaVida nuevoArbol = new ArbolDeLaVida(fila, columna, lblArbol, this);
        juego.setArbol(nuevoArbol);

        // Actualizar la juego.getMatriz() del juego
        juego.getMatriz()[fila][columna] = nuevoArbol;
    }
    
    public void actualizarPosicionTropa(Tropa tropa) {
        final int fx = tropa.getColumna() * TAMANO_CASILLA;
        final int fy = tropa.getFila()    * TAMANO_CASILLA;

        javax.swing.JLabel tmp = null;
        if (tropa instanceof Defensa) {
            tmp = defensaLabels.get((Defensa) tropa);
        } else if (tropa instanceof Zombies) {
            tmp = zombieLabels.get((Zombies) tropa);
        }
        if (tmp == null) return;

        final javax.swing.JLabel ref = tmp;

        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            ref.setLocation(fx, fy);
            ref.revalidate();
            ref.repaint();
            pnlJuego.repaint();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> {
                ref.setLocation(fx, fy);
                ref.revalidate();
                ref.repaint();
                pnlJuego.repaint();
            });
        }
    }

    
    public void mostrarArbolMuerto() {
        if (lblArbol != null) {
            ImageIcon iconMuerto = cargarIcono("/images/arbolMuerto.png");
            lblArbol.setIcon(iconMuerto);
            pnlJuego.repaint();
        }
    }
    
    public void crearLblZombie(Zombies zombie) {
        String ruta;
        if (zombie instanceof ZombieContacto) {
            ruta = "/images/zombie_contacto.png";
        } else if (zombie instanceof ZombieMedianoAlcance) {
            ruta = "/images/zombie_medio_alcance.png";
        } else if (zombie instanceof ZombieAereo) {
            ruta = "/images/zombie_aereo.png";
        } else if (zombie instanceof ZombieChoque) {
            ruta = "/images/zombie_choque.png";
        } else {
            ruta = "/images/zombie_default.png";
        } 
        
        ImageIcon icon = cargarIcono(ruta);
        JLabel lblZombie = new JLabel(icon);
        zombie.adjuntarUI(this, lblZombie, TAMANO_CASILLA);
        
        lblZombie.setBounds(zombie.getColumna() * TAMANO_CASILLA, zombie.getFila() * TAMANO_CASILLA, TAMANO_CASILLA, TAMANO_CASILLA);

        //mostrarlo en pantalla
        pnlJuego.add(lblZombie);
        pnlJuego.revalidate();
        pnlJuego.repaint();
        
        zombieLabels.put(zombie, lblZombie); //agregarlo a la tabla de hash
    }
    
    public void moverZombie(JLabel refLabel, int x, int y) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            refLabel.setLocation(x, y);
            refLabel.repaint();
            pnlJuego.repaint();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> {
                refLabel.setLocation(x, y);
                refLabel.repaint();
                pnlJuego.repaint();
            });
        }
    }
    
    public void mostrarZombie(Zombies zombie, int fila, int columna) {
        String ruta;
        if (zombie instanceof ZombieContacto) {
            ruta = "/images/zombie_contacto.png";
        } else if (zombie instanceof ZombieMedianoAlcance) {
            ruta = "/images/zombie_medio_alcance.png";
        } else if (zombie instanceof ZombieAereo) {
            ruta = "/images/zombie_aereo.png";
        } else if (zombie instanceof ZombieChoque) {
            ruta = "/images/zombie_choque.png";
        } else {
            ruta = "/images/zombie_default.png";
        }

        ImageIcon icon = cargarIcono(ruta);
        JLabel lblZombie = new JLabel(icon);

        lblZombie.setBounds(columna * TAMANO_CASILLA, fila * TAMANO_CASILLA, TAMANO_CASILLA, TAMANO_CASILLA);

        pnlJuego.add(lblZombie);
        pnlJuego.revalidate();
        pnlJuego.repaint();

        zombie.adjuntarUI(this, lblZombie, TAMANO_CASILLA);

        zombieLabels.put(zombie, lblZombie);
    }
    
    public void mostrarRIP(Tropa tropa){
        JLabel label = null;

        if (tropa instanceof Defensa) {
            label = defensaLabels.get((Defensa) tropa);
        } else if (tropa instanceof Zombies) {
            label = zombieLabels.get((Zombies) tropa);
        }

        if (label != null) {
            ImageIcon iconRIP = cargarIcono("/images/rip.png");
            if (iconRIP != null) {
                label.setIcon(iconRIP);
            }
            pnlJuego.repaint();
            javax.swing.Timer t = new javax.swing.Timer(700, e -> {
                eliminarLabelDe(tropa); // quita del panel y de los mapas
            });
            t.setRepeats(false);
            t.start();
            }
        }
    
    public void refrescarHUD() {
        actualizarHudNivelYEspacios(); 
    }
    
    public void eliminarLabelDe(Tropa tropa) {
        JLabel lbl = null;

        if (tropa instanceof Defensa) {
            lbl = defensaLabels.remove((Defensa) tropa);
        } else if (tropa instanceof Zombies) {
            lbl = zombieLabels.remove((Zombies) tropa);
        }

        if (lbl != null) {
            pnlJuego.remove(lbl);
            pnlJuego.revalidate();
            pnlJuego.repaint();
        }
    }
    
    public void resetearTableroCompleto() {
        //quitar todo lo visual
        pnlJuego.removeAll();
        pnlJuego.revalidate();
        pnlJuego.repaint();

        //Limpiar referencias de UI
        defensaLabels.clear();
        zombieLabels.clear();
        armasVisuales.clear();
        defensas.clear();
        lblArbol = null;

        //Estado local
        batallaIniciada = false;
        modoActual = ModoJuego.NORMAL;
        pnlJuego.setCursor(Cursor.getDefaultCursor());

        actualizarHudNivelYEspacios();
    }
    
    public void redibujarDefensasYArbol(Juego j) {
        resetearTableroCompleto();

        // Árbol vivo (si existe)
        if (j.getArbol() != null) {
            ImageIcon iconoArbol = cargarIcono("/Images/ArbolVivo.png");
            lblArbol = new JLabel(iconoArbol);
            lblArbol.setBounds(
                j.getArbol().getColumna() * TAMANO_CASILLA,
                j.getArbol().getFila()    * TAMANO_CASILLA,
                TAMANO_CASILLA, TAMANO_CASILLA
            );
            pnlJuego.add(lblArbol);
            // reconectar referencias para morir/cambiar imagen
            j.getArbol().setRefJuego(j);
            j.getArbol().setRefPantalla(this);
            j.getArbol().setRefLabel(lblArbol);
        }

        // Defensas ya mejoradas
        for (Defensa d : j.getListaDefensas()) {
            String ruta =
                (d instanceof ArmaDeContacto)       ? "/images/arma_contacto.png" :
                (d instanceof ArmaMedioAlcance)     ? "/images/arma_medio_alcance.png" :
                (d instanceof ArmaAtaqueMultiple)   ? "/images/arma_multiple.png" :
                (d instanceof ArmaAerea)            ? "/images/arma_aerea.png" :
                (d instanceof Bloque)               ? "/images/bloque.png" :
                                                      "/images/arma_default.png";

            ImageIcon icon = cargarIcono(ruta);
            JLabel lbl = (icon == null) ? new JLabel("🛡") : new JLabel(icon);
            lbl.setBounds(d.getColumna() * TAMANO_CASILLA, d.getFila() * TAMANO_CASILLA, TAMANO_CASILLA, TAMANO_CASILLA);

            pnlJuego.add(lbl);
            defensaLabels.put(d, lbl);
            d.setRefLabel(lbl);
            d.setRefPantalla(this);
            d.setRefJuego(j);
        }

        pnlJuego.revalidate();
        pnlJuego.repaint();
    }
    
    private void mostrarInfoObjeto(int fila, int columna) {
        
        //muestra la info del objeto durante la batalla con un click
        Tropa objeto = juego.getMatriz()[fila][columna];
        if (objeto == null) {
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("=== Información del objeto ===\n");
        info.append("Tipo: ").append(objeto.getClass().getSimpleName()).append("\n");
        info.append("Posición: [").append(fila).append(",").append(columna).append("]\n");
        info.append("Vida: ").append(objeto.getVidaInicial()).append("\n");
        info.append("Poder de golpe: ").append(objeto.getPoderGolpe()).append("\n");
        info.append("Golpes/seg: ").append(objeto.getGolpesPorSegundo()).append("\n\n");

        info.append("Ataques realizados: ").append(objeto.getAtaquesRealizados().size()).append("\n");
        info.append("Ataques recibidos: ").append(objeto.getAtaquesRecibidos().size()).append("\n");

        //mostrarlo
        JOptionPane.showMessageDialog(this, info.toString(), "Info del objeto", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlJuego = new javax.swing.JPanel();
        pnlControles = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        btnArmaContacto = new javax.swing.JButton();
        btnAgregarArmaMedioAlcance = new javax.swing.JButton();
        btnArmaMultiple = new javax.swing.JButton();
        AgregarArmaAerea = new javax.swing.JButton();
        btnBloque = new javax.swing.JButton();
        btnArbolDeLaVida = new javax.swing.JButton();
        jSpinner1 = new javax.swing.JSpinner();
        lblEspacioDisponible = new javax.swing.JLabel();
        lblNivel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlJuego.setBackground(new java.awt.Color(51, 102, 0));
        pnlJuego.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        javax.swing.GroupLayout pnlJuegoLayout = new javax.swing.GroupLayout(pnlJuego);
        pnlJuego.setLayout(pnlJuegoLayout);
        pnlJuegoLayout.setHorizontalGroup(
            pnlJuegoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        pnlJuegoLayout.setVerticalGroup(
            pnlJuegoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 496, Short.MAX_VALUE)
        );

        pnlControles.setBackground(new java.awt.Color(0, 153, 153));

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        btnPause.setText("Pause");
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });

        btnArmaContacto.setBackground(new java.awt.Color(0, 51, 102));
        btnArmaContacto.setForeground(new java.awt.Color(255, 255, 255));
        btnArmaContacto.setText("Agregar Arma de Contacto");
        btnArmaContacto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArmaContactoActionPerformed(evt);
            }
        });

        btnAgregarArmaMedioAlcance.setBackground(new java.awt.Color(0, 51, 102));
        btnAgregarArmaMedioAlcance.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarArmaMedioAlcance.setText("Agregar Arma Medio Alcance");
        btnAgregarArmaMedioAlcance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarArmaMedioAlcanceActionPerformed(evt);
            }
        });

        btnArmaMultiple.setBackground(new java.awt.Color(0, 51, 102));
        btnArmaMultiple.setForeground(new java.awt.Color(255, 255, 255));
        btnArmaMultiple.setText("Agregar Arma múltiple");
        btnArmaMultiple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArmaMultipleActionPerformed(evt);
            }
        });

        AgregarArmaAerea.setBackground(new java.awt.Color(0, 51, 102));
        AgregarArmaAerea.setForeground(new java.awt.Color(255, 255, 255));
        AgregarArmaAerea.setText("Agregar Arma Aerea");
        AgregarArmaAerea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AgregarArmaAereaActionPerformed(evt);
            }
        });

        btnBloque.setBackground(new java.awt.Color(0, 51, 102));
        btnBloque.setForeground(new java.awt.Color(255, 255, 255));
        btnBloque.setText("Agregar Bloque");
        btnBloque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBloqueActionPerformed(evt);
            }
        });

        btnArbolDeLaVida.setBackground(new java.awt.Color(102, 102, 102));
        btnArbolDeLaVida.setForeground(new java.awt.Color(255, 255, 255));
        btnArbolDeLaVida.setText("Agregar Árbol de la vida");
        btnArbolDeLaVida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArbolDeLaVidaActionPerformed(evt);
            }
        });

        lblEspacioDisponible.setBackground(new java.awt.Color(204, 204, 204));
        lblEspacioDisponible.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblEspacioDisponible.setOpaque(true);

        lblNivel.setBackground(new java.awt.Color(204, 204, 204));
        lblNivel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblNivel.setOpaque(true);

        jLabel1.setBackground(new java.awt.Color(0, 51, 51));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Campos");
        jLabel1.setOpaque(true);

        jLabel3.setBackground(new java.awt.Color(204, 204, 204));
        jLabel3.setText("   1");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel3.setOpaque(true);

        jLabel4.setBackground(new java.awt.Color(204, 204, 204));
        jLabel4.setText("   1");
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel4.setOpaque(true);

        jLabel5.setBackground(new java.awt.Color(204, 204, 204));
        jLabel5.setText("   3");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel5.setOpaque(true);

        jLabel6.setBackground(new java.awt.Color(204, 204, 204));
        jLabel6.setText("   2");
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel6.setOpaque(true);

        jLabel7.setBackground(new java.awt.Color(204, 204, 204));
        jLabel7.setText("   1");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel7.setOpaque(true);

        javax.swing.GroupLayout pnlControlesLayout = new javax.swing.GroupLayout(pnlControles);
        pnlControles.setLayout(pnlControlesLayout);
        pnlControlesLayout.setHorizontalGroup(
            pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlControlesLayout.createSequentialGroup()
                        .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblEspacioDisponible, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNivel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(119, 119, 119))
                    .addGroup(pnlControlesLayout.createSequentialGroup()
                        .addComponent(btnStart)
                        .addGap(18, 18, 18)
                        .addComponent(btnStop, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinner1)
                            .addComponent(btnPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlesLayout.createSequentialGroup()
                        .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnArbolDeLaVida, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnBloque, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAgregarArmaMedioAlcance, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnArmaContacto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnArmaMultiple, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AgregarArmaAerea, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(pnlControlesLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(14, 14, 14))))
        );
        pnlControlesLayout.setVerticalGroup(
            pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlControlesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnArmaContacto)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregarArmaMedioAlcance)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnArmaMultiple)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AgregarArmaAerea)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBloque)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnArbolDeLaVida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblNivel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEspacioDisponible, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart)
                    .addComponent(btnStop)
                    .addComponent(btnPause))
                .addGap(45, 45, 45))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlJuego, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlControles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlJuego, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlControles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
       
     if (juego.isBatallaEnCurso()) {
        JOptionPane.showMessageDialog(this, "La batalla ya está en curso.");
        return;
    }
    if (!juego.hayDefensasReales()) {
        JOptionPane.showMessageDialog(this, "Debes colocar al menos una defensa (además del Árbol) antes de iniciar");
        return;
    }
    if (juego.getArbol() == null) {
        JOptionPane.showMessageDialog(this, "Debes colocar el Árbol de la Vida antes de iniciar");
        return;
    }
    for (Defensa d : juego.getListaDefensas()) {
        if (d.isAlive()) d.interrupt();
    }
    for (Zombies z : juego.getListaZombies()) {
        if (z.isAlive()) z.interrupt();
    }
    // Limpieza visual más el modelo al parar manualmente

    modoActual = ModoJuego.NORMAL;
    pnlJuego.setCursor(Cursor.getDefaultCursor());
    juego.iniciarBatalla();
    JOptionPane.showMessageDialog(this, "¡La batalla ha iniciado!");
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed
       if (!juego.isBatallaEnCurso()) {
        JOptionPane.showMessageDialog(this, "No hay batalla en curso.");
        return;
    }

    if (!juego.isPausado()) {
        // Pausar
        juego.pausar();
        btnPause.setText("Resume");
        setModoJuegoBloqueado(true);   // opcional: bloquea Start durante pausa
        JOptionPane.showMessageDialog(this, "Batalla en pausa.");
    } else {
        // Reanudar
        juego.reanudar();
        btnPause.setText("Pause");
        setModoJuegoBloqueado(false);
        JOptionPane.showMessageDialog(this, "Batalla reanudada.");
    }
    }//GEN-LAST:event_btnPauseActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
          if (!juego.isBatallaEnCurso()) {
        JOptionPane.showMessageDialog(this, "No hay batalla en curso.");
        return;
    }

    // Asegura salir de pausa
    if (juego.isPausado()) {
        juego.reanudar();
        btnPause.setText("Pause");
    }

    //  todo y limpiar la pantalla sin avanzar/reintentar
    juego.detenerBatalla();
    // Limpieza visual inmediata
    resetearTableroCompleto();

    // No tocar nivel; solo dejamos listo para recolocar
    JOptionPane.showMessageDialog(this, "Batalla detenida. Puede rearmar su defensa y presionar Start.");

    }//GEN-LAST:event_btnStopActionPerformed

    private void btnAgregarArmaMedioAlcanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarArmaMedioAlcanceActionPerformed
    modoActual = ModoJuego.COLOCANDO_ARMA_MEDIO_ALCANCE;
    pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }//GEN-LAST:event_btnAgregarArmaMedioAlcanceActionPerformed

    private void btnArmaContactoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArmaContactoActionPerformed
    modoActual = ModoJuego.COLOCANDO_ARMA_CONTACTO;
    pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));   
    }//GEN-LAST:event_btnArmaContactoActionPerformed

    private void btnArmaMultipleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArmaMultipleActionPerformed
    modoActual = ModoJuego.COLOCANDO_ARMA_MULTIPLE;
    pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }//GEN-LAST:event_btnArmaMultipleActionPerformed

    private void AgregarArmaAereaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AgregarArmaAereaActionPerformed
    modoActual = ModoJuego.COLOCANDO_ARMA_AEREA;
    pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }//GEN-LAST:event_AgregarArmaAereaActionPerformed

    private void btnBloqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBloqueActionPerformed
    modoActual = ModoJuego.COLOCANDO_BLOQUE;   
    pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }//GEN-LAST:event_btnBloqueActionPerformed

    private void btnArbolDeLaVidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArbolDeLaVidaActionPerformed
        modoActual = ModoJuego.ARBOL;
        pnlJuego.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }//GEN-LAST:event_btnArbolDeLaVidaActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Pantalla.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Pantalla.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Pantalla.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Pantalla.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Pantalla().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AgregarArmaAerea;
    private javax.swing.JButton btnAgregarArmaMedioAlcance;
    private javax.swing.JButton btnArbolDeLaVida;
    private javax.swing.JButton btnArmaContacto;
    private javax.swing.JButton btnArmaMultiple;
    private javax.swing.JButton btnBloque;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel lblEspacioDisponible;
    private javax.swing.JLabel lblNivel;
    private javax.swing.JPanel pnlControles;
    private javax.swing.JPanel pnlJuego;
    // End of variables declaration//GEN-END:variables
}
