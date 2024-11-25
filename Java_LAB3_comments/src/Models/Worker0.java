// Worker0.java
package Models;

import java.io.*;
import java.net.*;

public class Worker0 {

    private static final int PUERTO = 8080; // Puerto en el que Worker0 escucha las conexiones del Cliente
    private static final int PUERTO_WORKER1 = 8081; // Puerto al que Worker0 reenviará los datos si el tiempo se agota
    private static final int PUERTO_CLIENTE = 9090; // Puerto donde Worker0 envía la respuesta al Cliente

    private static String clienteIP; // IP del cliente

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) { // Crear un servidor para escuchar en el puerto PUERTO
            System.out.println("Worker0 iniciado en puerto " + PUERTO);
            System.out.println("IP del Worker0: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                try (Socket clienteSocket = serverSocket.accept()) { // Esperar a que un cliente se conecte
                    clienteIP = clienteSocket.getInetAddress().getHostAddress(); // Obtener la IP del cliente
                    System.out.println("Cliente conectado desde: " + clienteIP);
                    manejarCliente(clienteSocket, true); // Llamar al método para manejar la conexión con el cliente
                } catch (Exception e) {
                    // Si ocurre un error al manejar al cliente, se captura la excepción
                    System.err.println("[Worker0] Error manejando cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Si ocurre un error al iniciar el servidor, se captura la excepción
            System.err.println("[Worker0] Error iniciando servidor: " + e.getMessage());
        }
    }

    // Método para manejar la conexión con el cliente
    private static void manejarCliente(Socket socket, boolean esDirectoDesdeCliente)
            throws IOException, ClassNotFoundException {
        // Crear flujos para leer y escribir objetos desde el socket
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
        // Si no es un cliente directo, se establece un flujo de salida
        ObjectOutputStream salida = esDirectoDesdeCliente ? null : new ObjectOutputStream(socket.getOutputStream());

        // Leer el vector, el método de ordenamiento, el tiempo límite y la IP del cliente
        int[] vector = (int[]) entrada.readObject();
        int metodoOrdenamiento = entrada.readInt();
        int tiempoLimite = entrada.readInt();
        clienteIP = entrada.readUTF(); // Leer IP del cliente

        System.out.println("[Worker0] Iniciando ordenamiento...");
        // Llamar al método que procesa el ordenamiento de acuerdo a los parámetros recibidos
        procesarOrdenamiento(vector, metodoOrdenamiento, tiempoLimite, salida, PUERTO_WORKER1);
    }

    // Método que procesa el ordenamiento según el algoritmo seleccionado
    private static void procesarOrdenamiento(int[] vector, int metodoOrdenamiento, int tiempoLimite,
            ObjectOutputStream salida, int puertoDestino) {
        // Crear un objeto de la clase Ordenamientos para realizar el ordenamiento
        Ordenamientos ordenador = new Ordenamientos();
        // Crear un hilo para ejecutar el ordenamiento en segundo plano
        Thread hiloOrdenamiento = new Thread(() -> {
            try {
                // Ejecutar el algoritmo de ordenamiento según el valor de metodoOrdenamiento
                switch (metodoOrdenamiento) {
                    case 1 -> ordenador.mergeSort(vector, 0, vector.length - 1); // MergeSort
                    case 2 -> ordenador.quickSort(vector, 0, vector.length - 1); // QuickSort
                    case 3 -> ordenador.heapSort(vector); // HeapSort
                }
            } catch (Exception e) {
                e.printStackTrace(); // Imprimir el error si ocurre una excepción durante el ordenamiento
            }
        });

        hiloOrdenamiento.start(); // Iniciar el hilo de ordenamiento

        try {
            // Esperar que el hilo termine dentro del tiempo límite especificado
            hiloOrdenamiento.join(tiempoLimite * 1000L);
            if (hiloOrdenamiento.isAlive()) { // Si el hilo sigue vivo, significa que se agotó el tiempo
                System.out.println("[Worker0] Tiempo agotado. Reenviando a Worker1...");
                System.out.println("-----------------------");
                hiloOrdenamiento.interrupt(); // Interrumpir el hilo de ordenamiento
                // Reenviar los datos a Worker1
                reenviarAWorker(vector, metodoOrdenamiento, tiempoLimite, "localhost", puertoDestino);
            } else {
                System.out.println("[Worker0] Ordenamiento completado. Enviando al cliente...");
                System.out.println("------------------------");
                // Enviar el resultado al cliente
                enviarAlCliente(vector, clienteIP);
            }
        } catch (InterruptedException e) {
            // Si el hilo es interrumpido, se captura la excepción
            System.err.println("[Worker0] Error durante la ejecucion: " + e.getMessage());
        }
    }

    // Método para reenviar los datos a Worker1 si el tiempo de ordenamiento se agotó
    private static void reenviarAWorker(int[] vector, int metodoOrdenamiento, int tiempoLimite, String host, int puerto) {
        try (Socket socket = new Socket(host, puerto); // Conectar con Worker1
             ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {
            // Enviar el vector, el método de ordenamiento, el tiempo límite y la IP del cliente
            salida.writeObject(vector);
            salida.writeInt(metodoOrdenamiento);
            salida.writeInt(tiempoLimite);
            salida.writeUTF(clienteIP); // Enviar IP del cliente
            salida.flush(); // Asegurarse de que los datos se envíen
        } catch (IOException e) {
            // Si ocurre un error al enviar los datos a Worker1, se captura la excepción
            System.err.println("[Worker0] Error al reenviar datos: " + e.getMessage());
        }
    }

    // Método para enviar el resultado al cliente
    private static void enviarAlCliente(int[] vector, String clienteIP) {
        try (Socket clienteSocket = new Socket(clienteIP, PUERTO_CLIENTE); // Conectar al cliente
             ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream())) {
            // Enviar el vector ordenado al cliente
            salida.writeObject(vector);
            salida.flush(); // Asegurarse de que los datos se envíen
        } catch (IOException e) {
            // Si ocurre un error al enviar los datos al cliente, se captura la excepción
            System.err.println("[Worker0] Error enviando al cliente: " + e.getMessage());
        }
    }
}
