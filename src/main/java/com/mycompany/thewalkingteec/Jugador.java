package com.mycompany.thewalkingteec;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author melissa
 */
import java.io.Serializable;

/**
 *
 * @author Alina
 */
public class Jugador implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String usuario;
    private String contrasena;
    private boolean esAdministrador;
    private String fechaCreacion;
    
    //constructor

    public Jugador(String usuario, String contrasena, boolean esAdministrador) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.esAdministrador = esAdministrador;
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
    }

    public boolean verificarContrasena(String contrasena) {
        return this.contrasena.equals(contrasena);
    }
    
    public boolean cambiarContrasena(String contrasenaActual, String nuevaContrasena) {
        if (verificarContrasena(contrasenaActual)) {
            this.contrasena = nuevaContrasena;
            return true;
        }
        return false;
    }
    
    //cambiar equals para que compare los caracteres, no direccion de memoria
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Jugador usuario = (Jugador) o;
        return usuario.equals(usuario.usuario);
    }
    
    @Override
    public int hashCode() {
        return usuario.hashCode();
    }


    
    
    
    
    //GETTERS Y SETTERS

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public boolean isEsAdministrador() {
        return esAdministrador;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setEsAdministrador(boolean esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return "Jugador{" + "usuario=" + usuario + ", contrasena=" + contrasena + ", esAdministrador=" + esAdministrador + ", fechaCreacion=" + fechaCreacion + '}';
    }
    
    
    
}