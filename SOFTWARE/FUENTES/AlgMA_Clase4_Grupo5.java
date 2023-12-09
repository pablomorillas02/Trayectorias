/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Pablo y Salva
 */
public class MA {
    private int k;
    private int dimension;
    private double minimo;
    private double maximo;
    private double optimo;
    
    private double[] solucion_actual;
    
    private int iteraciones;
    private int nIteracionesMax;
    private double pCamb;
    private double mod;
    private int estancamiento;
    
    Funcion funcion;
    Random numero;
    
    private int tenencia;
    private double intervalo;
    
    // Memoria a corto plazo
    private ArrayList<double[]> lista_explicita;
    private ArrayList<Integer> lista_tiempos; // tenencia soluciones explícitas
    
    private ArrayList<Integer> lista_cambios;
    private ArrayList<Integer> lista_tiempos_2; // tenencia cambios
    
    // Memoria a largo plazo
    private int[][] memoria_largo_plazo;
    private double[] intervalos;
    
    //Oscilación Estratégica
    private int porCientoEst;
    private double probOE;
    private int noMejora;
    private int contOE;
    
    //VNS
    private int contadorVNS;
    private boolean vnsMich;
    
    private StringBuilder log;
    
    public MA(){ 
    };
    
    public MA(int dimension, double minimo, double maximo, double optimo, 
            double[] solucion_actual, Funcion funcion, Random numero, 
            int tenencia, double mod, int nIteracionesMax, double pCamb, double probOE,
            int porCientoEst, double intervaloTabu,StringBuilder log, boolean vnsMich) {
        this.dimension = dimension;
        this.minimo = minimo;
        this.maximo = maximo;
        this.optimo = optimo;
        
        this.log = log;
        
        this.solucion_actual = solucion_actual;
        
        this.funcion = funcion;
        this.numero = numero;
        
        this.iteraciones = 0;
        this.nIteracionesMax = nIteracionesMax;
        this.pCamb = pCamb;
        this.mod = mod;
        
        
        this.k = numero.nextInt(6) + 4;
        
        this.tenencia = tenencia;
        this.intervalo = intervaloTabu;
        
        this.lista_explicita = new ArrayList<double[]>();
        this.lista_tiempos = new ArrayList<Integer>();
                
        this.lista_cambios = new ArrayList<Integer>();
        this.lista_tiempos_2 = new ArrayList<Integer>();
        
        this.memoria_largo_plazo = new int[dimension][dimension];
        for(int i = 0 ; i < dimension ; i++){
            for(int j = 0 ; j < dimension ; j++){
                this.memoria_largo_plazo[i][j] = 0;
            }
        }
        
        this.intervalos = new double[dimension + 1]; // Habrá 11 valores que delimiten. (Para que haya 10 huecos entre cada número)
        double salto = ((maximo - minimo)/dimension); // Quizás falle esto (para los decimales puede tener problemas)
        double limite = minimo; // Empezamos abajo del todo
        for(int i = 0 ; i < dimension + 1 ; i++){ //dimension + 1 (Hay 11 valores que delimitan)
            intervalos[i] = limite;
            
            limite += salto; // Sumamos el salto para calcular el siguiente límite del intervalo
        }
        
        
        this.porCientoEst = porCientoEst;
        this.noMejora = (nIteracionesMax * this.porCientoEst)/100;
        this.contOE = 0;
        this.probOE = probOE;
        
        this.contadorVNS = 1;
        this.vnsMich = vnsMich;
        
        
    }
    
    public double[] ejecutar(){
        
        double[][] vecinos = generar_vecinos(solucion_actual);
        
        while(iteraciones < nIteracionesMax){
            
            log.append("\n\nIteracion " + iteraciones);
            log.append("\nNúmero de vecinos generados: " + this.k);
            log.append("\nSolucion actual " + funcion.evaluar(solucion_actual) + "\n");           
            
            vecinos = generar_vecinos(solucion_actual);  
            actualizar_lista(vecinos);
            evaluar(vecinos , solucion_actual);    
            
            iteraciones++;
            
            actualizar_tiempos();
            this.k = numero.nextInt(6) + 4;
        }
        
        return solucion_actual;
    }
    
