import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket; // Socket para la conexión con el servidor
    private DataInputStream inputbuffer = null; // Flujo de entrada para recibir datos del servidor
    private DataOutputStream outputbuffer = null; // Flujo de salida para enviar datos al servidor
    Scanner keyboard = new Scanner(System.in); // Objeto Scanner para leer la entrada del usuario
    final String TERMINATION_COMMAND = "go out()"; // Comando de terminación de la comunicación

    // Método para establecer la conexión con el servidor
    public void raiserConnection(String ip, int puerto) {
        try {
            // Se crea un nuevo socket con la dirección IP y el puerto especificados
            socket = new Socket(ip, puerto);
            // Se muestra un mensaje indicando que se ha conectado al servidor
            showText("Connected to :" + socket.getInetAddress().getHostName());
        } catch (Exception e) {
            // En caso de excepción al establecer la conexión, se muestra un mensaje de error y se sale del programa
            showText("Exception when lifting Connection: " + e.getMessage());
            System.exit(0);
        }
    }

    // Método para mostrar un texto en la consola
    public static void showText(String s) {
        System.out.println(s);
    }

    // Método para abrir los flujos de entrada y salida
    public void openFlows() {
        try {
            // Se crea un flujo de entrada para recibir datos del servidor
            inputbuffer = new DataInputStream(socket.getInputStream());
            // Se crea un flujo de salida para enviar datos al servidor
            outputbuffer = new DataOutputStream(socket.getOutputStream());
            // Se vacía el flujo de salida
            outputbuffer.flush();
        } catch (IOException e) {
            // En caso de error al abrir los flujos, se muestra un mensaje de error
            showText("Error in opening flows");
        }
    }

    // Método para enviar datos al servidor
    public void send(String s) {
        try {
            // Se envía la cadena s al servidor a través del flujo de salida
            outputbuffer.writeUTF(s);
            // Se vacía el flujo de salida
            outputbuffer.flush();
        } catch (IOException e) {
            // En caso de excepción al enviar datos, se muestra un mensaje de error
            showText("IOException on send");
        }
    }

    // Método para cerrar la conexión con el servidor
    public void closeConnection() {
        try {
            // Se cierran los flujos de entrada y salida, así como el socket
            inputbuffer.close();
            outputbuffer.close();
            socket.close();
            // Se muestra un mensaje indicando que se ha finalizado la conexión
            showText("Connection finished");
        } catch (IOException e) {
            // En caso de excepción al cerrar la conexión, se muestra un mensaje de error
            showText("IOException on closeConnection()");
        } finally {
            // Se sale del programa
            System.exit(0);
        }
    }

    // Método para ejecutar la conexión con el servidor en un hilo separado
    public void executeConnection(String ip, int puerto) {
        Thread hilo = new Thread(() -> {
            try {
                // Se establece la conexión con el servidor
                raiserConnection(ip, puerto);
                // Se abren los flujos de entrada y salida
                openFlows();
                // Se empieza a recibir datos del servidor
                receiveData();
            } finally {
                // Se cierra la conexión con el servidor
                closeConnection();
            }
        });
        // Se inicia el hilo
        hilo.start();
    }

    // Método para recibir datos del servidor
    public void receiveData() {
        String st = "";
        try {
            // Se recibe continuamente datos del servidor hasta que se recibe el comando de terminación
            do {
                // Se lee una cadena del flujo de entrada
                st = inputbuffer.readUTF();
                // Se muestra el mensaje recibido del servidor en la consola
                showText("\n[Servidor] => " + st);
                System.out.print("\n[You] => ");
            } while (!st.equals(TERMINATION_COMMAND));
        } catch (IOException e) {
            // En caso de excepción al recibir datos, no se hace nada
        }
    }

    // Método para enviar datos al servidor
    public void writeData() {
        String entrance = "";
        // Se solicita continuamente al usuario que ingrese datos hasta que decida salir
        while (true) {
            System.out.print("[Usted] => ");
            // Se lee la entrada del usuario
            entrance = keyboard.nextLine();
            // Si la entrada no está vacía, se envía al servidor
            if (entrance.length() > 0)
                send(entrance);
        }
    }

    // Método principal del programa
    public static void main(String[] argumentos) {
        // Se crea una instancia de la clase Client
        Client customer = new Client();
        Scanner scanner = new Scanner(System.in);
        // Se solicita al usuario que ingrese la dirección IP del servidor
        showText("Enter the IP: [localhost by default] ");
        String ip = scanner.nextLine();
        // Si no se ingresa ninguna dirección IP, se utiliza "localhost" por defecto
        if (ip.length() <= 0) ip = "localhost";

        // Se solicita al usuario que ingrese el puerto del servidor
        showText("Port: [5050 by default] ");
        String puerto = scanner.nextLine();
        // Si no se ingresa ningún puerto, se utiliza "5050" por defecto
        if (puerto.length() <= 0) puerto = "5050";
        // Se ejecuta la conexión con el servidor utilizando la dirección IP y el puerto especificados
        customer.executeConnection(ip, Integer.parseInt(puerto));
        // Se comienza a enviar datos al servidor
        customer.writeData();
    }
}