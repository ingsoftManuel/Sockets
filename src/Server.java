import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private Socket socket; // Socket para la conexión con el cliente
    private ServerSocket serverSocket; // Socket del servidor
    private DataInputStream inputbuffer = null; // Flujo de entrada para recibir datos del cliente
    private DataOutputStream outputbuffer = null; // Flujo de salida para enviar datos al cliente
    Scanner scanner = new Scanner(System.in); // Objeto Scanner para leer la entrada del usuario
    final String TERMINATION_COMMAND = "go out()"; // Comando de terminación de la comunicación

    // Método para establecer la conexión con el cliente
    public void raiseConnection(int puerto) {
        try {
            // Se crea un ServerSocket que escucha en el puerto especificado
            serverSocket = new ServerSocket(puerto);
            // Se muestra un mensaje indicando que el servidor está esperando conexiones entrantes en el puerto especificado
            showText("Waiting for incoming connection on port " + String.valueOf(puerto) + "...");
            // Se acepta la conexión entrante y se crea un socket para la comunicación con el cliente
            socket = serverSocket.accept();
            // Se muestra un mensaje indicando que se ha establecido la conexión con el cliente
            showText("Connection established with: " + socket.getInetAddress().getHostName() + "\n\n\n");
        } catch (Exception e) {
            // En caso de excepción al establecer la conexión, se muestra un mensaje de error y se sale del programa
            showText("Error in raiseConnection(): " + e.getMessage());
            System.exit(0);
        }
    }

    // Método para abrir los flujos de entrada y salida
    public void flows() {
        try {
            // Se crea un flujo de entrada para recibir datos del cliente
            inputbuffer = new DataInputStream(socket.getInputStream());
            // Se crea un flujo de salida para enviar datos al cliente
            outputbuffer = new DataOutputStream(socket.getOutputStream());
            // Se vacía el flujo de salida
            outputbuffer.flush();
        } catch (IOException e) {
            // En caso de error al abrir los flujos, se muestra un mensaje de error
            showText("Error opening flows");
        }
    }

    // Método para recibir datos del cliente
    public void receiveData() {
        String st = "";
        try {
            // Se recibe continuamente datos del cliente hasta que se recibe el comando de terminación
            do {
                // Se lee una cadena del flujo de entrada
                st = inputbuffer.readUTF();
                // Se muestra el mensaje recibido del cliente en la consola
                showText("\n[Cliente] => " + st);
                System.out.print("\n[Usted] => ");
            } while (!st.equals(TERMINATION_COMMAND));
        } catch (IOException e) {
            // En caso de excepción al recibir datos, se cierra la conexión con el cliente
            closeConnection();
        }
    }

    // Método para enviar datos al cliente
    public void send(String s) {
        try {
            // Se envía la cadena s al cliente a través del flujo de salida
            outputbuffer.writeUTF(s);
            // Se vacía el flujo de salida
            outputbuffer.flush();
        } catch (IOException e) {
            // En caso de excepción al enviar datos, se muestra un mensaje de error
            showText("Error sending(): " + e.getMessage());
        }
    }

    // Método para mostrar un texto en la consola sin un salto de línea adicional
    public static void showText(String s) {
        System.out.print(s);
    }

    // Método para enviar datos al cliente
    public void writeData() {
        while (true) {
            System.out.print("[You] => ");
            // Se solicita al usuario que ingrese datos y se envían al cliente
            send(scanner.nextLine());
        }
    }

    // Método para cerrar la conexión con el cliente
    public void closeConnection() {
        try {
            // Se cierran los flujos de entrada y salida, así como el socket
            inputbuffer.close();
            outputbuffer.close();
            socket.close();
        } catch (IOException e) {
            // En caso de excepción al cerrar la conexión, se muestra un mensaje de error
            showText("Exception in closeConnection(): " + e.getMessage());
        } finally {
            // Se muestra un mensaje indicando que la conversación ha terminado y se sale del programa
            showText("Conversation ended....");
            System.exit(0);
        }
    }

    // Método para ejecutar la conexión con el cliente en un hilo separado
    public void executeConnection(int puerto) {
        Thread hilo = new Thread(() -> {
            while (true) {
                try {
                    // Se establece la conexión con el cliente
                    raiseConnection(puerto);
                    // Se abren los flujos de entrada y salida
                    flows();
                    // Se empieza a recibir datos del cliente
                    receiveData();
                } finally {
                    // Se cierra la conexión con el cliente
                    closeConnection();
                }
            }
        });
        // Se inicia el hilo
        hilo.start();
    }

    // Método principal del programa
    public static void main(String[] args) throws IOException {
        // Se crea una instancia de la clase Server
        Server s = new Server();
        Scanner sc = new Scanner(System.in);

        // Se solicita al usuario que ingrese el puerto en el que el servidor escuchará las conexiones entrantes
        showText("Enter the port [1000 by default]: ");
        String puerto = sc.nextLine();
        // Si no se ingresa ningún puerto, se utiliza "1000" por defecto
        if (puerto.length() <= 0) puerto = "1000";
        // Se ejecuta la conexión con el cliente en el puerto especificado
        s.executeConnection(Integer.parseInt(puerto));
        // Se comienza a enviar datos al cliente
        s.writeData();
    }
}