/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package setup;

import java.util.Random;

/**
 *
 * @author Pablo y Salva
 */
public class BLK {
    private int k;
    private int dimension;
    private double minimo;
    private double maximo;
    private double[] solucion_actual;
    Funcion funcion;
    Random numero;
    private double optimo;
    
    private int iteraciones;
    private int iteracionesMax;
    private double pCamb;
    private double mod;
    
    private StringBuilder log;
    
    Boolean mode;

    public BLK(){
        
    }
    
    // si mode es true BLK, si es false BL3
    public BLK(int dimension, double minimo, double maximo, double[] solucion_inicial, 
            Funcion funcion, Random aleatorio, double pCamb, int iteracionesMax, 
            double optimo, double mod, StringBuilder log, Boolean mode) {
        
        this.dimension = dimension;
        this.minimo = minimo;
        this.maximo = maximo;
        this.solucion_actual = solucion_inicial;
        this.funcion = funcion;
        this.iteraciones = 0;
        this.iteracionesMax = iteracionesMax;
        this.numero = aleatorio;
        this.pCamb = pCamb;
        this.mod = mod;
        this.optimo = optimo;
        this.log = log;
        
        this.mode = mode;
        
        if(mode) this.k = 4 + this.numero.nextInt(6);
        if(!mode) this.k = 3;   
    }
    
    public double[] ejecutar(){
        //double[][] vecinos = new double[k][dimension];
        
        double[][] vecinos = generar_vecinos(solucion_actual);
        
        while((iteraciones < iteracionesMax) && evaluar(vecinos , solucion_actual)){
            
            log.append("\n\nIteracion " + iteraciones);
            log.append("\nNúmero de vecinos generados: " + this.k);
            log.append("\nSolucion actual " + funcion.evaluar(solucion_actual) + "\n");
            
            if(mode) this.k = 4 + this.numero.nextInt(6);
            
            vecinos = generar_vecinos(solucion_actual);
        }
        
        return solucion_actual;
    }
    
    /*
     * @return false si no se ha encontrado mejora, true en caso contrario
     * @post en caso de devolver true, actualiza la mejor solucion
     */
    boolean evaluar(double[][] vecinos , double[] solucion_actual){
        int i = 0;
        while(i < k){
            if(Math.abs(optimo - funcion.evaluar(vecinos[i])) >= Math.abs(optimo - funcion.evaluar(solucion_actual))){ // Cercano al óptimo
                i++;
            }else{
                iteraciones++;
                this.solucion_actual = vecinos[i];
                return true;
            }
        }
        
        return false;
    }
    
    double[][] generar_vecinos(double[] s){    
        
        double[][] vecinos = new double[k][dimension];
        
        for(int i = 0 ; i < k ; i++){
            for(int j = 0 ; j < dimension ; j++){
                if(numero.nextDouble() < pCamb){
                    double nuevo_minimo = s[j] - mod * s[j];
                    double intervalo_minimo = (minimo > nuevo_minimo) ? minimo : nuevo_minimo;
                    double nuevo_maximo = s[j] + mod * s[j];
                    double intervalo_maximo = (maximo < nuevo_maximo) ? maximo : nuevo_maximo;
                    
                    vecinos[i][j] = intervalo_minimo + (intervalo_maximo - intervalo_minimo) * numero.nextDouble(); // [intervalo_minimo , intervalo_maximo]
                }else{
                    vecinos[i][j] = s[j];
                }
            }
        }
        
        return vecinos;
        
    }
    
    // Debug
    
    void print(double[] vector, int t) {
        for(int i = 0 ; i < t ; i++){
            System.out.print(vector[i] + " ");
        }
        System.out.print("->" + funcion.evaluar(vector) + "\n");
    }
    
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public double getMinimo() {
        return minimo;
    }

    public void setMinimo(double minimo) {
        this.minimo = minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public void setMaximo(double maximo) {
        this.maximo = maximo;
    }

    public double[] getSolucion_actual() {
        return solucion_actual;
    }

    public void setSolucion_actual(double[] solucion_actual) {
        this.solucion_actual = solucion_actual;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public void setFuncion(Funcion funcion) {
        this.funcion = funcion;
    }

    public int getIteraciones() {
        return iteraciones;
    }

    public void setIteraciones(int iteraciones) {
        this.iteraciones = iteraciones;
    }

    public Random getNumero() {
        return numero;
    }

    public void setNumero(Random numero) {
        this.numero = numero;
    }

}