    /*
     * @post actualiza la solución_actual, permite movimientos de empeoramiento
     */
    void evaluar(double[][] vecinos , double[] solucion_actual){
        double[] mejor_momento = new double[dimension];
        
        // Movimientos de empeoramiento
        boolean cent = false;
        int i = 0;     
        mejor_momento = vecinos[i];      
        i++;
        

        while(i < k){
            //Si vecinos[i] es peor que la solucion actual, pasamos
            if(Math.abs(optimo - funcion.evaluar(vecinos[i])) >= Math.abs(optimo - funcion.evaluar(mejor_momento)) || es_tabu(vecinos[i])){ // Cercano al óptimo
                i++;
            }else{
                cent = true;
                mejor_momento = vecinos[i];
            }
        }

        //Hay empeoramiento
        if(!cent){
            contOE++;

            //Cambiamos entorno
            if(this.contadorVNS == 1){                    
                vecinos = generar_vecinos_VNS_aleatorio(solucion_actual);
                this.contadorVNS++;
            }else if(this.contadorVNS == 2){
                vecinos = generar_vecinos_VNS_signo(solucion_actual);
                this.contadorVNS = 1;
            }
            
            
        }else{
            contOE = 0;
        }
  
        //Oscilación Estratégica
        
        if(contOE >= noMejora){
            log.append("Estancamiento\n");
            if(numero.nextDouble() <= probOE){
                diversificacion(vecinos);
            }else{
                intensificacion(vecinos);
            }
            
            contOE = 0;
            
        }

        if(Math.abs(optimo - funcion.evaluar(this.solucion_actual)) >= Math.abs(optimo - funcion.evaluar(mejor_momento))){
            // actualización memorias
            actualizar_lista_cambios(this.solucion_actual , mejor_momento);
            actualizar_tabla();

            this.solucion_actual = mejor_momento;
        }        
             
    }
    
    double[][] generar_vecinos_VNS_aleatorio(double[] s){
        
        log.append("Cambio de entorno a aleatorio\n");
        
        double[][] vecinos = new double[k][dimension];
        
        for(int i = 0 ; i < k ; i++){
            for(int j = 0 ; j < dimension ; j++){
                if(numero.nextDouble() < pCamb){                    
                    vecinos[i][j] = this.minimo + (this.maximo - this.minimo) * numero.nextDouble();
                }else{
                    vecinos[i][j] = s[j];
                }
            }
        }
        
        return vecinos;
    
    }
    
