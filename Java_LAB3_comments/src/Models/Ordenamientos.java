// Ordenamientos.java
package Models;

public class Ordenamientos {

    // MergeSort
    // Función principal para ordenar el arreglo usando el algoritmo MergeSort
    public void mergeSort(int[] arr, int left, int right) {
        // Condición de parada: si el subarreglo tiene más de un elemento
        if (left < right) {
            // Encuentra el punto medio del arreglo
            int middle = (left + right) / 2;
            
            // Llamadas recursivas para ordenar la primera y segunda mitad
            mergeSort(arr, left, middle); // Ordenar la mitad izquierda
            mergeSort(arr, middle + 1, right); // Ordenar la mitad derecha

            // Fusionar las dos mitades ordenadas
            merge(arr, left, middle, right);
        }
    }

    // Función auxiliar que fusiona dos subarreglos ordenados en un solo arreglo ordenado
    private void merge(int[] arr, int left, int middle, int right) {
        // Longitudes de los dos subarreglos
        int n1 = middle - left + 1;
        int n2 = right - middle;

        // Crear arreglos temporales para los dos subarreglos
        int[] L = new int[n1];
        int[] R = new int[n2];

        // Copiar los datos de arr en los arreglos temporales L y R
        for (int i = 0; i < n1; ++i) {
            L[i] = arr[left + i];
        }
        for (int j = 0; j < n2; ++j) {
            R[j] = arr[middle + 1 + j];
        }

        // Mezclar los dos subarreglos ordenados
        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        // Copiar los elementos restantes del subarreglo L (si los hay)
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        // Copiar los elementos restantes del subarreglo R (si los hay)
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    // QuickSort
    // Función principal para ordenar el arreglo usando el algoritmo QuickSort
    public void quickSort(int[] arr, int low, int high) {
        // Condición de parada: si el subarreglo tiene más de un elemento
        if (low < high) {
            // Encuentra el índice del pivote (elemento que divide el arreglo)
            int pi = partition(arr, low, high);
            
            // Llamadas recursivas para ordenar las dos mitades alrededor del pivote
            quickSort(arr, low, pi - 1); // Ordenar la mitad izquierda
            quickSort(arr, pi + 1, high); // Ordenar la mitad derecha
        }
    }

    // Función que realiza la partición del arreglo y coloca el pivote en su posición correcta
    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high]; // El pivote es el último elemento del subarreglo
        int i = (low - 1); // Índice del elemento más pequeño (antes de empezar a comparar)

        // Comparar cada elemento con el pivote y organizar el arreglo
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                // Intercambiar arr[i] y arr[j]
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        // Colocar el pivote en su posición correcta
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1; // Retorna el índice del pivote
    }

    // HeapSort
    // Función principal para ordenar el arreglo usando el algoritmo HeapSort
    public void heapSort(int[] arr) {
        int n = arr.length;

        // Construir el heap máximo (el árbol binario debe cumplir con la propiedad de heap)
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i); // Llamada a heapify para asegurar que el subárbol sea un heap
        }

        // Extraer elementos del heap uno por uno y colocar el mayor al final
        for (int i = n - 1; i > 0; i--) {
            // Mover el elemento más grande (la raíz) al final
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // Llamar a heapify en el heap reducido (sin el último elemento)
            heapify(arr, i, 0);
        }
    }

    // Función que ajusta un subárbol para cumplir con la propiedad de un heap máximo
    private void heapify(int[] arr, int n, int i) {
        int largest = i; // Inicializar el más grande como la raíz
        int left = 2 * i + 1; // Índice del hijo izquierdo
        int right = 2 * i + 2; // Índice del hijo derecho

        // Si el hijo izquierdo es más grande que la raíz
        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }

        // Si el hijo derecho es más grande que el más grande hasta ahora
        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        // Si el más grande no es la raíz, intercambiar y continuar con heapify
        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Llamada recursiva para heapificar el subárbol afectado
            heapify(arr, n, largest);
        }
    }
}
