// Worker1.java
package Models;

import java.io.*;
import java.net.*;

public class Worker1 {

    private static final int PUERTO = 8081; // Puerto en el que Worker1 escucha las conexiones de Worker0
    private static final int PUERTO_WORKER0 = 8080; // Puerto opcional para responder a Worker0 (si necesario)
    private static final int PUERTO_CLIENTE = 9090; // Puerto donde Worker1 responde al Cliente

    private static String clienteIP; // Almacena la IP del cliente

    public static void main(String[] args) {
        // Intentamos crear un servidor en el puerto PUERTO para escuchar conexiones entrantes
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Worker1 iniciado en puerto " + PUERTO);
            System.out.println("IP del Worker1: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                // Aceptamos una conexión entrante de Worker0
                try (Socket workerSocket = serverSocket.accept()) {
                    System.out.println("Conexion recibida desde: " + workerSocket.getInetAddress().getHostAddress());
                    // Llamamos al método para manejar esta conexión y procesar los datos
                    manejarCliente(workerSocket);
                } catch (Exception e) {
                    // Si ocurre un error durante la conexión con Worker0, lo imprimimos en la consola
                    System.err.println("[Worker1] Error manejando conexion: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Si no se puede iniciar el servidor, se captura la excepción
            System.err.println("[Worker1] Error iniciando servidor: " + e.getMessage());
        }
    }

    // Método que maneja la conexión con Worker0 y procesa el vector de números
    private static void manejarCliente(Socket socket) throws IOException, ClassNotFoundException {
        // Creamos un flujo de entrada para leer objetos desde el socket
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
        
        // Leemos el vector de números, el método de ordenamiento y el tiempo límite
        int[] vector = (int[]) entrada.readObject();
        int metodoOrdenamiento = entrada.readInt();
        int tiempoLimite = entrada.readInt();
        clienteIP = entrada.readUTF(); // Leemos la IP del cliente

        System.out.println("[Worker1] Iniciando ordenamiento...");
        // Llamamos al método que se encarga de procesar el ordenamiento de los datos
        procesarOrdenamiento(vector, metodoOrdenamiento, tiempoLimite, PUERTO_WORKER0);
    }

    // Método que se encarga de realizar el ordenamiento del vector según el método especificado
    private static void procesarOrdenamiento(int[] vector, int metodoOrdenamiento, int tiempoLimite, int puertoDestino) {
        // Creamos un objeto de la clase Ordenamientos para realizar los algoritmos de ordenamiento
        Ordenamientos ordenador = new Ordenamientos();
        
        // Creamos un hilo para realizar el ordenamiento de manera asíncrona
        Thread hiloOrdenamiento = new Thread(() -> {
            try {
                // Según el valor de metodoOrdenamiento, se ejecuta el algoritmo correspondiente
                switch (metodoOrdenamiento) {
                    case 1 -> ordenador.mergeSort(vector, 0, vector.length - 1); // MergeSort
                    case 2 -> ordenador.quickSort(vector, 0, vector.length - 1); // QuickSort
                    case 3 -> ordenador.heapSort(vector); // HeapSort
                }
            } catch (Exception e) {
                e.printStackTrace(); // Si ocurre algún error en el ordenamiento, se imprime
            }
        });

        hiloOrdenamiento.start(); // Iniciamos el hilo para que realice el ordenamiento

        try {
            // Esperamos que el hilo termine, pero no más allá del tiempo límite especificado
            hiloOrdenamiento.join(tiempoLimite * 1000L); // Convertimos el tiempo límite a milisegundos

            if (hiloOrdenamiento.isAlive()) { // Si el hilo sigue vivo, significa que se agotó el tiempo
                System.out.println("[Worker1] Tiempo agotado. Reenviando a Worker0...");
                System.out.println("-----------------------");
                hiloOrdenamiento.interrupt(); // Interrumpimos el hilo de ordenamiento
                // Reenviamos los datos a Worker0 para que siga procesando
                reenviarAWorker(vector, metodoOrdenamiento, tiempoLimite, "localhost", puertoDestino);
            } else {
                System.out.println("[Worker1] Ordenamiento completado. Enviando al cliente...");
                System.out.println("-----------------------");
                // Si el ordenamiento se completó antes del tiempo límite, enviamos los resultados al cliente
                enviarAlCliente(vector);
            }
        } catch (InterruptedException e) {
            // Si el hilo se interrumpe inesperadamente, se captura la excepción
            System.err.println("[Worker1] Error durante la ejecucion: " + e.getMessage());
        }
    }

    // Método que reenvía los datos a Worker0 si el tiempo se agotó
    private static void reenviarAWorker(int[] vector, int metodoOrdenamiento, int tiempoLimite, String host, int puerto) {
        try (Socket socket = new Socket(host, puerto); // Conectamos con Worker0
             ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {
            // Enviamos el vector, el método de ordenamiento, el tiempo límite y la IP del cliente a Worker0
            salida.writeObject(vector);
            salida.writeInt(metodoOrdenamiento);
            salida.writeInt(tiempoLimite);
            salida.writeUTF(clienteIP); // Enviamos la IP del cliente
            salida.flush(); // Aseguramos que los datos se envíen correctamente
        } catch (IOException e) {
            // Si ocurre un error al enviar los datos a Worker0, se captura la excepción
            System.err.println("[Worker1] Error al reenviar datos: " + e.getMessage());
        }
    }

    // Método que envía el vector ordenado de vuelta al cliente
    private static void enviarAlCliente(int[] vector) {
        try (Socket clienteSocket = new Socket(clienteIP, PUERTO_CLIENTE); // Conectamos con el cliente
             ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream())) {
            // Enviamos el vector ordenado al cliente
            salida.writeObject(vector);
            salida.flush(); // Aseguramos que los datos se envíen correctamente
        } catch (IOException e) {
            // Si ocurre un error al enviar los datos al cliente, se captura la excepción
            System.err.println("[Worker1] Error enviando al cliente: " + e.getMessage());
        }
    }
}