    double[][] generar_vecinos_VNS_signo(double[] s){
        
        log.append("Cambio de entorno a cambio de signo\n");
        
        double[][] vecinos = new double[k][dimension];
        
        for(int i = 0 ; i < k ; i++){
            for(int j = 0 ; j < dimension ; j++){
                if(numero.nextDouble() < pCamb){
                    if(!vnsMich){
                        vecinos[i][j] = vecinos[i][j] * -1;
                    }else{
                        vecinos[i][j] = 1/vecinos[i][j];
                    }
                }else{
                    vecinos[i][j] = s[j];
                }
            }
        }
        
        return vecinos;
    
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
    
    void actualizar_lista(double[][] vecinos){
        for(int i = 0 ; i < k ; i++){
            for(int j = 0 ; j < dimension ; j++){
                double nuevo_minimo = solucion_actual[j] - intervalo * solucion_actual[j];
                double intervalo_minimo = (minimo > nuevo_minimo) ? minimo : nuevo_minimo;
                double nuevo_maximo = solucion_actual[j] + intervalo * solucion_actual[j];
                double intervalo_maximo = (maximo < nuevo_maximo) ? maximo : nuevo_maximo;
                
                if(intervalo_minimo >= vecinos[i][j] || vecinos[i][j] <= intervalo_maximo){ // %1
                    actualizar_lista_explicita(vecinos[i]);  
                    
                    if(i < (k - 1)){
                        i++;
                        j = 0;
                    }else{
                        break;
                    }
                }
            }
        }
    }
    
    void actualizar_lista_explicita(double[] vecino){
        if(lista_explicita.size() != tenencia){
            lista_explicita.add(vecino);
            lista_tiempos.add(tenencia);
        }
    }
    
    void actualizar_tiempos(){
        if(lista_tiempos == null || lista_explicita == null || lista_tiempos_2 == null || lista_cambios == null) return;
        
        for(int i = 0 ; i < lista_tiempos.size() ; i++){ // actualización lista de soluciones explícitas
            if(lista_tiempos.get(i) == 1){
                lista_explicita.remove(i);
                lista_tiempos.remove(i);
            }else{
                lista_tiempos.set(i , lista_tiempos.get(i) - 1);
            }
        }
        
        for(int i = 0 ; i < lista_tiempos_2.size() ; i++){
            if(lista_tiempos_2.get(i) == 1){
                lista_cambios.remove(i);
                lista_tiempos_2.remove(i);
            }else{
                lista_tiempos_2.set(i, lista_tiempos_2.get(i) - 1);
            }
        }
    }
    
    void actualizar_tabla(){ // Recorre la solucion actual y suma 1 en cada intervalo en el que se cumpla la condición
        for(int i = 0 ; i < dimension ; i++){
            for(int j = 0 ; j < dimension ; j++){
                if(solucion_actual[i] >= intervalos[j] && solucion_actual[i] < intervalos[j + 1]){
                    memoria_largo_plazo[i][j]++;
                }
            }
        }
    }
    
    void print_tabla(){ // Muestra la matriz por pantalla
        for(int[] fila : memoria_largo_plazo) System.out.println(Arrays.toString(fila));
    }
    
    void actualizar_lista_cambios(double[] solucion_actual , double[] mejor_momento){
        if(lista_cambios.size() != tenencia){
            for(int i = 0 ; i < dimension ; i++){
                if(solucion_actual[i] != mejor_momento[i]){
                    lista_cambios.add(i);
                    lista_tiempos_2.add(tenencia);
                }
            }
        }
    }
    
    /*
     * @return true si es tabu, false si no (comprueba posiciones cambiadas y soluciones explícitas)
     */
    boolean es_tabu(double[] vecino){
        if(lista_explicita.contains(vecino)){
            return true;
        }else{ // compruebo que los valores que cambia sean distintisto a los realizados anteriormente
            for(int i = 0 ; i < lista_cambios.size() ; i++){
                if((vecino[lista_cambios.get(i)] == this.solucion_actual[lista_cambios.get(i)])) return false;
            }
        }
        
        return true;
    }

    
    public void diversificacion(double[][] vecinos) {
        log.append("Se eligio diversificacion\n");
        double menor;
        int[] interv = new int [dimension];
        double nValor;
        
        for(int i = 0; i < dimension; i++){
            menor = Double.POSITIVE_INFINITY;
            for(int j = 0; j < dimension; j++){
                if(memoria_largo_plazo[i][j] < menor){
                    menor = memoria_largo_plazo[i][j];
                    interv[i] = j;
                }
            }                    
        }
        
        generar_vecinos_OE(vecinos , solucion_actual, interv);
    }
    
    public void intensificacion(double[][] vecinos) {
        log.append("Se eligio intensificacion\n");
        double mayor;
        int[] interv = new int [dimension];
        double nValor;
        
        for(int i = 0; i < dimension; i++){
            mayor = Double.NEGATIVE_INFINITY;
            for(int j = 0; j < dimension; j++){
                if(memoria_largo_plazo[i][j] > mayor){
                    mayor = memoria_largo_plazo[i][j];
                    interv[i] = j;
                }
            }
        
            generar_vecinos_OE(vecinos , solucion_actual, interv);
            
        }
    }

    void generar_vecinos_OE(double[][] vecinos , double[] s, int[] interv){       
        for(int i = 0 ; i < k ; i++){
            for(int j = 0 ; j < dimension ; j++){
                double intervalo_minimo = this.intervalos[interv[j]];
                double intervalo_maximo = this.intervalos[interv[j]+1];

                vecinos[i][j] = intervalo_minimo + (intervalo_maximo - intervalo_minimo) * numero.nextDouble(); // [intervalo_minimo , intervalo_maximo]

            }
        }
    }
    
    // Debug
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

    public double getOptimo() {
        return optimo;
    }
    public void setOptimo(double optimo) {
        this.optimo = optimo;
    }

    public double[] getSolucion_actual() {
        return solucion_actual;
    }
    public void setSolucion_actual(double[] solucion_actual) {
        this.solucion_actual = solucion_actual;
    }

    public int getIteraciones() {
        return iteraciones;
    }
    public void setIteraciones(int iteraciones) {
        this.iteraciones = iteraciones;
    }

    public double getpCamb() {
        return pCamb;
    }
    public void setpCamb(double pCamb) {
        this.pCamb = pCamb;
    }

    public double getMod() {
        return mod;
    }
    public void setMod(double mod) {
        this.mod = mod;
    }

    public Funcion getFuncion() {
        return funcion;
    }
    public void setFuncion(Funcion funcion) {
        this.funcion = funcion;
    }

    public Random getNumero() {
        return numero;
    }
    public void setNumero(Random numero) {
        this.numero = numero;
    }  
}
