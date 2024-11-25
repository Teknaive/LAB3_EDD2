// Worker0.java
package Models;

import java.io.*;
import java.net.*;

public class Worker0 {

    private static final int PUERTO = 8080; // Escucha conexiones del Cliente
    private static final int PUERTO_WORKER1 = 8081; // Reenvía datos a Worker1
    private static final int PUERTO_CLIENTE = 9090; // Responde al Cliente

    private static String clienteIP;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Worker0 iniciado en puerto " + PUERTO);
            System.out.println("IP del Worker0: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                try (Socket clienteSocket = serverSocket.accept()) {
                    clienteIP = clienteSocket.getInetAddress().getHostAddress();
                    System.out.println("Cliente conectado desde: " + clienteIP);
                    manejarCliente(clienteSocket, true);
                } catch (Exception e) {
                    System.err.println("[Worker0] Error manejando cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Worker0] Error iniciando servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket socket, boolean esDirectoDesdeCliente)
            throws IOException, ClassNotFoundException {
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream salida = esDirectoDesdeCliente ? null : new ObjectOutputStream(socket.getOutputStream());

        int[] vector = (int[]) entrada.readObject();
        int metodoOrdenamiento = entrada.readInt();
        int tiempoLimite = entrada.readInt();
        clienteIP = entrada.readUTF(); // Leer IP del cliente

        System.out.println("[Worker0] Iniciando ordenamiento...");
        procesarOrdenamiento(vector, metodoOrdenamiento, tiempoLimite, salida, PUERTO_WORKER1);
    }

    private static void procesarOrdenamiento(int[] vector, int metodoOrdenamiento, int tiempoLimite,
            ObjectOutputStream salida, int puertoDestino) {
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
                System.out.println("[Worker0] Tiempo agotado. Reenviando a Worker1...");
                System.out.println("-----------------------");
                hiloOrdenamiento.interrupt();
                reenviarAWorker(vector, metodoOrdenamiento, tiempoLimite, "localhost", puertoDestino);
            } else {
                System.out.println("[Worker0] Ordenamiento completado. Enviando al cliente...");
                System.out.println("------------------------");
                enviarAlCliente(vector, clienteIP);
            }
        } catch (InterruptedException e) {
            System.err.println("[Worker0] Error durante la ejecucion: " + e.getMessage());
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
            System.err.println("[Worker0] Error al reenviar datos: " + e.getMessage());
        }
    }

    private static void enviarAlCliente(int[] vector, String clienteIP) {
        try (Socket clienteSocket = new Socket(clienteIP, PUERTO_CLIENTE); ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream())) {
            salida.writeObject(vector);
            salida.flush();
        } catch (IOException e) {
            System.err.println("[Worker0] Error enviando al cliente: " + e.getMessage());
        }
    }
}
