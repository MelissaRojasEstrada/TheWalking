/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */
public class TheWalkingTeec {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Juego juego = new Juego("Jugador 1");
            Pantalla pantalla = new Pantalla(juego);
            pantalla.setVisible(true);
        });
    }
        }
