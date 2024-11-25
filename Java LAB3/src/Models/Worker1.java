// Worker1.java
package Models;

import java.io.*;
import java.net.*;

public class Worker1 {

    private static final int PUERTO = 8081; // Escucha conexiones de Worker0
    private static final int PUERTO_WORKER0 = 8080; // Opcional para responder a Worker0 (si necesario)
    private static final int PUERTO_CLIENTE = 9090; // Responde al Cliente

    private static String clienteIP;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Worker1 iniciado en puerto " + PUERTO);
            System.out.println("IP del Worker1: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                try (Socket workerSocket = serverSocket.accept()) {
                    System.out.println("Conexion recibida desde: " + workerSocket.getInetAddress().getHostAddress());
                    manejarCliente(workerSocket);
                } catch (Exception e) {
                    System.err.println("[Worker1] Error manejando conexion: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Worker1] Error iniciando servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
        int[] vector = (int[]) entrada.readObject();
        int metodoOrdenamiento = entrada.readInt();
        int tiempoLimite = entrada.readInt();
        clienteIP = entrada.readUTF(); // Leer IP del cliente

        System.out.println("[Worker1] Iniciando ordenamiento...");
        procesarOrdenamiento(vector, metodoOrdenamiento, tiempoLimite, PUERTO_WORKER0);
    }

    private static void procesarOrdenamiento(int[] vector, int metodoOrdenamiento, int tiempoLimite, int puertoDestino) {
        Ordenamientos ordenador = new Ordenamientos();
        Thread hiloOrdenamiento = new Thread(() -> {
            try {
                switch (metodoOrdenamiento) {
                    case 1 ->
                        ordenador.mergeSort(vector, 0, vector.length - 1);
                    case 2 ->
                        ordenador.quickSort(vector, 0, vector.length - 1);
                    case 3 ->
                        ordenador.heapSort(vector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        hiloOrdenamiento.start();

        try {
            hiloOrdenamiento.join(tiempoLimite * 1000L);
            if (hiloOrdenamiento.isAlive()) {
                System.out.println("[Worker1] Tiempo agotado. Reenviando a Worker0...");
                System.out.println("-----------------------");
                hiloOrdenamiento.interrupt();
                reenviarAWorker(vector, metodoOrdenamiento, tiempoLimite, "localhost", puertoDestino);
            } else {
                System.out.println("[Worker1] Ordenamiento completado. Enviando al cliente...");
                System.out.println("-----------------------");
                enviarAlCliente(vector);
            }
        } catch (InterruptedException e) {
            System.err.println("[Worker1] Error durante la ejecucion: " + e.getMessage());
        }
    }

    private static void reenviarAWorker(int[] vector, int metodoOrdenamiento, int tiempoLimite, String host, int puerto) {
        try (Socket socket = new Socket(host, puerto); ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {
            salida.writeObject(vector);
            salida.writeInt(metodoOrdenamiento);
            salida.writeInt(tiempoLimite);
            salida.writeUTF(clienteIP); // Enviar IP del cliente
            salida.flush();
        } catch (IOException e) {
            System.err.println("[Worker1] Error al reenviar datos: " + e.getMessage());
        }
    }

    private static void enviarAlCliente(int[] vector) {
        try (Socket clienteSocket = new Socket(clienteIP, PUERTO_CLIENTE); ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream())) {
            salida.writeObject(vector);
            salida.flush();
        } catch (IOException e) {
            System.err.println("[Worker1] Error enviando al cliente: " + e.getMessage());
        }
    }
}
