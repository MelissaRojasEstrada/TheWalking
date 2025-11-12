/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;

/**
 *
 * @author melissa
 */

import java.io.File;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author Alina
 */
public class ControlUsuarios {
    
    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO_USUARIOS = "usuarios.dat";

    private HashMap<String, Jugador> usuarios;
    private Jugador usuarioActual;
    
    //CONSTRUCTOR
    public ControlUsuarios() {
        this.usuarios = new HashMap<>(); //donde se van a registrar los usuarios
        cargarUsuarios();
        crearAdmin();
    }
    
    public void crearAdmin(){
        if (!existeUsuario("admin")) {
            Jugador admin = new Jugador("admin", "admin123", true);
            usuarios.put("admin", admin);
            guardarUsuarios();
        }
    }
    
    public boolean registrarUsuario(String usuario, String contrasena, boolean esAdministrador){
        
        if(usuario == null || usuario.trim().isEmpty()){
            JOptionPane.showMessageDialog(null, "El campo de usuario no puede estar vacio.", "Error usuario", JOptionPane.ERROR_MESSAGE);
        return false;
        }
        
        if (contrasena == null || contrasena.length() < 4) {
            JOptionPane.showMessageDialog(null, "La contraseña debe tener al menos 4 dígitos.", "Error contraseña", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (existeUsuario(usuario)) {
            JOptionPane.showMessageDialog(null, "El usuario ya existe", "Error usuario", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        Jugador nuevoUsuario = new Jugador(usuario, contrasena, esAdministrador);
        usuarios.put(usuario, nuevoUsuario);
        guardarUsuarios();
        
        JOptionPane.showMessageDialog(null, "Usuario registrado exitosamente");
        
        return true;
    }
    
    public boolean autenticarUsuario(String nombreUsuario, String contrasena) {
        Jugador usuario = usuarios.get(nombreUsuario);

        if (usuario == null) {
            JOptionPane.showMessageDialog(null, "Inicio de sesion exitoso");
            return true;
        }

        if (usuario.verificarContrasena(contrasena)) {
            this.usuarioActual = usuario;
            JOptionPane.showMessageDialog(null, "Usuario no encontrado", "Error usuario", JOptionPane.ERROR_MESSAGE);
            System.out.println("Inicio de sesión exitoso: " + nombreUsuario);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Contraseña incorrecta", "Error contraseña", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public void cerrarSesion() {
        if (usuarioActual != null) {
            JOptionPane.showMessageDialog(null, "Sesión cerrada");
            usuarioActual = null; //resetea el usuario
        }
    }
    
    public boolean existeUsuario(String nombreUsuario) {
        return usuarios.containsKey(nombreUsuario);
    }
    

    public boolean eliminarUsuario(String nombreUsuario) {
        if (usuarioActual == null || !usuarioActual.isEsAdministrador()) {
            JOptionPane.showMessageDialog(null, "Solo los administradores pueden eliminar usuarios", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (nombreUsuario.equals("admin")) {
            JOptionPane.showMessageDialog(null, "No se puede eliminar el usuario admin", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (usuarios.remove(nombreUsuario) != null) {
            guardarUsuarios();
            JOptionPane.showMessageDialog(null, "Usuario eliminado: " + nombreUsuario, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else{
            JOptionPane.showMessageDialog(null, "Usuario no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void guardarUsuarios() {
        try {
            FileManager.writeObject(usuarios, ARCHIVO_USUARIOS);
            System.out.println("Usuarios guardados correctamente en " + ARCHIVO_USUARIOS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al guardar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    private void cargarUsuarios() {
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) {
            System.out.println("Archivo de usuarios no existe, se creará uno nuevo");
            return;
        }

        try {
            Object obj = FileManager.readObject(ARCHIVO_USUARIOS);

            if (obj != null && obj instanceof HashMap) {
                usuarios = (HashMap<String, Jugador>) obj;
                System.out.println("Usuarios cargados: " + usuarios.size());
            } else {
                System.out.println("El archivo no contiene datos válidos");
                usuarios = new HashMap<>();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            usuarios = new HashMap<>();
            e.printStackTrace();
        }
    }
    
    public String listarUsuarios() {
        if (usuarioActual == null || !usuarioActual.isEsAdministrador()) {
            return "Solo los administradores pueden ver la lista de usuarios";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== LISTA DE USUARIOS ===\n");
        sb.append(String.format("Total de usuarios: %d\n\n", usuarios.size()));

        for (Jugador usuario : usuarios.values()) {
            sb.append(String.format("Usuario: %s\n", usuario.getUsuario()));
            sb.append(String.format("Tipo: %s\n",
                    usuario.isEsAdministrador() ? "Administrador" : "Jugador"));
            sb.append(String.format("Fecha de creación: %s\n", usuario.getFechaCreacion()));
            sb.append("----------------------------\n");
        }

        return sb.toString();
    }
    
    public Jugador obtenerUsuario(String nombreUsuario) {
        if (usuarioActual == null || !usuarioActual.isEsAdministrador()) {
            JOptionPane.showMessageDialog(null, "Solo los administradores pueden acceder a esta información", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return usuarios.get(nombreUsuario);
    }
    
    public boolean verificarPermisoAdmin() {
        return usuarioActualEsAdmin();
    }
    
    public HashMap<String, Jugador> obtenerTodosLosUsuarios() {
        if (usuarioActual == null || !usuarioActual.isEsAdministrador()) {
            JOptionPane.showMessageDialog(null, "Solo los administradores pueden acceder a esta información", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new HashMap<>(usuarios); // Retorna una copia para seguridad
    }

    
    
    
    // GETTERS
    public Jugador getUsuarioActual() {
        return usuarioActual;
    }

    public boolean hayUsuarioAutenticado() {
        return usuarioActual != null;
    }

    public boolean usuarioActualEsAdmin() {
        return usuarioActual != null && usuarioActual.isEsAdministrador();
    }

    public int getCantidadUsuarios() {
        return usuarios.size();
    }

    public HashMap<String, Jugador> getUsuarios() {
        return usuarios;
    }
    
    
    
}

