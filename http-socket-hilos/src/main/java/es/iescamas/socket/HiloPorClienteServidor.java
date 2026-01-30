package es.iescamas.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor TCP que atiende clientes mediante un hilo por conexión.
 * Sirve HTML básico y un favicon desde src/main/resources/favicon.ico
 * @author Robert Esquerre Valiente
 * @version 1.0
 * @since 2026-01
 */
public class HiloPorClienteServidor implements Runnable {

    /** Puerto donde escucha el servidor. */
    protected int serverPort = 9001;

    /** Socket servidor. */
    protected ServerSocket serversocket = null;

    /** Flag de parada. */
    protected boolean isStopped;

    /** Referencia al hilo que ejecuta run(). */
    protected Thread runningThread = null;

    /**
     * Constructor
     * @param serverPort Puerto del servidor
     */
    public HiloPorClienteServidor(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        while (!isStopped()) {
            try {
                Socket clientSocket = this.serversocket.accept();

                new Thread(() -> {
                    try {
                        processClientRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "client-" + clientSocket.getPort()).start();

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }

        System.out.println("Server Stopped");
    }

    /**
     * Procesa la conexión de un cliente.
     * @param clientSocket Conexión con el cliente
     * @throws IOException
     * @apiNote Ejemplos de rutas:
     * <pre>
     * http://localhost:9090/nombre/Robert
     * </pre>
     * - Ruta dinámica: /nombre/Robert --> Responde "Hola Robert"
     * - Ruta vacía: / --> Responde "Servidor OK"
     * - Ruta desconocida: Responde "404 Not Found" 
     */
    private void processClientRequest(Socket clientSocket) throws IOException {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
             OutputStream out = clientSocket.getOutputStream()) {

            // 1) Leer la primera línea: "GET /ruta HTTP/1.1"
            String requestLine = br.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String path = "/";
            if (requestLine.startsWith("GET ")) {
                int start = 4;
                int end = requestLine.indexOf(' ', start);
                if (end > start) 
                	path = requestLine.substring(start, end);
            }

            // 2) Favicon: servir el fichero real desde resources y salir
            if ("/favicon.ico".equals(path)) {
                serveFavicon(out);
                return;
            }

            // 3) Datos del cliente
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort(); // puerto remoto del cliente
            String remote = clientSocket.getRemoteSocketAddress().toString(); // /IP:PUERTO

            long time = System.currentTimeMillis();
            String fecha = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date(time));
            
            // MEJORA 1: RUTA DINÁMICA
            // MEJORA 2: MANEJO DE RUTAS 404 NOT FOUND
            String mensajeBienvenida = "👍 Servidor OK 👍";
            String body;
            String estado = "200 OK";
            
            // Ruta vacía
            if (path.equals("/")) {
            	body = generarHtmlDetallado(mensajeBienvenida, path, fecha, clientIp, clientPort, remote);
			} else if (path.startsWith("/nombre/")) { // Ruta con nombre para saludo
				// Extraemos el nombre
            	String nombre = path.substring(8);
            	
            	// Si no esta vacío cambiamos el mensaje
            	if (!nombre.isEmpty()) {
					mensajeBienvenida = "😊 Hola " + nombre + " 😊";
				}
            	body = generarHtmlDetallado(mensajeBienvenida, path, fecha, clientIp, clientPort, remote);
            } else {
            	// Ruta inexistente
				estado = "404 Not Found";
				body = "<html><body><h1>404 Not Found</h1><p>La página no existe.</p></body></html>";
			}
            

            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            String headers =
                    "HTTP/1.1" + estado + "\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            // Log: ignorar favicon (ya se devuelve arriba) y registrar petición normal
            System.out.println("[" + Thread.currentThread().getName() + "] " + requestLine);
            System.out.println("[" + Thread.currentThread().getName() + "] Cliente: " + remote);
            System.out.println("[" + Thread.currentThread().getName() + "] Petición procesada: " + fecha);
        }
    }
    
    // MEJORA 3: Mejorar el HTML
    /**
     * Genera el html detallado al que solo hay que pasar los datos
     * @param titulo Mensaje principal de la página
     * @param path Ruta del cliente
     * @param fecha Hora actual del servidor
     * @param ip Dirección IP del cliente
     * @param port Puerto remoto del cliente
     * @param remote Dirección completa del socket remoto
     * @return HTML Detallado
     */
    private String generarHtmlDetallado(String titulo, String path, String fecha, String ip, int port, String remote) {
    	return "<html>"
                + "<head>"
                + "<title>Servidor PSP</title>"
                + "<style>"
                + "  body { background-color: #fdf2f0; font-family: Arial; text-align: center; padding-top: 50px; }"
                + "  .caja { background-color: white; border: 3px solid coral; border-radius: 10px; "
                + "          display: inline-block; padding: 20px; text-align: left; min-width: 300px; }"
                + "  h3 { color: blue; margin-top: 0; }"
                + "  .dato { font-weight: bold; color: #555; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class='caja'>"
                + "    <h3>" + titulo + "</h3>"
                + "    <p><span class='dato'>📍 Ruta:</span> " + path + "</p>"
                + "    <p><span class='dato'>🕒 Fecha:</span> " + fecha + "</p>"
                + "    <p><span class='dato'>🧵 Hilo:</span> " + Thread.currentThread().getName() + "</p>"
                + "    <hr>"
                + "    <p><span class='dato'>💻 Tu IP:</span> " + ip + "</p>"
                + "    <p><span class='dato'>🔌 Puerto:</span> " + port + "</p>"
                + "<p><span class='dato'>🔗 Remote:</span> " + remote + "</p>"
                + "  </div>"
                + "</body></html>";
    }

    /**
     * Sirve el favicon real desde el classpath: src/main/resources/favicon.ico
     */
    private void serveFavicon(OutputStream out) throws IOException {
        try (InputStream iconStream = HiloPorClienteServidor.class.getResourceAsStream("/favicon.ico")) {

            if (iconStream == null) {
                out.write(("HTTP/1.1 404 Not Found\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();
                return;
            }

            byte[] iconBytes = iconStream.readAllBytes();

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: image/x-icon\r\n" +
                    "Content-Length: " + iconBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(iconBytes);
            out.flush();
        }
    }

    /**
     * 
     * @return true si el servidor ha recibido la orden de parada o está cerrado
     */
    private synchronized boolean isStopped() {
        return isStopped;
    }

    private void openServerSocket() {
        try {
            this.serversocket = new ServerSocket(this.serverPort);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open port " + serverPort, ex);
        }
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (this.serversocket != null) {
                this.serversocket.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
