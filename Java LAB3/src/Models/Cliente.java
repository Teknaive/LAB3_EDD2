package Models;
// Redes diferentes: curl ifconfig.me
// Mismas redes: 192.168.56.1

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Cliente {

    private static String HOST_WORKER = "127.0.0.1"; // IP por defecto del servidor
    private static final int PUERTO_WORKER0 = 8080; // Conecta a Worker0
    private static final int PUERTO_CLIENTE = 9090; // Escucha respuesta de Worker0/Worker1

    private static String localIP;

    static {
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("No se pudo obtener la IP local: " + e.getMessage());
            localIP = "127.0.0.1"; // Fallback a localhost
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            configurarHostWorker(scanner);

            while (true) {
                System.out.println("\n------------------Menu-------------------");
                System.out.println("Seleccione como desea crear el vector o la opcion que prefiera:");
                System.out.println("1. Numeros aleatorios");
                System.out.println("2. Desde archivo TXT");
                System.out.println("3. Salir");
                System.out.println("---------------------------------------");
                System.out.print("Escriba unicamente el numero de su eleccion: ");

                int opcionEntrada = scanner.nextInt();
                if (opcionEntrada == 3) {
                    break;
                }

                int[] vector = null;
                if (opcionEntrada == 1) {
                    System.out.println("Ingrese la longitud del vector (Ingrese unicamente un valor entero):");
                    int n = scanner.nextInt();
                    vector = generarVectorAleatorio(n);
                } else if (opcionEntrada == 2) {
                    scanner.nextLine();
                    System.out.println("Ingrese la ruta del archivo TXT (Ej: C:\\Users\\subje\\Desktop\\Ejemplo.txt): ");
                    String rutaArchivo = scanner.nextLine();
                    vector = leerVectorDesdeArchivo(rutaArchivo);
                }

                System.out.println("\nSeleccione el metodo de ordenamiento:");
                System.out.println("1. MergeSort");
                System.out.println("2. QuickSort");
                System.out.println("3. HeapSort");
                System.out.print("Escriba unicamente el numero de su eleccion: ");
                int metodoOrdenamiento = scanner.nextInt();

                System.out.println("\nIngrese el tiempo limite en segundos (Ingrese unicamente un valor entero):");
                int tiempoLimite = scanner.nextInt();

                System.out.println("\nVector original: " + Arrays.toString(vector));

                long tiempoInicio = System.currentTimeMillis();

                // Iniciar servidor para recibir respuesta
                Thread servidorThread = new Thread(() -> {
                    try {
                        esperarRespuesta();
                    } catch (Exception e) {
                        System.err.println("Error esperando respuesta: " + e.getMessage());
                    }
                });
                servidorThread.start();

                // Pequeña pausa para asegurar que el servidor esté listo
                Thread.sleep(1000);

                // Enviar solicitud al worker
                try (Socket socket = new Socket(HOST_WORKER, PUERTO_WORKER0)) {
                    System.out.println("\nConectando con Worker0 en " + HOST_WORKER + ":" + PUERTO_WORKER0);
                    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                    salida.writeObject(vector);
                    salida.writeInt(metodoOrdenamiento);
                    salida.writeInt(tiempoLimite);
                    salida.writeUTF(localIP); // Enviar IP local del cliente
                    salida.flush();
                    System.out.println("Datos enviados con exito");
                } catch (ConnectException e) {
                    System.err.println("No se pudo conectar con el servidor en " + HOST_WORKER + ":" + PUERTO_WORKER0);
                    System.out.println("Por favor revise la IP y puerto, y asegurese de que el servidor está en ejecucion.");
                    break; // Salir del bucle si no es posible conectar
                }

                // Esperar a que termine el thread del servidor
                servidorThread.join();

                long tiempoFin = System.currentTimeMillis();
                System.out.println("\nTiempo total de espera: " + ((tiempoFin - tiempoInicio) / 1000.0) + " segundos");
            }
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void configurarHostWorker(Scanner scanner) {
        System.out.println("Ingrese la direccion IP del host para los workers");
        System.out.println("(presione Enter para usar " + HOST_WORKER + "): ");
        String ip = scanner.nextLine();
        if (!ip.trim().isEmpty()) {
            HOST_WORKER = ip;
        }
        System.out.println("Usando la direccion IP: " + HOST_WORKER);
    }

    private static int[] generarVectorAleatorio(int n) {
        int[] vector = new int[n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            vector[i] = random.nextInt(1000);
        }
        return vector;
    }

    private static int[] leerVectorDesdeArchivo(String rutaArchivo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            StringBuilder contenido = new StringBuilder();
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append(",");
            }
            String[] numerosStr = contenido.toString().split(",");
            int[] vector = new int[numerosStr.length];
            for (int i = 0; i < numerosStr.length; i++) {
                vector[i] = Integer.parseInt(numerosStr[i].trim());
            }
            return vector;
        }
    }

    private static void esperarRespuesta() throws IOException, ClassNotFoundException {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_CLIENTE)) {
            System.out.println("Esperando vector ordenado...");
            try (Socket workerSocket = serverSocket.accept(); ObjectInputStream entrada = new ObjectInputStream(workerSocket.getInputStream())) {
                int[] vectorOrdenado = (int[]) entrada.readObject();
                System.out.println("Vector ordenado: " + Arrays.toString(vectorOrdenado));
            }
        }
    }
}
