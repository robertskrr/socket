package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class ServidorSocketStream {

	public static void main(String[] args) {
		try {
			System.out.println("Creando socket servidor");

			ServerSocket serverSocket = new ServerSocket();

			System.out.println("Realizando el bind");

			// 1. Configura el servidor para escuchar en IP válida (recomendado: 0.0.0.0 si
			// procede) y un puerto elegido: 6666
			InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 6666);
			serverSocket.bind(addr);

			// 3. Añade logs de IP/puerto y del cliente remoto.
			System.out.println("Servidor escuchando en IP: " + addr.getHostName() + " Puerto: " + addr.getPort());

			Socket newSocket = serverSocket.accept();

			System.out.println("Conexión recibida desde: " + newSocket.getRemoteSocketAddress());

			InputStream is = newSocket.getInputStream();
			OutputStream os = newSocket.getOutputStream();

			byte[] mensaje = new byte[100]; // Reajustado bytes por mensaje más amplio
			is.read(mensaje);

			System.out.println("Mensaje recibido: " + new String(mensaje));

			System.out.println("Cerrando el nuevo socket");
			newSocket.close();

			System.out.println("Cerrando el socket servidor");
			serverSocket.close();

			System.out.println("Terminado");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}