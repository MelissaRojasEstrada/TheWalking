/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.thewalkingteec;
import java.util.List;

/**
 * @author melissa
 */
public class ArmaMedioAlcance extends Arma {
    private String tipoProyectil; // flecha, bala, fuego, etc.
    private int velocidadProyectil; // velocidad del proyectil 
    
    public ArmaMedioAlcance() {
        super();
        setCostoCampos(1);
        setNombre("Arma de Mediano Alcance");
        setVidaInicial(50);
        setAlcance(5);
        setPoderGolpe(30);
        setGolpesPorSegundo(3);
        setTiempoRecarga(800); // Ataca cada 0.8 segundos
        setMuniciones(-1); // Municiones infinitas por defecto
        setVidaInicial(50);
        setVidaActual(50);
        
        this.tipoProyectil = "Proyectil";
        this.velocidadProyectil = 10;
    }
    

    public ArmaMedioAlcance(String tipoProyectil, int alcance) {
        this();
        this.tipoProyectil = tipoProyectil;
        setAlcance(alcance);
        setNombre("Arma de " + tipoProyectil);

        switch (tipoProyectil.toLowerCase()) {
            case "flecha":
                setPoderGolpe(25);
                setGolpesPorSegundo(4);
                setTiempoRecarga(700);
                this.velocidadProyectil = 15;
                break;
            case "bala":
                setPoderGolpe(35);
                setGolpesPorSegundo(5);
                setTiempoRecarga(600);
                this.velocidadProyectil = 20;
                break;
            case "fuego":
                setPoderGolpe(40);
                setGolpesPorSegundo(2);
                setTiempoRecarga(1000);
                this.velocidadProyectil = 8;
                break;
            default:
                // mantener valores por defecto
        }
    }
    
    @Override
     public boolean zombieEnRango(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return false; 
        double distancia = calcularDistanciaEuclidiana(zombie);
        return distancia <= getAlcance();
    }
    
    @Override
    public void atacarZombie(Zombies zombie) {
        if (zombie == null || zombie.getVidaActual() <= 0) return; 
        if (!zombieEnRango(zombie)) {
            System.out.println(getNombre() + " no alcanza a " + zombie.getNombre() +
                    " (dist=" + String.format("%.1f", calcularDistanciaEuclidiana(zombie)) +
                    ", alcance=" + getAlcance() + ")");
            return;
        }

        int dano = calcularDano();
        int vidaRestante = zombie.recibirAtaque(dano);
        registrarAtaque(zombie, dano);

        System.out.println(getNombre() + " disparó " + tipoProyectil + " a " + zombie.getNombre() + " causando " + dano + " de daño. Vida restante: " + vidaRestante);
    }
    

    @Override
     public void subirNivel() {
        super.subirNivel(); // solo incrementa el nivel en Tropa


        setAlcance(getAlcance() + 1);                   // +1 rango
        setPoderGolpe(getPoderGolpe() + 4);             // +4 daño por nivel
        setVidaInicial(getVidaInicial() + 8);           // +8 vida máx
        setVidaActual(Math.min(getVidaActual() + 8, getVidaInicial()));

        // (Opcional) reduce ligeramente recarga, sin bajar de 300ms:
        setTiempoRecarga(Math.max(300, getTiempoRecarga() - 30));

        // Ajuste suave por la “potencia” del proyectil:
        velocidadProyectil += 2;

        System.out.println(getNombre() + " subió al nivel " + getNivel() +
                ". Alcance=" + getAlcance() +
                ", PG=" + getPoderGolpe() +
                ", RC=" + getTiempoRecarga() + "ms");
    }
    
    @Override
     protected int calcularDano() {

        int danoBase = getPoderGolpe();
        int bonusVelocidad = Math.max(0, velocidadProyectil / 10); // p.ej. 10→+1, 20→+2
        return danoBase + bonusVelocidad;
    }
    

    public void cambiarMunicion(String nuevoTipo) {
        this.tipoProyectil = nuevoTipo;
        System.out.println(getNombre() + " cambió a munición " + nuevoTipo);

        switch (nuevoTipo.toLowerCase()) {
            case "explosiva":
                setPoderGolpe(getPoderGolpe() + 12);
                setTiempoRecarga(getTiempoRecarga() + 100); // más lenta
                break;
            case "perforante":
                setPoderGolpe(getPoderGolpe() + 8);
                velocidadProyectil += 5;                   // más bonus
                break;
            case "incendiaria":
                setPoderGolpe(getPoderGolpe() + 10);
                // Si querés representar DOT, implementarlo en atacarZombie con ticks; por ahora solo +daño
                break;
            default:
                // sin cambios
        }
    }
    @Override
    public boolean objetivoEnRango(Zombies objetivo) {
        // Contacto: solo ataca si está al lado (distancia = 1)
        int distancia = Math.abs(this.getFila() - objetivo.getFila()) +
                       Math.abs(this.getColumna() - objetivo.getColumna());
        return distancia == 1;
    }
    
    @Override
    public Zombies buscarObjetivoCercano(List<Zombies> zombies) {
        Zombies masCercano = null;
        int menorDistancia = Integer.MAX_VALUE;
        
        for (Zombies zombie : zombies) {
            if (zombie instanceof Zombies && zombie.getVidaInicial() > 0) {
                int distancia = Math.abs(this.getFila() - zombie.getFila()) +
                               Math.abs(this.getColumna() - zombie.getColumna());
                
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    masCercano = zombie;
                }
            }
        }
        
        return masCercano;
    }
    
    @Override
    public void atacar(Tropa objetivoAAtacar) {
        if (objetivoAAtacar == null || objetivoAAtacar.getVidaInicial() <= 0) {
            return;
        }
        
        int dano = this.getPoderGolpe();
        int vidaAntes = objetivoAAtacar.getVidaInicial();
        objetivoAAtacar.recibirAtaque(dano);
        
        // Registrar el ataque
        registrarAtaque(objetivoAAtacar, dano);
        
        System.out.println("Arma [" + getFila() + "][" + getColumna() + "] atacó causando " + dano + " daño");
    }
    
    
    // Getters y Setters
    public String getTipoProyectil() {
        return tipoProyectil;
    }

    public void setTipoProyectil(String tipoProyectil) {
        this.tipoProyectil = tipoProyectil;
    }

    public int getVelocidadProyectil() {
        return velocidadProyectil;
    }

    public void setVelocidadProyectil(int velocidadProyectil) {
        this.velocidadProyectil = velocidadProyectil;
    }

}