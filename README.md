# LAB3_EDD2

## Instrucciones para Ejecutar el Programa

### Requisitos Previos

- Asegúrate de tener instalado Java Development Kit (JDK) en tu sistema. Puedes verificar la instalación ejecutando el siguiente comando en tu terminal:
  ```sh
  java -version
  ```
- Asegúrate de tener configurado tu entorno de desarrollo con el Java Compiler (javac).
- Las clases del programa deben estar organizadas dentro del paquete `Models`.

### Paso 1: Compilar el Código

- Dirígete a la carpeta donde se encuentran los archivos `.java` del programa.
- Ejecuta el siguiente comando para compilar todos los archivos `.java`:
  ```sh
  javac Models/*.java
  ```
- Esto generará los archivos `.class` correspondientes a las clases del programa.

### Paso 2: Ejecutar la Clase Worker0

La clase `Worker0` es el primer componente que debes ejecutar, ya que es el servidor principal que escucha las conexiones de los clientes y reenvía las solicitudes a `Worker1` si es necesario.

- Para ejecutar `Worker0`, abre una terminal y navega al directorio donde se encuentran los archivos compilados.
- Ejecuta el siguiente comando:
  ```sh
  java Models.Worker0
  ```
- Esto iniciará el servidor `Worker0` en el puerto 8080. Verás un mensaje similar a este:
  ```
  Worker0 iniciado en puerto 8080
  IP del Worker0: 192.168.0.101
  ```

### Paso 3: Ejecutar la Clase Worker1

Una vez que `Worker0` esté en ejecución, ahora puedes ejecutar `Worker1`, que escuchará las solicitudes reenviadas por `Worker0` en el puerto 8081.

- Abre una nueva terminal (sin cerrar la de `Worker0`).
- Ejecuta el siguiente comando:
  ```sh
  java Models.Worker1
  ```
- Esto iniciará el servidor `Worker1` en el puerto 8081. Verás un mensaje similar a este:
  ```
  Worker1 iniciado en puerto 8081
  IP del Worker1: 192.168.0.102
  ```

### Paso 4: Ejecutar la Clase Cliente

Una vez que tanto `Worker0` como `Worker1` están en ejecución, puedes ejecutar la clase `Cliente` para simular una conexión desde un cliente a `Worker0`, que a su vez podría reenviar la solicitud a `Worker1` si el tiempo se agota.

- Abre otra nueva terminal (sin cerrar las terminales de `Worker0` y `Worker1`).
- Ejecuta el siguiente comando:
  ```sh
  java Models.Cliente
  ```
- El cliente se conectará al `Worker0` en el puerto 8080, enviará el vector y las configuraciones de ordenamiento, y esperará los resultados. Verás un mensaje similar a este:
  ```
  Conectando al Worker0 en puerto 8080
  ```
- Si el tiempo de ejecución del ordenamiento se agota, el cliente recibirá una actualización indicando que la solicitud fue reenviada a `Worker1`.
  ```
  El tiempo se agotó. Recibiendo resultados de Worker1.
  ```
- Al finalizar, el cliente mostrará los resultados del vector ordenado.

### Flujo de Ejecución Completo

1. Inicia `Worker0` en el puerto 8080.
2. Inicia `Worker1` en el puerto 8081.
3. Inicia `Cliente`, que se conecta primero a `Worker0`, y en caso de que sea necesario, la solicitud se reenviará a `Worker1`.
