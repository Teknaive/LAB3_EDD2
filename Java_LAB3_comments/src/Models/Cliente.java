package Models;
// Redes diferentes: curl ifconfig.me
// Mismas redes: 192.168.56.1

import java.io.*; // Importación de clases para entrada/salida (archivos, sockets, etc.).
import java.net.*; // Clases para manejar direcciones IP y sockets.
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Cliente {

    // IP predeterminada del servidor (Worker). Puede ser configurada por el usuario.
    private static String HOST_WORKER = "127.0.0.1";

    // Puerto en el que se conecta con el servidor Worker.
    private static final int PUERTO_WORKER0 = 8080;

    // Puerto donde este cliente escuchará la respuesta del servidor.
    private static final int PUERTO_CLIENTE = 9090;

    // Variable que almacena la IP local de la máquina cliente.
    private static String localIP;

    // Bloque estático que se ejecuta al cargar la clase.
    // Aquí se obtiene la IP local de la máquina cliente.
    static {
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // Si no se puede obtener la IP local, se usa localhost como respaldo.
            System.err.println("No se pudo obtener la IP local: " + e.getMessage());
            localIP = "127.0.0.1";
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            configurarHostWorker(scanner); // Configura la IP del servidor Worker.

            while (true) {
                // Menú principal del cliente para seleccionar opciones.
                System.out.println("\n------------------Menu-------------------");
                System.out.println("Seleccione como desea crear el vector o la opcion que prefiera:");
                System.out.println("1. Numeros aleatorios");
                System.out.println("2. Desde archivo TXT");
                System.out.println("3. Salir");
                System.out.println("---------------------------------------");
                System.out.print("Escriba unicamente el numero de su eleccion: ");

                int opcionEntrada = scanner.nextInt(); // Lee la opción del usuario.
                if (opcionEntrada == 3) {
                    break; // Finaliza el programa si elige salir.
                }

                int[] vector = null; // Declaración del vector.
                if (opcionEntrada == 1) {
                    // Generación de un vector con números aleatorios.
                    System.out.println("Ingrese la longitud del vector (Ingrese unicamente un valor entero):");
                    int n = scanner.nextInt();
                    vector = generarVectorAleatorio(n);
                } else if (opcionEntrada == 2) {
                    // Lectura de un vector desde un archivo de texto.
                    scanner.nextLine(); // Limpia el buffer.
                    System.out.println("Ingrese la ruta del archivo TXT (Ej: C:\\Users\\subje\\Desktop\\Ejemplo.txt): ");
                    String rutaArchivo = scanner.nextLine();
                    vector = leerVectorDesdeArchivo(rutaArchivo);
                }

                // Selección del método de ordenamiento.
                System.out.println("\nSeleccione el metodo de ordenamiento:");
                System.out.println("1. MergeSort");
                System.out.println("2. QuickSort");
                System.out.println("3. HeapSort");
                System.out.print("Escriba unicamente el numero de su eleccion: ");
                int metodoOrdenamiento = scanner.nextInt();

                // Tiempo límite para el ordenamiento.
                System.out.println("\nIngrese el tiempo limite en segundos (Ingrese unicamente un valor entero):");
                int tiempoLimite = scanner.nextInt();

                System.out.println("\nVector original: " + Arrays.toString(vector)); // Muestra el vector original.

                long tiempoInicio = System.currentTimeMillis(); // Inicia el cronómetro.

                // Inicia un thread para actuar como servidor y esperar la respuesta del Worker.
                Thread servidorThread = new Thread(() -> {
                    try {
                        esperarRespuesta(); // Espera un vector ordenado del Worker.
                    } catch (Exception e) {
                        System.err.println("Error esperando respuesta: " + e.getMessage());
                    }
                });
                servidorThread.start();

                // Pausa breve para asegurar que el servidor esté listo.
                Thread.sleep(1000);

                // Enviar datos al servidor Worker.
                try (Socket socket = new Socket(HOST_WORKER, PUERTO_WORKER0)) {
                    System.out.println("\nConectando con Worker0 en " + HOST_WORKER + ":" + PUERTO_WORKER0);
                    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                    salida.writeObject(vector); // Envía el vector.
                    salida.writeInt(metodoOrdenamiento); // Envía el método de ordenamiento.
                    salida.writeInt(tiempoLimite); // Envía el tiempo límite.
                    salida.writeUTF(localIP); // Envía la IP local del cliente.
                    salida.flush(); // Asegura que los datos se envíen.
                    System.out.println("Datos enviados con exito");
                } catch (ConnectException e) {
                    // Error si no se puede conectar con el servidor Worker.
                    System.err.println("No se pudo conectar con el servidor en " + HOST_WORKER + ":" + PUERTO_WORKER0);
                    System.out.println("Por favor revise la IP y puerto, y asegurese de que el servidor está en ejecucion.");
                    break;
                }

                // Espera a que el thread del servidor termine.
                servidorThread.join();

                // Calcula y muestra el tiempo total de espera.
                long tiempoFin = System.currentTimeMillis();
                System.out.println("\nTiempo total de espera: " + ((tiempoFin - tiempoInicio) / 1000.0) + " segundos");
            }
        } catch (Exception e) {
            // Captura errores generales en el cliente.
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Permite al usuario configurar la IP del servidor Worker.
    private static void configurarHostWorker(Scanner scanner) {
        System.out.println("Ingrese la direccion IP del host para los workers");
        System.out.println("(presione Enter para usar " + HOST_WORKER + "): ");
        String ip = scanner.nextLine();
        if (!ip.trim().isEmpty()) {
            HOST_WORKER = ip;
        }
        System.out.println("Usando la direccion IP: " + HOST_WORKER);
    }

    // Genera un vector de números aleatorios con una longitud dada.
    private static int[] generarVectorAleatorio(int n) {
        int[] vector = new int[n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            vector[i] = random.nextInt(1000); // Números aleatorios entre 0 y 999.
        }
        return vector;
    }

    // Lee un vector desde un archivo de texto.
    private static int[] leerVectorDesdeArchivo(String rutaArchivo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            StringBuilder contenido = new StringBuilder();
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append(","); // Agrega cada línea al contenido.
            }
            String[] numerosStr = contenido.toString().split(",");
            int[] vector = new int[numerosStr.length];
            for (int i = 0; i < numerosStr.length; i++) {
                vector[i] = Integer.parseInt(numerosStr[i].trim()); // Convierte a entero.
            }
            return vector;
        }
    }

    // Inicia un servidor que escucha y procesa el vector ordenado enviado por el Worker.
    private static void esperarRespuesta() throws IOException, ClassNotFoundException {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_CLIENTE)) {
            System.out.println("Esperando vector ordenado...");
            try (Socket workerSocket = serverSocket.accept(); ObjectInputStream entrada = new ObjectInputStream(workerSocket.getInputStream())) {
                int[] vectorOrdenado = (int[]) entrada.readObject(); // Recibe el vector ordenado.
                System.out.println("Vector ordenado: " + Arrays.toString(vectorOrdenado));
            }
        }
    }
}
