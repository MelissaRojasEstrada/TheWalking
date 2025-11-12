/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Alina
 */
public class ManejarPartidas {
    
    public static final String DIRECTORIO_PARTIDAS = "partidas/";
    public static final String EXTENSION = ".ser";

    //CONSTRUCTOR: crea el archivo
    public ManejarPartidas() {
        File directorio = new File(DIRECTORIO_PARTIDAS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }
    
    public boolean guardarPartida(PartidaGuardada partida) {
        try {
            String nombreArchivo = DIRECTORIO_PARTIDAS + partida.getNombreArchivo();

            FileOutputStream fileOut = new FileOutputStream(nombreArchivo);
            BufferedOutputStream buffer = new BufferedOutputStream(fileOut);
            ObjectOutputStream out = new ObjectOutputStream(buffer);

            out.writeObject(partida);

            out.close();
            buffer.close();
            fileOut.close();

            JOptionPane.showMessageDialog(null, "Partida guardada exitosamente");
            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar la partida");
            e.printStackTrace();
            return false;
        }
    }
    
    public PartidaGuardada cargarPartida(String nombreJugador, int numeroPartida) {
        try {
            String nombreArchivo = DIRECTORIO_PARTIDAS + nombreJugador
                    + "_partida" + numeroPartida + EXTENSION;

            FileInputStream fileIn = new FileInputStream(nombreArchivo);
            BufferedInputStream buffer = new BufferedInputStream(fileIn);
            ObjectInputStream in = new ObjectInputStream(buffer);

            PartidaGuardada partida = (PartidaGuardada) in.readObject();

            in.close();
            buffer.close();
            fileIn.close();

            JOptionPane.showMessageDialog(null, "Partida cargada exitosamente");            
            return partida;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la partida");
            return null;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error: Clase no encontrada al cargar partida");
            e.printStackTrace();
            return null;
        }
    }
    
    
    public ArrayList<PartidaGuardada> listarPartidasJugador(String nombreJugador) {
        //lista las partidas de un jugador en especifico
        ArrayList<PartidaGuardada> partidas = new ArrayList<>();
        File directorio = new File(DIRECTORIO_PARTIDAS);

        if (!directorio.exists()) {
            return partidas;
        }

        File[] archivos = directorio.listFiles();
        if (archivos == null) {
            return partidas;
        }

        //buscar archivos de un jugador
        for (File archivo : archivos) {
            String nombreArchivo = archivo.getName();
            if (nombreArchivo.startsWith(nombreJugador + "_partida")
                    && nombreArchivo.endsWith(EXTENSION)) {

                try {
                    Object obj = FileManager.readObject(archivo.getAbsolutePath());

                    if (obj instanceof PartidaGuardada) {
                        PartidaGuardada partida = (PartidaGuardada) obj;
                        partidas.add(partida);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al leer partida");
                }
            }
        }

        return partidas;
    }
    
    public ArrayList<PartidaGuardada> listarTodasLasPartidas() {
        ArrayList<PartidaGuardada> partidas = new ArrayList<>();
        File directorio = new File(DIRECTORIO_PARTIDAS);

        if (!directorio.exists()) {
            return partidas;
        }

        File[] archivos = directorio.listFiles();
        if (archivos == null) {
            return partidas;
        }

        for (File archivo : archivos) {
            if (archivo.getName().endsWith(EXTENSION)) {
                try {
                    Object obj = FileManager.readObject(archivo.getAbsolutePath());

                    if (obj instanceof PartidaGuardada) {
                        PartidaGuardada partida = (PartidaGuardada) obj;
                        partidas.add(partida);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al leer partida");
                }
            }
        }

        return partidas;
    }
    
    public int obtenerSiguienteNumeroPartida(String nombreJugador) {
        ArrayList<PartidaGuardada> partidas = listarPartidasJugador(nombreJugador);

        if (partidas.isEmpty()) {
            return 1;
        }

        int maxNumero = 0;
        for (PartidaGuardada partida : partidas) {
            if (partida.getNumeroPartida() > maxNumero) {
                maxNumero = partida.getNumeroPartida();
            }
        }

        return maxNumero + 1;
    }
    
    public boolean eliminarPartida(String nombreJugador, int numeroPartida) {
        String nombreArchivo = DIRECTORIO_PARTIDAS + nombreJugador
                + "_partida" + numeroPartida + EXTENSION;

        File archivo = new File(nombreArchivo);

        if (archivo.exists()) {
            boolean eliminado = archivo.delete();
            if (eliminado) {
                JOptionPane.showMessageDialog(null, "Partida eliminada");
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo eliminar la partida");
            }
            return eliminado;
        } else {
            JOptionPane.showMessageDialog(null, "La partida no existe");
            return false;
        }
    }
    
    public boolean existePartida(String nombreJugador, int numeroPartida) {
        //verifica si existe la partida
        String nombreArchivo = DIRECTORIO_PARTIDAS + nombreJugador + "_partida" + numeroPartida + EXTENSION;
        File archivo = new File(nombreArchivo);
        return archivo.exists();
    }
    
    //TODO: revisar
    public String generarReporteJugador(String nombreJugador) {
        ArrayList<PartidaGuardada> partidas = listarPartidasJugador(nombreJugador);

        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════╗\n");
        sb.append("║    PARTIDAS GUARDADAS DE: ").append(nombreJugador);
        int espacios = 22 - nombreJugador.length();
        for (int i = 0; i < espacios; i++) {
            sb.append(" ");
        }
        sb.append("║\n");
        sb.append("╚═══════════════════════════════════════════════════╝\n\n");

        if (partidas.isEmpty()) {
            sb.append("No hay partidas guardadas.\n");
        } else {
            for (PartidaGuardada partida : partidas) {
                sb.append("───────────────────────────────────────────────────\n");
                sb.append(partida.getInfoResumida());
                sb.append("\n");
            }
        }

        return sb.toString();
    }


    public boolean exportarBitacora(PartidaGuardada partida, int numeroNivel, String rutaDestino) {
        //bitacora en archivo de texto
        BitacoraNivel bitacora = partida.getBitacoraNivel(numeroNivel);

        if (bitacora == null) {
            JOptionPane.showMessageDialog(null, "No existe butacora para este nivel");
            return false;
        }

        try {
            String contenido = bitacora.generarReporteCompleto();
            FileManager.writeFile(rutaDestino, contenido);

            JOptionPane.showMessageDialog(null, "Bitácora exportada con éxito a: " + rutaDestino);
            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al exportar bitacora");
            return false;
        }
    }
    
    
    //GETTERS

    public static String getDIRECTORIO_PARTIDAS() {
        return DIRECTORIO_PARTIDAS;
    }

    public static String getEXTENSION() {
        return EXTENSION;
    }
    
}
